package ru.sendsay.sdk.models.eventfilter

import ru.sendsay.sdk.util.SendsayGson
import ru.sendsay.sdk.util.Logger
import com.google.gson.annotations.SerializedName

data class EventFilterEvent(
    val eventType: String,
    val properties: Map<String, Any?>,
    val timestamp: Double?
)

data class EventFilter(
    @SerializedName("event_type")
    val eventType: String,
    @SerializedName("filter")
    val filter: List<EventPropertyFilter>
) {
    companion object {
        internal fun deserialize(data: String): EventFilter =
            SendsayGson.instance.fromJson(data, EventFilter::class.java)
    }

    fun passes(event: EventFilterEvent): Boolean {
        if (event.eventType != eventType) {
            Logger.v(this, "EventFilter eventType mismatch")
            return false
        }
        return filter.all { it.passes(event) }
    }

    fun serialize(): String = SendsayGson.instance.toJson(this)
}

data class EventPropertyFilter(
    @SerializedName("attribute")
    val attribute: EventFilterAttribute,
    @SerializedName("constraint")
    val constraint: EventFilterConstraint
) {
    companion object {
        fun timestamp(constraint: EventFilterConstraint) =
            EventPropertyFilter(TimestampAttribute(), constraint)
        fun property(name: String, constraint: EventFilterConstraint) =
            EventPropertyFilter(PropertyAttribute(name), constraint)
    }
    fun passes(event: EventFilterEvent): Boolean {
        return constraint.passes(event, attribute)
    }
}
