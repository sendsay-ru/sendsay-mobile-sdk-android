package com.sendsay.sdk.manager

import com.sendsay.sdk.services.OnIntegrationStoppedCallback
import com.sendsay.sdk.util.OnForegroundStateListener
import com.sendsay.sdk.util.currentTimeSeconds
import com.sendsay.sdk.util.logOnException

internal abstract class SessionManager : OnForegroundStateListener, OnIntegrationStoppedCallback {

    abstract fun onSessionStart()

    abstract fun onSessionEnd()

    abstract fun startSessionListener()

    abstract fun stopSessionListener()

    abstract fun trackSessionEnd(timestamp: Double = currentTimeSeconds())

    abstract fun trackSessionStart(timestamp: Double = currentTimeSeconds())

    abstract fun reset()

    override fun onStateChanged(isForeground: Boolean) {
        runCatching {
            if (isForeground) {
                onSessionStart()
            } else {
                onSessionEnd()
            }
        }.logOnException()
    }

    abstract override fun onIntegrationStopped()
}
