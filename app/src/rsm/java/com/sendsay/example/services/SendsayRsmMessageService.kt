package com.sendsay.example.services

import android.app.NotificationManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.gson.reflect.TypeToken
import com.sendsay.sdk.Sendsay
import com.sendsay.sdk.util.SendsayGson
import ru.rustore.sdk.pushclient.messaging.exception.RuStorePushClientException
import ru.rustore.sdk.pushclient.messaging.model.RemoteMessage
import ru.rustore.sdk.pushclient.messaging.service.RuStoreMessagingService


class SendsayRsmMessageService : RuStoreMessagingService() {
    val gson = SendsayGson.instance

    fun <T> T.serializeToSendsayMap(): Map<String, Any> {
        val json = gson.toJson(this)
        val map = gson.fromJson<MutableMap<String, Any>>(json, object : TypeToken<Map<String, Any>>() {}.type).also {
            it["source"] = "xnpe_platform"
        }
        return map
    }

    private val notificationManager by lazy {
        getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    }



    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("RuStoreToken", "onNewToken token = $token")
        // replace with handleNewRsmToken once backend is ready
        Sendsay.handleNewRsmToken(applicationContext, token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        /** backend or gorush logic */
//        Sendsay.handleRemoteMessage(applicationContext, message.data, notificationManager)
        /**  tests from web console and local */
        Sendsay.handleRemoteMessage(
            applicationContext,
            message.notification?.serializeToSendsayMap() as? Map<String, String>,
            notificationManager
        )
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onError(errors: List<RuStorePushClientException>) {
        // Получение ошибок, которые могут возникнуть во время работы SDK
        errors.forEach { error -> error.printStackTrace() }
    }

    // TODO: реализовать позже с бэком
    override fun onDeletedMessages() {
        // Метод вызывается, если один или несколько push-уведомлений не доставлены на устройство.
        // Например, если время жизни уведомления истекло до момента доставки.
        // При вызове этого метода рекомендуется синхронизироваться со своим сервером, чтобы не пропустить данные.
    }
}