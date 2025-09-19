package com.sendsay.sdk.manager

import com.sendsay.sdk.models.Event
import com.sendsay.sdk.models.EventType
import com.sendsay.sdk.models.MessageItem
import com.sendsay.sdk.services.OnIntegrationStoppedCallback

interface AppInboxManager : OnIntegrationStoppedCallback {
    fun fetchAppInbox(callback: (List<MessageItem>?) -> Unit)
    fun fetchAppInboxItem(messageId: String, callback: (MessageItem?) -> Unit)
    fun reload()
    fun onEventCreated(event: Event, type: EventType)
    fun markMessageAsRead(messageId: MessageItem, callback: ((Boolean) -> Unit)?)
    override fun onIntegrationStopped()
}
