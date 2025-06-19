package com.sendsay.sdk.manager

import android.app.NotificationManager
import com.sendsay.sdk.models.SendsayConfiguration
import com.sendsay.sdk.models.NotificationChannelImportance
import com.sendsay.sdk.models.NotificationPayload
import com.sendsay.sdk.util.TokenType
import com.sendsay.sdk.util.currentTimeSeconds

internal interface FcmManager {
    fun trackToken(
        token: String? = null,
        tokenTrackFrequency: SendsayConfiguration.TokenFrequency?,
        tokenType: TokenType?
    )
    fun handleRemoteMessage(
        messageData: Map<String, String>?,
        manager: NotificationManager,
        showNotification: Boolean = true,
        timestamp: Double = currentTimeSeconds()
    )
    fun showNotification(manager: NotificationManager, payload: NotificationPayload)
    fun findNotificationChannelImportance(): NotificationChannelImportance
}
