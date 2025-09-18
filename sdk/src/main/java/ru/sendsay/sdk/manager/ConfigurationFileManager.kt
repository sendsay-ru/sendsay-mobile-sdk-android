package ru.sendsay.sdk.manager

import android.content.Context
import ru.sendsay.sdk.models.SendsayConfiguration
import ru.sendsay.sdk.util.Logger
import com.google.gson.Gson

internal object ConfigurationFileManager {
    fun getConfigurationFromDefaultFile(context: Context): SendsayConfiguration? {
        val data = readContentFromDefaultFile(context)

        if (data.isNullOrEmpty()) {
            Logger.e(this, "No data found on Configuration file")
            return null
        }

        return Gson().fromJson(data, SendsayConfiguration::class.java)
    }

    private fun readContentFromDefaultFile(context: Context): String? {
        return try {
            val configurationFileName = "sendsay_configuration.json"
            val inputStream =
                javaClass.classLoader?.getResourceAsStream(configurationFileName)
                    ?: context.assets.open(configurationFileName)
            val buffer = inputStream.bufferedReader()
            val inputString = buffer.use { it.readText() }
            Logger.d(this, "Configuration file successfully loaded")
            inputString
        } catch (e: Exception) {
            Logger.e(this, "Could not load configuration file ")
            null
        }
    }
}
