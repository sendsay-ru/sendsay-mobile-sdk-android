package ru.sendsay.example.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import ru.sendsay.example.utils.serializable
import ru.sendsay.sdk.SendsayExtras
import ru.sendsay.sdk.models.NotificationAction
import ru.sendsay.sdk.models.NotificationData

class MyReceiver : BroadcastReceiver() {

    // React on push action
    override fun onReceive(context: Context, intent: Intent) {
        // Extract push data
        val data = intent.getParcelableExtra<NotificationData>(
            SendsayExtras.EXTRA_DATA,
            NotificationData::class.java
        )
        val actionInfo = intent.serializable<NotificationAction>(
            SendsayExtras.EXTRA_ACTION_INFO,
        )
        val customData = intent.serializable<HashMap<String, String>>(
            SendsayExtras.EXTRA_CUSTOM_DATA,
        )

        // Process push data as you need
        print(customData)
    }
}
