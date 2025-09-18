package ru.sendsay.sdk.models

import ru.sendsay.sdk.util.currentTimeSeconds
import com.google.gson.annotations.SerializedName

class Event(
    @SerializedName("event_type")
    var type: String? = null,
    var timestamp: Double? = currentTimeSeconds(),
    var age: Double? = null,
    @SerializedName("customer_ids")
    var customerIds: HashMap<String, String?>? = null,
    @SerializedName("properties")
    var properties: HashMap<String, Any>? = null
)
