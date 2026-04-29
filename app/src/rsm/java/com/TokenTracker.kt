import android.content.Context
import android.os.Build
import android.text.Html
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sendsay.sdk.Sendsay
import com.sendsay.sdk.util.Logger
import com.sendsay.sdk.util.copyToClipboard
import ru.rustore.sdk.core.exception.RuStoreException
import ru.rustore.sdk.core.feature.model.FeatureAvailabilityResult
import ru.rustore.sdk.pushclient.RuStorePushClient
import ru.rustore.sdk.pushclient.utils.resolveForPush
import ru.rustore.sdk.core.tasks.OnCompletionListener
import ru.rustore.sdk.pushclient.messaging.model.TestNotificationPayload

class TokenTracker {
    companion object {
        const val LOG_TAG = "TokenTracker"
    }

    var lastToken = "wait and try again"

    fun checkPushAvailability(context: Context) : Boolean {
        val isInstalled = try {
            context.packageManager.getPackageInfo("ru.vk.store", 0)
            true
        } catch (e: Exception) {
            val result = false
            Logger.d(LOG_TAG, "RuStore installed = $result!")
            showAlertDialogWithUrl(context)
            result
        }
        Logger.d(LOG_TAG, "RuStore installed = $isInstalled")

        RuStorePushClient.checkPushAvailability()
            .addOnSuccessListener { result ->
                Logger.i(LOG_TAG, "checkPushAvailability SUCCESS !")
                when (result) {
                    FeatureAvailabilityResult.Available -> {
                        Logger.i(LOG_TAG, "checkPushAvailability -> ! AVAILABLE !")
                    }

                    is FeatureAvailabilityResult.Unavailable -> {
                        Logger.i(LOG_TAG, "checkPushAvailability -> ...UNAVAILABLE...")
                        result.cause.resolveForPush(context)
                    }
                }
            }
            .addOnFailureListener { throwable ->
                showAlertDialogWithUrl(context)
                Logger.e(LOG_TAG, "checkPushAvailability onFailure", throwable)
            }

        return isInstalled
    }

    fun trackToken(context: Context?) {
        var isPushAvailable : Boolean? = null
        // Проверяем установлен ли RuStore или другой VkCore на устройстве:
        context?.let { isPushAvailable = checkPushAvailability(it) }

        if (isPushAvailable == true) object : Thread() {
            override fun run() {
                try {
                    val token = RuStorePushClient.getToken()
                        .addOnSuccessListener { result ->
                            Logger.d(LOG_TAG, "getToken onSuccess token = $result")
                        }
                        .addOnFailureListener { throwable ->
                            Logger.e(LOG_TAG, "getToken onFailure", throwable)
                        }.await()

                    // Check whether the token is empty.
                    if (!TextUtils.isEmpty(token)) {
                        Sendsay.trackRsmPushToken(token)
                    }
                } catch (e: RuStoreException) {
                    Logger.e(this, "get rustore push token failed, $e")
                }
            }
        }.start()
    }

    fun getToken(context: Context?): String {
//        this.lifecycleScope.launch{
        RuStorePushClient.getToken()
            .addOnSuccessListener { token ->
                // Check the token is empty.
                if (!TextUtils.isEmpty(token)) {
                    lastToken = token
                    context?.copyToClipboard(token)
                }
                Logger.d(LOG_TAG, "getToken onSuccess token = $token")
            }
            .addOnFailureListener { throwable ->
                Toast.makeText(context, "Токен недоступен", Toast.LENGTH_SHORT).show()
                Logger.e(LOG_TAG, "getToken onFailure", throwable)
            }
//        }
        return lastToken
    }

    fun testLocalPush(context: Context?) {
        context?.let { checkPushAvailability(it) }

        val testNotificationPayload = TestNotificationPayload(
            title = "RuStore Push Title",
            body = "testRsmLocalPush-Puck-Serenk",
            imgUrl = "https://static.rustore.ru/rustore-strapi/6/logo_color_30_px_2_fa2039288f.svg",
            data = mapOf("some_key" to "some_value")
        )

        RuStorePushClient.sendTestNotification(testNotificationPayload)
            .addOnCompletionListener {
                Logger.d(LOG_TAG, "Test Local Push Completed")
            }.addOnSuccessListener {
                Toast.makeText(context, "Пуш отправлен!", Toast.LENGTH_SHORT).show()
                Logger.d(LOG_TAG, "Test Local Push Sended")
            }.addOnFailureListener { throwable ->
                Toast.makeText(context, "Пуш сломался =(", Toast.LENGTH_SHORT).show()
                Logger.e(LOG_TAG, "Test Local Push onFailure", throwable)
            }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun showAlertDialogWithUrl(context: Context) {
        val url = "https://www.rustore.ru/instruction"
        val linkText = "Скачать RuStore"

        // Construct the HTML message string
        val message = "Ссылка на приложение:\n<a href=\"$url\">$linkText</a>"

        val builder = MaterialAlertDialogBuilder(context)
        builder.setTitle("Пожалуйста, установите RuStore!\nЧтобы работали пуш-уведомления.")
            .setMessage(Html.fromHtml(message, Html.FROM_HTML_MODE_COMPACT))
            .setCancelable(false)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }

        val alertDialog = builder.create()
        alertDialog.show()

        // IMPORTANT: Make the links clickable by setting the MovementMethod of the message TextView
        // This must be done AFTER the dialog is shown
        val msgTextView: TextView? = alertDialog.findViewById(android.R.id.message)
        msgTextView?.movementMethod = LinkMovementMethod.getInstance()
    }
}
