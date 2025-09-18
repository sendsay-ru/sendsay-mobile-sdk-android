package ru.sendsay.sdk.manager

import ru.sendsay.sdk.models.Event
import ru.sendsay.sdk.models.EventType
import ru.sendsay.sdk.models.ExportedEvent
import ru.sendsay.sdk.models.InAppMessage
import ru.sendsay.sdk.services.OnIntegrationStoppedCallback
import java.util.Date

internal interface InAppMessageManager : OnIntegrationStoppedCallback {
    fun reload(
        callback: ((Result<Unit>) -> Unit)? = null
    )

    fun findMessagesByFilter(
        eventType: String,
        properties: Map<String, Any?>,
        timestamp: Double?
    ): List<InAppMessage>

    fun sessionStarted(sessionStartDate: Date)
    fun onEventCreated(event: Event, type: EventType)
    fun onEventUploaded(event: ExportedEvent)
    fun clear()
    override fun onIntegrationStopped()
}

internal interface InAppMessageTrackingDelegate {
    fun track(
        message: InAppMessage,
        action: String,
        interaction: Boolean,
        trackingAllowed: Boolean,
        text: String? = null,
        link: String? = null,
        error: String? = null
    )
}
