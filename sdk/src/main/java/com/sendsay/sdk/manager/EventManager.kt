package com.sendsay.sdk.manager

import com.sendsay.sdk.models.EventType
import com.sendsay.sdk.util.currentTimeSeconds

internal interface EventManager {
    fun track(
        eventType: String? = null,
        timestamp: Double? = currentTimeSeconds(),
        properties: HashMap<String, Any> = hashMapOf(),
        type: EventType,
        customerIds: Map<String, String?>? = null
    )
    fun processTrack(
        eventType: String? = null,
        timestamp: Double? = currentTimeSeconds(),
        properties: HashMap<String, Any> = hashMapOf(),
        type: EventType,
        trackingAllowed: Boolean,
        customerIds: Map<String, String?>? = null
    )
}
