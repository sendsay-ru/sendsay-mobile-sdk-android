package ru.sendsay.sdk.repository

import ru.sendsay.sdk.services.OnIntegrationStoppedCallback

internal interface DeviceInitiatedRepository : OnIntegrationStoppedCallback {
    fun get(): Boolean
    fun set(boolean: Boolean)
    override fun onIntegrationStopped()
}
