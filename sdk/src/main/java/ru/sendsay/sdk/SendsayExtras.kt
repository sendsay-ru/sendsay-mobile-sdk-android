package ru.sendsay.sdk

class SendsayExtras {
    companion object {
        const val ACTION_CLICKED = "ru.sendsay.sdk.action.PUSH_CLICKED"
        const val ACTION_DEEPLINK_CLICKED = "ru.sendsay.sdk.action.PUSH_DEEPLINK_CLICKED"
        const val ACTION_URL_CLICKED = "ru.sendsay.sdk.action.PUSH_URL_CLICKED"
        const val EXTRA_NOTIFICATION_ID = "NotificationId"
        const val EXTRA_DATA = "NotificationData"
        const val EXTRA_CUSTOM_DATA = "NotificationCustomData"
        const val EXTRA_ACTION_INFO = "notification_action"
        const val EXTRA_DELIVERED_TIMESTAMP = "NotificationDeliveredTimestamp"
    }
}
