package ru.sendsay.sdk.manager

import android.app.NotificationManager
import ru.sendsay.sdk.models.SendsayConfiguration
import ru.sendsay.sdk.models.NotificationChannelImportance
import ru.sendsay.sdk.models.NotificationPayload
import ru.sendsay.sdk.util.TokenType
import ru.sendsay.sdk.util.currentTimeSeconds

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
