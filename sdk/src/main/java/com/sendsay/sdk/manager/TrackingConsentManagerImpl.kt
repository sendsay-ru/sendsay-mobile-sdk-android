package com.sendsay.sdk.manager

import android.content.Context
import com.sendsay.sdk.Sendsay
import com.sendsay.sdk.Sendsay.initGate
import com.sendsay.sdk.exceptions.InvalidConfigurationException
import com.sendsay.sdk.manager.TrackingConsentManager.MODE
import com.sendsay.sdk.manager.TrackingConsentManager.MODE.CONSIDER_CONSENT
import com.sendsay.sdk.models.Constants
import com.sendsay.sdk.models.Constants.EventTypes
import com.sendsay.sdk.models.EventType
import com.sendsay.sdk.models.EventType.PUSH_OPENED
import com.sendsay.sdk.models.EventType.TRACK_EVENT
import com.sendsay.sdk.models.InAppContentBlock
import com.sendsay.sdk.models.InAppMessage
import com.sendsay.sdk.models.MessageItem
import com.sendsay.sdk.models.NotificationAction
import com.sendsay.sdk.models.NotificationChannelImportance
import com.sendsay.sdk.models.NotificationData
import com.sendsay.sdk.models.PropertiesList
import com.sendsay.sdk.models.TrackSSECData
import com.sendsay.sdk.models.TrackingSSECType
import com.sendsay.sdk.network.NetworkHandlerImpl
import com.sendsay.sdk.network.SendsayServiceImpl
import com.sendsay.sdk.preferences.SendsayPreferencesImpl
import com.sendsay.sdk.repository.CampaignRepository
import com.sendsay.sdk.repository.CampaignRepositoryImpl
import com.sendsay.sdk.repository.CustomerIdsRepositoryImpl
import com.sendsay.sdk.repository.SendsayConfigRepository
import com.sendsay.sdk.repository.UniqueIdentifierRepositoryImpl
import com.sendsay.sdk.services.SendsayProjectFactory
import com.sendsay.sdk.services.inappcontentblock.InAppContentBlockTrackingDelegateImpl
import com.sendsay.sdk.util.GdprTracking
import com.sendsay.sdk.util.Logger
import com.sendsay.sdk.util.SendsayGson
import com.sendsay.sdk.util.currentTimeSeconds

internal class TrackingConsentManagerImpl(
    private val eventManager: EventManager,
    private val campaignRepository: CampaignRepository,
    private val inappMessageTrackingDelegate: InAppMessageTrackingDelegate,
    private val inAppContentBlockTrackingDelegate: InAppContentBlockTrackingDelegate
) : TrackingConsentManager {

    override fun trackClickedPush(
        data: NotificationData?,
        actionData: NotificationAction?,
        timestamp: Double?,
        mode: MODE
    ) {
        var trackingAllowed = true
        if (mode == CONSIDER_CONSENT && data?.hasTrackingConsent == false && actionData?.isTrackingForced != true) {
            Logger.e(
                this,
                "Event for clicked notification is not tracked because consent is not given nor forced"
            )
            trackingAllowed = false
        }
        val properties = PropertiesList(
            hashMapOf(
                "status" to "clicked",
                "platform" to "android",
                "url" to (actionData?.url ?: "app"),
                "cta" to (actionData?.actionName ?: "notification")
            )
        )
        if (data != null) {
            // we'll consider the campaign data as just created - for expiration handling
            data.campaignData.createdAt = currentTimeSeconds()
            campaignRepository.set(data.campaignData)
        }
        if (data?.getTrackingData() != null) {
            for (item in data.getTrackingData()) {
                properties[item.key] = item.value
            }
        }
        if (data?.consentCategoryTracking != null) {
            properties["consent_category_tracking"] = data.consentCategoryTracking
        }
        if (actionData?.isTrackingForced == true) {
            properties["tracking_forced"] = true
        }
        eventManager.processTrack(
            eventType = if (data?.hasCustomEventType == true) data.eventType else EventTypes.push,
            properties = properties.properties,
            type = if (data?.hasCustomEventType == true) TRACK_EVENT else PUSH_OPENED,
            timestamp = timestamp,
            trackingAllowed = trackingAllowed
        )
    }

    override fun trackDeliveredPush(
        data: NotificationData?,
        timestamp: Double,
        mode: MODE,
        shownStatus: Constants.PushNotifShownStatus,
        notificationChannelImportance: NotificationChannelImportance
    ) {
        var trackingAllowed = true
        if (mode == CONSIDER_CONSENT && data?.hasTrackingConsent == false) {
            Logger.e(
                this,
                "Event for delivered notification is not tracked because consent is not given"
            )
            trackingAllowed = false
        }
        val properties = PropertiesList(
            hashMapOf(
                "status" to "delivered",
                "platform" to "android",
                "state" to shownStatus.value,
                "notification_importance" to notificationChannelImportance.trackValue
            )
        )
        if (data?.getTrackingData() != null) {
            for (item in data.getTrackingData()) {
                properties[item.key] = item.value
            }
        }
        if (data?.consentCategoryTracking != null) {
            properties["consent_category_tracking"] = data.consentCategoryTracking
        }
        eventManager.processTrack(
            eventType = if (data?.hasCustomEventType == true) data.eventType else Constants.EventTypes.push,
            properties = properties.properties,
            type = if (data?.hasCustomEventType == true) EventType.TRACK_EVENT else EventType.PUSH_DELIVERED,
            timestamp = timestamp,
            trackingAllowed = trackingAllowed
        )
    }

    override fun trackInAppMessageShown(message: InAppMessage, mode: MODE) {
        var trackingAllowed = true
        if (mode == CONSIDER_CONSENT && !message.hasTrackingConsent) {
            Logger.e(
                this,
                "Event for shown inAppMessage is not tracked because consent is not given"
            )
            trackingAllowed = false
        }
        inappMessageTrackingDelegate.track(message, "show", false, trackingAllowed)
    }

    override fun trackInAppMessageClick(
        message: InAppMessage,
        buttonText: String?,
        buttonLink: String?,
        mode: MODE
    ) {
        var trackingAllowed = true
        if (mode == CONSIDER_CONSENT && !message.hasTrackingConsent && !GdprTracking.isTrackForced(
                buttonLink
            )
        ) {
            Logger.e(
                this,
                "Event for clicked inAppMessage is not tracked because consent is not given"
            )
            trackingAllowed = false
        }
        inappMessageTrackingDelegate.track(
            message,
            "click",
            true,
            trackingAllowed,
            buttonText,
            buttonLink
        )
    }

    override fun trackInAppMessageClose(
        message: InAppMessage,
        buttonText: String?,
        userInteraction: Boolean,
        mode: MODE
    ) {
        var trackingAllowed = true
        if (mode == CONSIDER_CONSENT && !message.hasTrackingConsent) {
            Logger.e(
                this,
                "Event for closed inAppMessage is not tracked because consent is not given"
            )
            trackingAllowed = false
        }
        inappMessageTrackingDelegate.track(
            message,
            "close",
            userInteraction,
            trackingAllowed,
            text = buttonText
        )
    }

    override fun trackInAppMessageError(message: InAppMessage, error: String, mode: MODE) {
        var trackingAllowed = true
        if (mode == CONSIDER_CONSENT && !message.hasTrackingConsent) {
            Logger.e(
                this,
                "Event for error of inAppMessage showing is not tracked because consent is not given"
            )
            trackingAllowed = false
        }
        inappMessageTrackingDelegate.track(message, "error", false, trackingAllowed, error = error)
    }

    override fun trackAppInboxOpened(item: MessageItem, mode: MODE) {
        val customerIds = item.customerIds
        if (customerIds.isEmpty()) {
            Logger.e(this, "AppInbox message contains empty customerIds")
            return
        }
        var trackingAllowed = true
        if (mode == CONSIDER_CONSENT && item.content != null && !item.content!!.hasTrackingConsent) {
            Logger.e(this, "Event for AppInbox showing is not tracked because consent is not given")
            trackingAllowed = false
        }
        val properties = PropertiesList(
            hashMapOf("status" to "opened", "platform" to "android")
        )
        item.content?.trackingData?.let { trackingData ->
            for (each in trackingData) {
                properties[each.key] = each.value
            }
        }
        item.content?.consentCategoryTracking?.let { consentCategoryTracking ->
            properties["consent_category_tracking"] = consentCategoryTracking
        }
        properties["action_type"] = "app inbox"
        properties["platform"] = "android"
        eventManager.processTrack(
            eventType = Constants.EventTypes.push,
            properties = properties.properties,
            type = EventType.APP_INBOX_OPENED,
            timestamp = currentTimeSeconds(),
            trackingAllowed = trackingAllowed,
            customerIds = customerIds
        )
    }

    override fun trackAppInboxClicked(
        message: MessageItem,
        buttonText: String?,
        buttonLink: String?,
        mode: MODE
    ) {
        val customerIds = message.customerIds
        if (customerIds.isEmpty()) {
            Logger.e(this, "AppInbox message contains empty customerIds")
            return
        }
        var trackingAllowed = true
        if (mode == CONSIDER_CONSENT && !message.hasTrackingConsent && !GdprTracking.isTrackForced(
                buttonLink
            )
        ) {
            Logger.e(this, "Event for clicked AppInbox is not tracked because consent is not given")
            trackingAllowed = false
        }
        val properties = PropertiesList(
            hashMapOf(
                "status" to "clicked",
                "platform" to "android",
                "url" to (buttonLink ?: "app"),
                "cta" to (buttonText ?: "inbox")
            )
        )
        message.content?.trackingData?.let { trackingData ->
            for (item in trackingData) {
                properties[item.key] = item.value
            }
        }
        message.content?.consentCategoryTracking?.let { consentCategoryTracking ->
            properties["consent_category_tracking"] = consentCategoryTracking
        }
        if (GdprTracking.isTrackForced(buttonLink)) {
            properties["tracking_forced"] = true
        }
        properties["action_type"] = "app inbox"
        properties["platform"] = "android"
        eventManager.processTrack(
            eventType = Constants.EventTypes.push,
            properties = properties.properties,
            type = EventType.APP_INBOX_CLICKED,
            timestamp = currentTimeSeconds(),
            trackingAllowed = trackingAllowed,
            customerIds = customerIds
        )
    }

    override fun trackInAppContentBlockShown(
        placeholderId: String,
        contentBlock: InAppContentBlock,
        mode: MODE
    ) {
        var trackingAllowed = true
        if (mode == CONSIDER_CONSENT && !contentBlock.hasTrackingConsent) {
            Logger.e(
                this,
                "Event for shown InApp Content Block is not tracked because consent is not given"
            )
            trackingAllowed = false
        }
        inAppContentBlockTrackingDelegate.track(
            placeholderId,
            contentBlock,
            "show",
            false,
            trackingAllowed
        )
    }

    override fun trackInAppContentBlockClick(
        placeholderId: String,
        contentBlock: InAppContentBlock,
        buttonText: String?,
        buttonLink: String?,
        mode: MODE
    ) {
        var trackingAllowed = true
        if (mode == CONSIDER_CONSENT && !contentBlock.hasTrackingConsent && !GdprTracking.isTrackForced(
                buttonLink
            )
        ) {
            Logger.e(
                this,
                "Event for clicked InApp Content Block is not tracked because consent is not given"
            )
            trackingAllowed = false
        }
        inAppContentBlockTrackingDelegate.track(
            placeholderId,
            contentBlock,
            "click",
            true,
            trackingAllowed,
            buttonText,
            buttonLink
        )
    }

    override fun trackInAppContentBlockClose(
        placeholderId: String,
        contentBlock: InAppContentBlock,
        mode: MODE
    ) {
        var trackingAllowed = true
        if (mode == CONSIDER_CONSENT && !contentBlock.hasTrackingConsent) {
            Logger.e(
                this,
                "Event for closed InApp Content Block is not tracked because consent is not given"
            )
            trackingAllowed = false
        }
        inAppContentBlockTrackingDelegate.track(
            placeholderId,
            contentBlock,
            "close",
            true,
            trackingAllowed
        )
    }

    override fun trackInAppContentBlockError(
        placeholderId: String,
        contentBlock: InAppContentBlock,
        error: String,
        mode: MODE
    ) {
        var trackingAllowed = true
        if (mode == CONSIDER_CONSENT && !contentBlock.hasTrackingConsent) {
            Logger.e(
                this,
                "Event for error of InApp Content Block showing is not tracked because consent is not given"
            )
            trackingAllowed = false
        }
        inAppContentBlockTrackingDelegate.track(
            placeholderId,
            contentBlock,
            "error",
            false,
            trackingAllowed,
            error = error
        )
    }

    override fun trackSSEC(type: TrackingSSECType, data: TrackSSECData) {
        initGate.waitForInitialize {
            val properties = data.toProperties(type)

            Sendsay.trackEvent(
                properties = properties,
                timestamp = null,
                eventType = type.value
            )
        }
    }

    companion object {
        fun createSdklessInstance(context: Context): TrackingConsentManagerImpl {
            val configuration = SendsayConfigRepository.get(context)
                ?: throw InvalidConfigurationException("No previous Sendsay configuration found")
            val preferences = SendsayPreferencesImpl(context)
            val eventRepository = TemporaryEventRepositoryImpl(context)
            val uniqueIdentifierRepository = UniqueIdentifierRepositoryImpl(preferences)
            val customerIdsRepository = CustomerIdsRepositoryImpl(
                SendsayGson.instance, uniqueIdentifierRepository, preferences
            )
            val networkManager = NetworkHandlerImpl(configuration)
            val sendsayService = SendsayServiceImpl(SendsayGson.instance, networkManager)
            val connectionManager = ConnectionManagerImpl(context)
            val flushManager = FlushManagerImpl(
                configuration,
                eventRepository,
                sendsayService,
                connectionManager
            ) {
                // no action for identifyCustomer - SDK is not initialized
            }
            val projectFactory = try {
                SendsayProjectFactory(context, configuration)
            } catch (e: InvalidConfigurationException) {
                if (configuration.advancedAuthEnabled) {
                    Logger.w(this, "Turning off advanced auth for campaign data tracking")
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
            val campaignRepository = CampaignRepositoryImpl(SendsayGson.instance, preferences)
            val inappMessageTrackingDelegate = EventManagerInAppMessageTrackingDelegate(
                context, eventManager
            )
            val inAppContentBlockTrackingDelegate = InAppContentBlockTrackingDelegateImpl(
                context, eventManager
            )
            return TrackingConsentManagerImpl(
                eventManager = eventManager,
                campaignRepository = campaignRepository,
                inappMessageTrackingDelegate = inappMessageTrackingDelegate,
                inAppContentBlockTrackingDelegate = inAppContentBlockTrackingDelegate
            )
        }
    }
}
