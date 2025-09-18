package ru.sendsay.sdk.manager

import ru.sendsay.sdk.services.OnIntegrationStoppedCallback
import ru.sendsay.sdk.util.OnForegroundStateListener
import ru.sendsay.sdk.util.currentTimeSeconds
import ru.sendsay.sdk.util.logOnException

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
