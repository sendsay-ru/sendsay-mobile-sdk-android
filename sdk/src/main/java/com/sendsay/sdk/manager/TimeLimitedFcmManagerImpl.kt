package com.sendsay.sdk.manager

import android.content.Context
import android.graphics.Bitmap
import com.sendsay.sdk.Sendsay
import com.sendsay.sdk.exceptions.InvalidConfigurationException
import com.sendsay.sdk.models.SendsayConfiguration
import com.sendsay.sdk.network.SendsayServiceImpl
import com.sendsay.sdk.network.NetworkHandlerImpl
import com.sendsay.sdk.preferences.SendsayPreferencesImpl
import com.sendsay.sdk.repository.CampaignRepositoryImpl
import com.sendsay.sdk.repository.CustomerIdsRepositoryImpl
import com.sendsay.sdk.repository.PushNotificationRepositoryImpl
import com.sendsay.sdk.repository.PushTokenRepository
import com.sendsay.sdk.repository.PushTokenRepositoryProvider
import com.sendsay.sdk.repository.UniqueIdentifierRepositoryImpl
import com.sendsay.sdk.services.SendsayProjectFactory
import com.sendsay.sdk.services.inappcontentblock.InAppContentBlockTrackingDelegateImpl
import com.sendsay.sdk.util.SendsayGson
import com.sendsay.sdk.util.Logger
import com.sendsay.sdk.util.runWithTimeout

/**
 * Purpose of TimeLimitedFcmManager is to mirror a implementation of FcmManagerImpl but without SDK usage.
 * TimeLimitedFcmManager is used in case of SDK API usage, when SDK is not initialized yet and we still need to handle
 * FCM data (notifications, push tokens)
 * !!! Keep behavior in sync with FcmManagerImpl
 * !!! Dont use SendsaySDK API nor internal parts here
 * !!! All implementations must be limited by total time (max 10s) due to Android OS limitations and usage
 */
internal class TimeLimitedFcmManagerImpl(
    context: Context,
    configuration: SendsayConfiguration,
    eventManager: EventManager,
    pushTokenRepository: PushTokenRepository,
    trackingConsentManager: TrackingConsentManager
) : FcmManagerImpl(
    context,
    configuration,
    eventManager,
    pushTokenRepository,
    trackingConsentManager
) {

    companion object {

        private val NOTIF_BITMAP_DOWNLOAD_TIMELIMIT: Long = 5000
        private val FLUSH_TIMELIMIT: Long = 5000

        /**
         * Creates an instance of TimeLimitedFcmManager that is intependent from SDK initialization process.
         */
        fun createSdklessInstance(context: Context, configuration: SendsayConfiguration): TimeLimitedFcmManagerImpl {
            val preferences = SendsayPreferencesImpl(context)
            val eventRepository = TemporaryEventRepositoryImpl(context)
            val uniqueIdentifierRepository = UniqueIdentifierRepositoryImpl(preferences)
            val customerIdsRepository = CustomerIdsRepositoryImpl(
                SendsayGson.instance, uniqueIdentifierRepository, preferences
            )
            val networkManager = NetworkHandlerImpl(configuration)
            val sendsayService = SendsayServiceImpl(SendsayGson.instance, networkManager)
            val connectionManager = ConnectionManagerImpl(context)
            val flushManager = TimeLimitedFlushManagerImpl(
                configuration,
                eventRepository,
                sendsayService,
                connectionManager,
                {
                    // no action for identifyCustomer - SDK is not initialized
                },
                FLUSH_TIMELIMIT
            )
            val projectFactory = try {
                SendsayProjectFactory(context, configuration)
            } catch (e: InvalidConfigurationException) {
                if (configuration.advancedAuthEnabled) {
                    Logger.w(this, "Turning off advanced auth for notification data tracking")
                    configuration.advancedAuthEnabled = false
                }
                SendsayProjectFactory(context, configuration)
            }
            val eventManager = EventManagerImpl(
                configuration, eventRepository, customerIdsRepository, flushManager, projectFactory,
                onEventCreated = { event, type ->
                    // no action for any event - SDK is not initialized
                }
            )
            val pushTokenRepository = PushTokenRepositoryProvider.get(context)
            val pushNotificationRepository = PushNotificationRepositoryImpl(preferences)
            val campaignRepository = CampaignRepositoryImpl(SendsayGson.instance, preferences)
            val inappMessageTrackingDelegate = EventManagerInAppMessageTrackingDelegate(
                context, eventManager
            )
            val inAppContentBlockTrackingDelegate = InAppContentBlockTrackingDelegateImpl(
                context, eventManager
            )
            val trackingConsentManager = TrackingConsentManagerImpl(
                eventManager, campaignRepository, inappMessageTrackingDelegate, inAppContentBlockTrackingDelegate
            )
            return TimeLimitedFcmManagerImpl(
                context, configuration, eventManager, pushTokenRepository, trackingConsentManager
            )
        }
    }

    override fun onSelfCheckReceived() {
        // self check has no meaning while usage of this manager
        // but there is chance that SDK may be initialized meanwhile
        if (Sendsay.isInitialized) {
            super.onSelfCheckReceived()
        } else {
            Logger.w(this, "Self-check notification has been delivered but not handled")
        }
    }

    /**
     * Tries to download Bitmap for notification. Image may be too large or slow network.
     * If Image cannot be download in limited time, no image is returned.
     */
    override fun getBitmapFromUrl(url: String): Bitmap? {
        return runWithTimeout(NOTIF_BITMAP_DOWNLOAD_TIMELIMIT, {
            super.getBitmapFromUrl(url)
        }, {
            Logger.w(this, "Bitmap download takes too long")
            null
        })
    }
}
