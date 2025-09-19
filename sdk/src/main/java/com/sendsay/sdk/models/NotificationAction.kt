package com.sendsay.sdk.models

import com.sendsay.sdk.util.GdprTracking
import java.io.Serializable

data class NotificationAction(
    val actionType: String,
    val actionName: String? = null,
    val url: String? = null
) : Serializable {
    companion object {
        const val ACTION_TYPE_BUTTON = "button"
        const val ACTION_TYPE_NOTIFICATION = "notification"
    }
    val isTrackingForced: Boolean
        get() = GdprTracking.isTrackForced(url)
}
