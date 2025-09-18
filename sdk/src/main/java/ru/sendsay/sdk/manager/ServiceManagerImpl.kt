package ru.sendsay.sdk.manager

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import ru.sendsay.sdk.models.FlushPeriod
import ru.sendsay.sdk.services.SendsayContextProvider
import ru.sendsay.sdk.services.SendsayPeriodicFlushWorker

internal class ServiceManagerImpl : ServiceManager {

    override fun startPeriodicFlush(context: Context, flushPeriod: FlushPeriod) {
        val request = PeriodicWorkRequest.Builder(
                SendsayPeriodicFlushWorker::class.java,
                flushPeriod.amount,
                flushPeriod.timeUnit
        ).setConstraints(
                Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                SendsayPeriodicFlushWorker.WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                request
        )
    }

    override fun stopPeriodicFlush(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(SendsayPeriodicFlushWorker.WORK_NAME)
    }

    override fun onIntegrationStopped() {
        SendsayContextProvider.applicationContext?.let { stopPeriodicFlush(it) }
    }
}
