package ru.sendsay.sdk.services

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import ru.sendsay.sdk.Sendsay
import ru.sendsay.sdk.models.FlushMode
import ru.sendsay.sdk.util.Logger
import ru.sendsay.sdk.util.returnOnException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

internal class SendsayPeriodicFlushWorker(
    context: Context,
    workerParameters: WorkerParameters
) : Worker(context, workerParameters) {

    companion object {
        const val WORK_NAME = "SendsayPeriodicFlushWorker"
    }

    override fun doWork(): Result {
        Logger.d(this, "doWork -> Starting...")
        if (!Sendsay.isInitialized) {
            return Result.failure()
        }
        if (Sendsay.flushMode != FlushMode.PERIOD) {
            return Result.failure()
        }
        runCatching {
            val countDownLatch = CountDownLatch(1)
            Sendsay.flushData {
                // Once our flushing is done we want to tell the system that we finished and we should reschedule
                countDownLatch.countDown()
            }
            try {
                countDownLatch.await(20, TimeUnit.SECONDS)
            } catch (e: InterruptedException) {
                Logger.e(this, "doWork -> flush was interrupted", e)
                return Result.failure()
            }
        }.returnOnException { Result.failure() }
        return Result.success()
    }

    override fun onStopped() {
        super.onStopped()
        Logger.d(this, "onStopped")
    }
}
