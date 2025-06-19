package com.sendsay.sdk.manager

import com.sendsay.sdk.Sendsay
import com.sendsay.sdk.models.Event
import com.sendsay.sdk.models.EventType
import com.sendsay.sdk.models.SendsayConfiguration
import com.sendsay.sdk.models.ExportedEvent
import com.sendsay.sdk.models.FlushMode
import com.sendsay.sdk.models.Route
import com.sendsay.sdk.repository.CustomerIdsRepository
import com.sendsay.sdk.repository.EventRepository
import com.sendsay.sdk.services.SendsayProjectFactory
import com.sendsay.sdk.util.Logger
import com.sendsay.sdk.util.ensureOnBackgroundThread

internal open class EventManagerImpl(
    private val configuration: SendsayConfiguration,
    private val eventRepository: EventRepository,
    private val customerIdsRepository: CustomerIdsRepository,
    private val flushManager: FlushManager,
    private val projectFactory: SendsayProjectFactory,
    private val onEventCreated: (Event, EventType) -> Unit
) : EventManager {

    fun addEventToQueue(event: Event, eventType: EventType, trackingAllowed: Boolean) {
        Logger.d(this, "addEventToQueue")

        val route = when (eventType) {
            EventType.TRACK_CUSTOMER -> Route.TRACK_CUSTOMERS
            EventType.PUSH_TOKEN -> Route.TRACK_CUSTOMERS
            EventType.CAMPAIGN_CLICK -> Route.TRACK_CAMPAIGN
            else -> Route.TRACK_EVENTS
        }

        val projects = arrayListOf(projectFactory.mainSendsayProject)
        projects.addAll(configuration.projectRouteMap[eventType] ?: arrayListOf())
        ensureOnBackgroundThread {
            for (project in projects.distinct()) {
                val exportedEvent = ExportedEvent(
                    type = event.type,
                    timestamp = event.timestamp,
                    age = event.age,
                    customerIds = event.customerIds,
                    properties = event.properties,
                    projectId = project.projectToken,
                    route = route,
                    sendsayProject = project,
                    sdkEventType = eventType.name
                )
                if (trackingAllowed) {
                    Logger.d(this, "Added Event To Queue: ${exportedEvent.id}")
                    eventRepository.add(exportedEvent)
                } else {
                    Logger.d(this, "Event has not been added to Queue: ${exportedEvent.id}" +
                        "because real tracking is not allowed")
                }
            }

            // If flush mode is set to immediate, events should be send to Sendsay APP immediatelly
            if (Sendsay.flushMode == FlushMode.IMMEDIATE) {
                flushManager.flushData()
            }
        }
    }

    override fun track(
        eventType: String?,
        timestamp: Double?,
        properties: HashMap<String, Any>,
        type: EventType,
        customerIds: Map<String, String?>?
    ) {
        processTrack(eventType, timestamp, properties, type, true, customerIds)
    }

    override fun processTrack(
        eventType: String?,
        timestamp: Double?,
        properties: HashMap<String, Any>,
        type: EventType,
        trackingAllowed: Boolean,
        customerIds: Map<String, String?>?
    ) {
        if (Sendsay.isStopped) {
            Logger.e(
                this,
                "Event ${type.name}${eventType?.let { "($it)" } ?: ""} has not been tracked, SDK is stopping"
            )
            return
        }
        val trackedProperties: HashMap<String, Any> = hashMapOf()
        if (canUseDefaultProperties(type)) {
            trackedProperties.putAll(configuration.defaultProperties)
        }
        trackedProperties.putAll(properties)
        val customerIdsMap: HashMap<String, String?> = hashMapOf()
        if (customerIds.isNullOrEmpty()) {
            customerIdsMap.putAll(customerIdsRepository.get().toHashMap())
        } else {
            customerIdsMap.putAll(customerIds)
        }
        val event = Event(
            type = eventType,
            timestamp = timestamp,
            customerIds = customerIdsMap,
            properties = trackedProperties
        )
        addEventToQueue(event, type, trackingAllowed)
        notifyEventCreated(event, type)
    }

    internal fun notifyEventCreated(event: Event, type: EventType) {
        onEventCreated(event, type)
    }

    private fun canUseDefaultProperties(type: EventType): Boolean {
        return when (type) {
            EventType.TRACK_CUSTOMER, EventType.PUSH_TOKEN -> configuration.allowDefaultCustomerProperties
            else -> true
        }
    }
}
