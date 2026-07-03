package com.sendsay.example.services

import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import com.sendsay.sdk.Sendsay
import com.huawei.hms.push.HmsMessageService
import com.huawei.hms.push.RemoteMessage
import com.sendsay.example.BuildConfig
import com.sendsay.example.LogCollector
import java.lang.Exception

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

    override fun onTokenError(ex: Exception?) {
        super.onTokenError(ex)
        LogCollector.error(
            BuildConfig.FLAVOR,
            ex?.stackTraceToString() ?: "Unknown Token error"
        )
    }
}
