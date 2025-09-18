package ru.sendsay.sdk.models

data class PushOpenedData(
    val actionType: SendsayNotificationActionType,
    val actionUrl: String?,
    val extraData: Map<String, String>
)
