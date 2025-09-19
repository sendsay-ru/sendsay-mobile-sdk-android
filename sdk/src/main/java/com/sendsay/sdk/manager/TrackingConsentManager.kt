package com.sendsay.sdk.manager

import com.sendsay.sdk.models.Constants
import com.sendsay.sdk.models.InAppContentBlock
import com.sendsay.sdk.models.InAppMessage
import com.sendsay.sdk.models.MessageItem
import com.sendsay.sdk.models.NotificationAction
import com.sendsay.sdk.models.NotificationChannelImportance
import com.sendsay.sdk.models.NotificationData
import com.sendsay.sdk.models.TrackSSECData
import com.sendsay.sdk.models.TrackingSSECType

internal interface TrackingConsentManager {
    fun trackClickedPush(data: NotificationData?, actionData: NotificationAction?, timestamp: Double?, mode: MODE)
    fun trackDeliveredPush(
        data: NotificationData?,
        timestamp: Double,
        mode: MODE,
        shownStatus: Constants.PushNotifShownStatus,
        notificationChannelImportance: NotificationChannelImportance
    )
    fun trackInAppMessageShown(message: InAppMessage, mode: MODE)
    fun trackInAppMessageClick(message: InAppMessage, buttonText: String?, buttonLink: String?, mode: MODE)
    fun trackInAppMessageClose(message: InAppMessage, buttonText: String?, userInteraction: Boolean, mode: MODE)
    fun trackInAppMessageError(message: InAppMessage, error: String, mode: MODE)
    fun trackAppInboxOpened(item: MessageItem, mode: MODE)
    fun trackAppInboxClicked(message: MessageItem, buttonText: String?, buttonLink: String?, mode: MODE)
    fun trackInAppContentBlockShown(placeholderId: String, contentBlock: InAppContentBlock, mode: MODE)
    fun trackInAppContentBlockClick(
        placeholderId: String,
        contentBlock: InAppContentBlock,
        buttonText: String?,
        buttonLink: String?,
        mode: MODE
    )
    fun trackInAppContentBlockClose(placeholderId: String, contentBlock: InAppContentBlock, mode: MODE)
    fun trackInAppContentBlockError(placeholderId: String, contentBlock: InAppContentBlock, error: String, mode: MODE)

    fun trackSSEC(type: TrackingSSECType, data: TrackSSECData)

    enum class MODE {
        CONSIDER_CONSENT, IGNORE_CONSENT
    }
}
