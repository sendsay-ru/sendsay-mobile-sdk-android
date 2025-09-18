package ru.sendsay.sdk.services

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import ru.sendsay.sdk.Sendsay
import ru.sendsay.sdk.util.Logger
import ru.sendsay.sdk.util.returnOnException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit.SECONDS

internal class SendsaySessionEndWorker(
    context: Context,
    workerParameters: WorkerParameters
) : Worker(context, workerParameters) {

    override fun doWork(): Result {
        Logger.d(this, "doWork -> Starting...")
        return Sendsay.runCatching {
            Sendsay.requireInitialized {
                val countDownLatch = CountDownLatch(1)
                Sendsay.trackAutomaticSessionEnd()
                Logger.d(this, "doWork -> Starting flushing data")
                Sendsay.flushData {
                    Logger.d(this, "doWork -> Finished")
                    countDownLatch.countDown()
                }
                try {
                    var successFinish = countDownLatch.await(20, SECONDS)
                    if (!successFinish) {
                        Logger.e(this, "doWork -> Timeout!")
                        return@requireInitialized Result.failure()
                    }
                } catch (e: InterruptedException) {
                    Logger.e(this, "doWork -> countDownLatch was interrupted", e)
                    return@requireInitialized Result.failure()
                }
                Logger.d(this, "doWork -> Success!")
                return@requireInitialized Result.success()
            }
        }.returnOnException { Result.failure() } ?: Result.failure()
    }
}
