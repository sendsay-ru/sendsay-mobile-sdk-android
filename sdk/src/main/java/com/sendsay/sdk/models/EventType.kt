package com.sendsay.sdk.models

import com.sendsay.sdk.util.Logger

enum class EventType {
    // Install event is fired only once when the app is first installed.
    INSTALL,

    // Session start event used to mark the start of a session, typically when an app comes to foreground.
    SESSION_START,

    // Session end event used to mark the end of a session, typically when an app goes to background.
    SESSION_END,

    // Custom event tracking, used to report any custom events that you want.
    TRACK_EVENT,

    // Tracking of customers is used to identify a current customer by some identifier.
    TRACK_CUSTOMER,

    // Virtual and hard payments can be tracked to better measure conversions for example.
    PAYMENT,

    // Event used for registering the push notifications token of the device with Sendsay.
    PUSH_TOKEN,

    // For tracking that push notification has been delivered
    PUSH_DELIVERED,

    // For tracking that a push notification has been opened.
    PUSH_OPENED,

    // For tracking that a campaign button has been clicked.
    CAMPAIGN_CLICK,

    // For tracking in-app message related events.
    BANNER,

    // For tracking app inbox message opened
    APP_INBOX_OPENED,

    // For tracking app inbox action clicked
    APP_INBOX_CLICKED;

    companion object {
        fun valueOfOrNull(source: String): EventType? {
            return try {
                valueOf(source)
            } catch (e: Exception) {
                Logger.e(this, "Unknown EventType name '$source'")
                null
            }
        }
    }
}
