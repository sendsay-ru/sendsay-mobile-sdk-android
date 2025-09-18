package ru.sendsay.sdk.services

import android.app.Application
import android.content.Context
import ru.sendsay.sdk.Sendsay
import ru.sendsay.sdk.manager.SessionManagerImpl
import ru.sendsay.sdk.preferences.SendsayPreferencesImpl
import ru.sendsay.sdk.repository.AppInboxCacheImpl
import ru.sendsay.sdk.repository.CampaignRepositoryImpl
import ru.sendsay.sdk.repository.CustomerIdsRepositoryImpl
import ru.sendsay.sdk.repository.DeviceInitiatedRepositoryImpl
import ru.sendsay.sdk.repository.DrawableCacheImpl
import ru.sendsay.sdk.repository.EventRepositoryImpl
import ru.sendsay.sdk.repository.SendsayConfigRepository
import ru.sendsay.sdk.repository.FontCacheImpl
import ru.sendsay.sdk.repository.HtmlNormalizedCacheImpl
import ru.sendsay.sdk.repository.InAppContentBlockDisplayStateRepositoryImpl
import ru.sendsay.sdk.repository.InAppMessageDisplayStateRepositoryImpl
import ru.sendsay.sdk.repository.InAppMessagesCacheImpl
import ru.sendsay.sdk.repository.PushTokenRepositoryProvider
import ru.sendsay.sdk.repository.SegmentsCacheImpl
import ru.sendsay.sdk.repository.UniqueIdentifierRepositoryImpl
import ru.sendsay.sdk.telemetry.TelemetryManager
import ru.sendsay.sdk.telemetry.storage.FileTelemetryStorage
import ru.sendsay.sdk.util.SendsayGson
import ru.sendsay.sdk.util.Logger
import ru.sendsay.sdk.util.logOnException
import ru.sendsay.sdk.util.logOnException
import java.util.concurrent.ConcurrentLinkedQueue

class SendsayDeintegrateManager {
    val onIntegrationStoppedCallbacks = ConcurrentLinkedQueue<OnIntegrationStoppedCallback>()
    /**
     * Runs all callbacks that are required to be notified about SDK de-integration.
     * Mainly for stop some processes.
     */
    fun notifyDeintegration() {
        do {
            val pendingCallback = onIntegrationStoppedCallbacks.poll()
            pendingCallback?.let { executeCallbackSafely(it) }
        } while (pendingCallback != null)
        Logger.v(this, "All pending callbacks invoked")
    }
    /**
     * Runs `callback` code on SendsaySDK de-integration.
     * If SDK is already stopped then callback is not registered.
     */
    fun registerForIntegrationStopped(callback: OnIntegrationStoppedCallback) {
        if (Sendsay.isStopped) {
            Logger.e(this, "Callback for SDK de-integration is not registered because SDK is already stopped")
            return
        }
        onIntegrationStoppedCallbacks.add(callback)
    }
    private fun executeCallbackSafely(callback: OnIntegrationStoppedCallback) = runCatching {
        callback.onIntegrationStopped()
    }.logOnException()

    /**
     * Removes `callback` from list of callbacks that are required to be notified about SDK de-integration.
     */
    fun unregisterForIntegrationStopped(callback: OnIntegrationStoppedCallback) {
        onIntegrationStoppedCallbacks.remove(callback)
    }

    /**
     * Removes all SDK local data, caches, etc...
     */
    fun clearLocalCustomerData() {
        val context = SendsayContextProvider.applicationContext
        if (context == null) {
            Logger.e(this, "Unable to clear all SDK data because application context is null")
        }
        context?.let {
            clearTrackedEvents(it)
            clearAppInbox(it)
            clearSegments(it)
            clearSession(it)
            clearPushToken(it)
            clearCampaignData(it)
            clearInstallEvent(it)
            clearConfiguration(it)
            clearContentBlocks(it)
            clearInAppMessages(it)
            clearResourcesCaches(it)
            clearCookieRepository(it)
            clearCustomerIdsRepository(it)
            clearTelemetry(it)
        }
    }

    private fun clearTelemetry(context: Context) {
        val application = context.applicationContext as Application
        FileTelemetryStorage(application).clear()
        TelemetryManager.getSharedPreferences(application).edit().clear().apply()
    }

    private fun clearCustomerIdsRepository(context: Context) {
        val prefs = SendsayPreferencesImpl(context)
        val cookieRepo = UniqueIdentifierRepositoryImpl(prefs)
        CustomerIdsRepositoryImpl(SendsayGson.instance, cookieRepo, prefs).clear()
    }

    private fun clearCookieRepository(context: Context) {
        val prefs = SendsayPreferencesImpl(context)
        UniqueIdentifierRepositoryImpl(prefs).clear()
    }

    private fun clearResourcesCaches(context: Context) {
        DrawableCacheImpl(context).clear()
        FontCacheImpl(context).clear()
    }

    private fun clearInAppMessages(context: Context) {
        val prefs = SendsayPreferencesImpl(context)
        InAppMessagesCacheImpl(context, SendsayGson.instance).clear()
        InAppMessageDisplayStateRepositoryImpl(prefs, SendsayGson.instance).clear()
    }

    private fun clearContentBlocks(context: Context) {
        val prefs = SendsayPreferencesImpl(context)
        InAppContentBlockDisplayStateRepositoryImpl(prefs).clear()
        HtmlNormalizedCacheImpl(context, prefs).clearAll()
    }

    private fun clearConfiguration(context: Context) {
        SendsayConfigRepository.clear(context)
    }

    private fun clearInstallEvent(context: Context) {
        DeviceInitiatedRepositoryImpl(SendsayPreferencesImpl(context)).set(false)
    }

    private fun clearCampaignData(context: Context) {
        val prefs = SendsayPreferencesImpl(context)
        CampaignRepositoryImpl(SendsayGson.instance, prefs).clear()
    }

    private fun clearPushToken(context: Context) {
        PushTokenRepositoryProvider.get(context).clear()
    }

    private fun clearSession(context: Context) {
        SendsayPreferencesImpl(context).apply {
            this.remove(SessionManagerImpl.PREF_SESSION_START)
            this.remove(SessionManagerImpl.PREF_SESSION_END)
        }
    }

    private fun clearSegments(context: Context) {
        Sendsay.segmentationDataCallbacks.clear()
        SegmentsCacheImpl(context, SendsayGson.instance).clear()
    }

    private fun clearAppInbox(context: Context) {
        AppInboxCacheImpl(context, SendsayGson.instance).clear()
    }

    private fun clearTrackedEvents(context: Context) {
        EventRepositoryImpl(context).onIntegrationStopped()
    }
}

interface OnIntegrationStoppedCallback {
    fun onIntegrationStopped()
}
