package ru.sendsay.sdk.repository

import android.content.Context
import ru.sendsay.sdk.models.SendsayConfiguration
import ru.sendsay.sdk.preferences.SendsayPreferencesImpl
import com.google.gson.Gson
import ru.sendsay.sdk.Sendsay
import ru.sendsay.sdk.util.Logger

internal object SendsayConfigRepository {

    private const val PREF_CONFIG = "SendsayConfigurationPref"

    fun set(context: Context, configuration: SendsayConfiguration) {
        if (Sendsay.isStopped) {
            Logger.e(this, "Last known SDK configuration store failed, SDK is stopping")
            return
        }
        val prefs = SendsayPreferencesImpl(context)
        val gson = Gson()
        val jsonConfiguration = gson.toJson(configuration)
        prefs.setString(PREF_CONFIG, jsonConfiguration)
    }

    fun get(context: Context): SendsayConfiguration? {
        if (Sendsay.isStopped) {
            Logger.e(this, "Last known SDK configuration load failed, SDK is stopping")
            return null
        }
        val prefs = SendsayPreferencesImpl(context)
        val gson = Gson()
        val jsonConfig = prefs.getString(PREF_CONFIG, "")
        if (jsonConfig.isEmpty())
            return null

        return try {
            gson.fromJson(jsonConfig, SendsayConfiguration::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun clear(context: Context) {
        SendsayPreferencesImpl(context).remove(PREF_CONFIG)
    }
}
