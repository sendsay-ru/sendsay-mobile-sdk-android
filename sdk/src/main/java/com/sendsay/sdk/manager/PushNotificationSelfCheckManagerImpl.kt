package com.sendsay.sdk.manager

import android.app.Activity
import android.app.AlertDialog
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.sendsay.sdk.models.SendsayConfiguration
import com.sendsay.sdk.network.SendsayService
import com.sendsay.sdk.repository.CustomerIdsRepository
import com.sendsay.sdk.repository.PushTokenRepository
import com.sendsay.sdk.services.SendsayContextProvider
import com.sendsay.sdk.services.SendsayProjectFactory
import com.sendsay.sdk.util.SendsayGson
import com.sendsay.sdk.util.Logger
import com.sendsay.sdk.util.TokenType
import java.io.IOException
import java.lang.ref.WeakReference
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response

internal class PushNotificationSelfCheckManagerImpl(
    context: Context,
    private val configuration: SendsayConfiguration,
    private val customerIdsRepository: CustomerIdsRepository,
    private val tokenRepository: PushTokenRepository,
    private val flushManager: FlushManager,
    private val sendsayService: SendsayService,
    private val projectFactory: SendsayProjectFactory,
    private val operationsTimeout: Long = 15000
) : PushNotificationSelfCheckManager {
    private val presenter = SelfCheckDialogPresenter(context)
    private var selfCheckPushReceived = false

    private data class SelfCheckResponse(val success: Boolean)

    override fun selfCheckPushReceived() {
        selfCheckPushReceived = true
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun start() {
        GlobalScope.launch(Dispatchers.Main.immediate) {
            presenter.observeLifecycle()
            try {
                val result = withContext(Dispatchers.Default) { runSelfCheck() }
                logResult(result)
                presenter.show(result)
            } finally {
                presenter.stopObservingIfIdle()
            }
        }
    }

    private suspend fun runSelfCheck(): SelfCheckResult {
        Logger.i(this, "Waiting for push token.")
        val token = waitForPushToken() ?: return SelfCheckResult.TokenMissing

        Logger.i(this, "Requesting self-check push notification.")
        if (!requestSelfCheckPush(token, tokenRepository.getLastTokenType())) {
            return SelfCheckResult.RequestFailed
        }

        Logger.i(this, "Waiting for self-check push notification.")
        if (!waitForSelfCheckPushReceived()) return SelfCheckResult.PushNotReceived
        return SelfCheckResult.Success
    }

    private fun logResult(result: SelfCheckResult) {
        val message = "${result.title}. ${result.details.replace("\n", " ")}"
        if (result == SelfCheckResult.Success) Logger.i(this, message)
        else Logger.e(this, message)
    }

    private suspend fun waitForPushToken(): String? {
        val token = withTimeoutOrNull(operationsTimeout.milliseconds) {
            while (tokenRepository.get() == null) {
                delay(operationsTimeout.milliseconds / 10)
            }
            return@withTimeoutOrNull tokenRepository.get()
        }
        // we also have to wait for token to be tracked on Sendsay servers
        if (flushManager.isRunning) {
            while (flushManager.isRunning) delay(operationsTimeout.milliseconds / 10)
        } else {
            suspendCancellableCoroutine { continuation ->
                flushManager.flushData {
                    continuation.resume(Unit)
                }
            }
        }
        delay(operationsTimeout.milliseconds / 5) // wait a bit more to let Sendsay process the token
        return token
    }

    private suspend fun requestSelfCheckPush(
        pushToken: String,
        tokenType: TokenType
    ): Boolean = suspendCancellableCoroutine { continuation ->
        sendsayService.postPushSelfCheck(
            projectFactory.mainSendsayProject,
            customerIdsRepository.get(),
            pushToken,
            tokenType
        ).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val parsedResponse = SendsayGson.instance.fromJson<SelfCheckResponse>(
                        response.body.string(),
                        SelfCheckResponse::class.java
                    )
                    continuation.resume(parsedResponse.success)
                } else {
                    continuation.resume(false)
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                continuation.resume(false)
            }
        })
    }

    private suspend fun waitForSelfCheckPushReceived(): Boolean {
        withTimeoutOrNull(operationsTimeout.milliseconds) {
            while (!selfCheckPushReceived) {
                delay(operationsTimeout.milliseconds / 10)
            }
        }
        return selfCheckPushReceived
    }
}

internal enum class SelfCheckResult(
    private val completedSteps: Int,
    private val description: String,
) {
    TokenMissing(
        0,
        "Unable to get push token. Check your Firebase/HMS setup. " +
            "Don't forget to call Sendsay.handleNewToken() in your FirebaseMessagingService, " +
            "or Sendsay.handleNewHmsToken() in your HmsMessageService.",
    ),
    RequestFailed(
        1,
        "Unable to send self-check push notification from Sendsay. " +
            "Check your push notification setup in Sendsay administration.",
    ),
    PushNotReceived(
        2,
        "Unable to receive self-check push notification from Sendsay. " +
            "Check your push notification setup in Sendsay administration. " +
            "Don't forget to call Sendsay.handleRemoteMessage() in your FirebaseMessagingService " +
            "(HmsMessageService for Huawei integration)",
    ),
    Success(3, "You are now ready to receive push notifications from Sendsay.");

    val title: String
        get() = "Push notification setup self-check " +
            if (this == Success) "succeeded" else "failed"

    val details: String
        get() = "$description \n\nSelf-check only runs in debug builds.\n" +
            "To disable it, set Sendsay.checkPushSetup\u00A0=\u00A0false"

    val dialogMessage: String
        get() = buildString {
            for ((index, step) in steps.withIndex()) {
                if (index > completedSteps) break
                appendLine("${if (index < completedSteps) "✓" else "✗"} $step")
            }
            appendLine()
            append(details)
        }

    private companion object {
        val steps = listOf(
            "Track push token",
            "Request push notification",
            "Receive push notification",
        )
    }
}

internal class SelfCheckDialogPresenter(context: Context) {
    private val application = context.applicationContext as Application
    private val initialActivity = (context as? Activity)?.let { WeakReference(it) }
    private var currentResumedActivity: Activity? = null
    private var pendingResult: SelfCheckResult? = null
    private var resultDialog: AlertDialog? = null
    private var isObservingLifecycle = false

    private val lifecycleListener = object : Application.ActivityLifecycleCallbacks {
        override fun onActivityResumed(activity: Activity) {
            currentResumedActivity = activity
            showPendingResult()
        }

        override fun onActivityPaused(activity: Activity) = releaseActivity(activity)
        override fun onActivityDestroyed(activity: Activity) = releaseActivity(activity)
        override fun onActivityStarted(activity: Activity) {}
        override fun onActivityStopped(activity: Activity) {}
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    }

    fun show(result: SelfCheckResult) {
        pendingResult = result
        observeLifecycle()
        showPendingResult()
    }

    fun observeLifecycle() {
        if (isObservingLifecycle) return
        application.registerActivityLifecycleCallbacks(lifecycleListener)
        currentResumedActivity = SendsayContextProvider.resumedActivity
            ?: initialActivity?.get()?.takeIf { activity ->
                (activity as? LifecycleOwner)?.lifecycle?.currentState ==
                    Lifecycle.State.RESUMED
            }
        isObservingLifecycle = true
    }

    fun stopObservingIfIdle() {
        if (pendingResult != null || !isObservingLifecycle) return
        application.unregisterActivityLifecycleCallbacks(lifecycleListener)
        isObservingLifecycle = false
        currentResumedActivity = null
    }

    private fun showPendingResult() {
        val result = pendingResult ?: return
        val activity = currentResumedActivity ?: return
        if (resultDialog != null || activity.isFinishing || activity.isDestroyed) return

        val dialog = AlertDialog.Builder(activity)
            .setTitle(result.title)
            .setMessage(result.dialogMessage)
            .setNeutralButton("OK") { _, _ -> }
            .create()
        dialog.setOnDismissListener {
            if (resultDialog === dialog) {
                resultDialog = null
                pendingResult = null
                stopObservingIfIdle()
            }
        }
        resultDialog = dialog
        try {
            dialog.show()
        } catch (exception: WindowManager.BadTokenException) {
            resultDialog = null
            Logger.e(
                this,
                "Unable to show self-check result; waiting for next Activity.",
                exception,
            )
        }
    }

    private fun releaseActivity(activity: Activity) {
        if (currentResumedActivity !== activity) return
        currentResumedActivity = null
        val dialog = resultDialog
        resultDialog = null
        // Preserve the result when its Activity pauses or is recreated.
        dialog?.dismiss()
    }
}
