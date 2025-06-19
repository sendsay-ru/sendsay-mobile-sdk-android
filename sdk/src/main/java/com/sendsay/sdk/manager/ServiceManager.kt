package com.sendsay.sdk.manager

import android.content.Context
import com.sendsay.sdk.models.FlushPeriod
import com.sendsay.sdk.services.OnIntegrationStoppedCallback

internal interface ServiceManager : OnIntegrationStoppedCallback {
    fun startPeriodicFlush(context: Context, flushPeriod: FlushPeriod)
    fun stopPeriodicFlush(context: Context)
    override fun onIntegrationStopped()
}
