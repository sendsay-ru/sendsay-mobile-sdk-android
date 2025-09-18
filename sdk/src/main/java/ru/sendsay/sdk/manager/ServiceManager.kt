package ru.sendsay.sdk.manager

import android.content.Context
import ru.sendsay.sdk.models.FlushPeriod
import ru.sendsay.sdk.services.OnIntegrationStoppedCallback

internal interface ServiceManager : OnIntegrationStoppedCallback {
    fun startPeriodicFlush(context: Context, flushPeriod: FlushPeriod)
    fun stopPeriodicFlush(context: Context)
    override fun onIntegrationStopped()
}
