package com.sendsay.sdk.repository

import com.sendsay.sdk.Sendsay
import com.sendsay.sdk.preferences.SendsayPreferences
import com.sendsay.sdk.util.Logger

internal class DeviceInitiatedRepositoryImpl(private val preferences: SendsayPreferences) :
        DeviceInitiatedRepository {
    private val KEY = "SendsayDeviceInitiated"

    override fun get(): Boolean {
        if (Sendsay.isStopped) {
            Logger.e(this, "Install flag not loaded, SDK is stopping")
            return false
        }
        return preferences.getBoolean(KEY, false)
    }

    override fun set(boolean: Boolean) {
        preferences.setBoolean(KEY, boolean)
    }

    override fun onIntegrationStopped() {
        set(false)
    }
}
