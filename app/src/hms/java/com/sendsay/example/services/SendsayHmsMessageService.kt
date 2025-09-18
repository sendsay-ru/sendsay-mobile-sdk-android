package ru.sendsay.example.services

import android.app.NotificationManager
import android.content.Context
import ru.sendsay.sdk.Sendsay
import com.huawei.hms.push.HmsMessageService
import com.huawei.hms.push.RemoteMessage

class SendsayHmsMessageService : HmsMessageService() {

    private val notificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Sendsay.handleRemoteMessage(applicationContext, message.dataOfMap, notificationManager)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // replace with handleNewHmsToken once backend is ready
        Sendsay.handleNewHmsToken(applicationContext, token)
    }
}
