package ru.sendsay.example.services

import android.app.NotificationManager
import android.content.Context
import ru.sendsay.sdk.Sendsay
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class SendsayFirebaseMessageService : FirebaseMessagingService() {

    private val notificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Sendsay.handleRemoteMessage(applicationContext, message.data, notificationManager)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Sendsay.handleNewToken(applicationContext, token)
    }
}
