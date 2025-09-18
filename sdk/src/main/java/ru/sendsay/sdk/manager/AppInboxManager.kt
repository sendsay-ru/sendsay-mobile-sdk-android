package ru.sendsay.sdk.manager

import ru.sendsay.sdk.models.Event
import ru.sendsay.sdk.models.EventType
import ru.sendsay.sdk.models.MessageItem
import ru.sendsay.sdk.services.OnIntegrationStoppedCallback

interface AppInboxManager : OnIntegrationStoppedCallback {
    fun fetchAppInbox(callback: (List<MessageItem>?) -> Unit)
    fun fetchAppInboxItem(messageId: String, callback: (MessageItem?) -> Unit)
    fun reload()
    fun onEventCreated(event: Event, type: EventType)
    fun markMessageAsRead(messageId: MessageItem, callback: ((Boolean) -> Unit)?)
    override fun onIntegrationStopped()
}
