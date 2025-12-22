package com.sendsay.sdk.repository

import android.icu.util.Calendar
import com.google.gson.Gson
import com.sendsay.sdk.models.PushOpenedData
import com.sendsay.sdk.preferences.SendsayPreferences
import com.sendsay.sdk.util.Logger
import com.sendsay.sdk.util.fromJson
import kotlin.time.Duration.Companion.minutes
import kotlin.time.DurationUnit

internal class PushNotificationRepositoryImpl(
    private val preferences: SendsayPreferences
) : PushNotificationRepository {
    companion object {
        //    private val ISSUE_LETTER_EXPIRE_DURATION: Long = 48.hours.toLong(DurationUnit.MILLISECONDS)
        val ISSUE_LETTER_EXPIRE_DURATION: Long = 15.minutes.toLong(DurationUnit.MILLISECONDS)

        val KEY_ISSUE_LETTER_DATETIME_DATA = "SendsayIssueLetterDateTimeData"
        val KEY_ISSUE = "sendsay_issue_id"
        val KEY_LETTER = "sendsay_letter_id"
    }

    private val KEY_EXTRA_DATA = "SendsayPushNotificationExtraData"
    private val KEY_DELIVERED_DATA = "SendsayDeliveredPushNotificationData"
    private val KEY_CLICKED_DATA = "SendsayClickedPushNotificationData"

    override fun getExtraData(): Map<String, Any>? {
        val dataString = preferences.getString(KEY_EXTRA_DATA, "")
        if (dataString.isEmpty()) {
            return null
        }
        val mapData = Gson().fromJson<HashMap<String, Any>>(dataString)
        checkIssueAndLetterOnExpire(mapData)
        return mapData
    }

    private fun checkIssueAndLetterOnExpire(data: Map<String, Any>) {
        val now = Calendar.getInstance().timeInMillis
        val lastIssueLetterDateTime: Long =
            (data.entries.firstOrNull { it.key == KEY_ISSUE_LETTER_DATETIME_DATA }
                ?.value as? Long ?: now)
        if ((now - lastIssueLetterDateTime)
            >= ISSUE_LETTER_EXPIRE_DURATION
        ) {
            val mutableMap = data.toMutableMap()
            mutableMap.remove(KEY_ISSUE_LETTER_DATETIME_DATA)
            mutableMap.remove(KEY_ISSUE)
            mutableMap.remove(KEY_LETTER)
            setExtraData(mutableMap)
        }
    }

    override fun setExtraData(data: Map<String, Any>) {
        setLastDateTimeOnIssueAndLetter(data)
        val dataString = Gson().toJson(data)
        preferences.setString(KEY_EXTRA_DATA, dataString)
    }

    private fun setLastDateTimeOnIssueAndLetter(data: Map<String, Any>) {
        if (data.entries.any { it.key == KEY_ISSUE || it.key == KEY_LETTER }) {
            val currentDateTime = Calendar.getInstance().timeInMillis
            data.entries.plus(KEY_ISSUE_LETTER_DATETIME_DATA to currentDateTime)
        }
    }

    override fun appendDeliveredNotification(data: Map<String, String>) {
        val storedDeliveredNotifications = getDeliveredNotifications()
        val newDeliveredNotifications = storedDeliveredNotifications + data
        val dataString = Gson().toJson(newDeliveredNotifications)
        preferences.setString(KEY_DELIVERED_DATA, dataString)
    }

    override fun popDeliveredPushData(): List<Map<String, Any>> {
        val storedDeliveredNotifications = getDeliveredNotifications()
        clearDeliveredData()
        return storedDeliveredNotifications
    }

    private fun clearDeliveredData() {
        preferences.remove(KEY_DELIVERED_DATA)
    }

    private fun getDeliveredNotifications(): List<Map<String, String>> = runCatching {
        val dataString = preferences.getString(KEY_DELIVERED_DATA, "")
        if (dataString.isEmpty()) {
            return emptyList()
        }
        return Gson().fromJson<List<Map<String, String>>>(dataString)
    }.getOrElse {
        Logger.e(this, "Unable to read delivered notifications stored locally", it)
        return@getOrElse emptyList()
    }

    override fun appendClickedNotification(data: PushOpenedData) {
        val storedClickedNotifications = getClickedNotifications()
        val newClickedNotifications = storedClickedNotifications + data
        val dataString = Gson().toJson(newClickedNotifications)
        preferences.setString(KEY_CLICKED_DATA, dataString)
    }

    override fun popClickedPushData(): List<PushOpenedData> {
        val storedClickedNotifications = getClickedNotifications()
        clearClickedData()
        return storedClickedNotifications
    }

    private fun clearClickedData() {
        preferences.remove(KEY_CLICKED_DATA)
    }

    private fun getClickedNotifications(): List<PushOpenedData> = runCatching {
        val dataString = preferences.getString(KEY_CLICKED_DATA, "")
        if (dataString.isEmpty()) {
            return emptyList()
        }
        return Gson().fromJson<List<PushOpenedData>>(dataString)
    }.getOrElse {
        Logger.e(this, "Unable to read clicked notifications stored locally", it)
        return@getOrElse emptyList()
    }

    override fun clearExtraData() {
        preferences.remove(KEY_EXTRA_DATA)
    }

    override fun clearAll() {
        clearExtraData()
        clearDeliveredData()
        clearClickedData()
    }
}
