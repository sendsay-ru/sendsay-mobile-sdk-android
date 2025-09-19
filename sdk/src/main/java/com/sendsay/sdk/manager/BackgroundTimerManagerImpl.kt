package com.sendsay.sdk.manager

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.sendsay.sdk.models.SendsayConfiguration
import com.sendsay.sdk.repository.SendsayConfigRepository
import com.sendsay.sdk.services.SendsaySessionEndWorker
import com.sendsay.sdk.util.Logger
import java.util.concurrent.TimeUnit

/**
 * Handles background session tracking
 */
internal class BackgroundTimerManagerImpl(
    context: Context,
    private val configuration: SendsayConfiguration
) : BackgroundTimerManager {
    val application = context.applicationContext
    private val keyUniqueName = "KeyUniqueName"

    /**
     * Method will setup a timer  for a time period obtained from configuration
     */
    override fun startTimer() {
        SendsayConfigRepository.set(application, configuration)
        val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

        // Build one time work request
        val workRequest = OneTimeWorkRequest
                .Builder(SendsaySessionEndWorker::class.java)
                .setConstraints(constraints)
                .setInitialDelay(configuration.sessionTimeout.toLong(), TimeUnit.SECONDS)
                .build()

        // Enqueue request
        WorkManager
                .getInstance(application)
                .beginUniqueWork(
                        keyUniqueName,
                        ExistingWorkPolicy.REPLACE,
                        workRequest
                )
                .enqueue()

        Logger.d(this, "BackgroundTimerManagerImpl.start() -> enqueued background task...")
    }

    /**
     * Cancel timer that was set previously
     */
    override fun stopTimer() {
        Logger.d(this, "BackgroundTimerManagerImpl.stop() -> cancelling all work")
        WorkManager
                .getInstance(application)
                .cancelUniqueWork(keyUniqueName)
    }
}
