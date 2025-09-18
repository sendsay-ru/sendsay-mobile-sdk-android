package ru.sendsay.sdk.models

interface PushNotificationDelegate {
    fun onSilentPushNotificationReceived(notificationData: Map<String, Any>)
    fun onPushNotificationReceived(notificationData: Map<String, Any>)
    fun onPushNotificationOpened(
        action: SendsayNotificationActionType,
        url: String?,
        notificationData: Map<String, Any>
    )
}
