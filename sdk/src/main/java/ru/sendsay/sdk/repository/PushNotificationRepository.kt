package ru.sendsay.sdk.repository

import ru.sendsay.sdk.models.PushOpenedData

internal interface PushNotificationRepository {
    fun getExtraData(): Map<String, Any>?
    fun setExtraData(data: Map<String, Any>)
    fun clearExtraData()
    fun appendDeliveredNotification(data: Map<String, String>)
    fun popDeliveredPushData(): List<Map<String, Any>>
    fun popClickedPushData(): List<PushOpenedData>
    fun appendClickedNotification(data: PushOpenedData)
    fun clearAll()
}
