package ru.sendsay.sdk.repository

import ru.sendsay.sdk.models.CampaignData
import ru.sendsay.sdk.preferences.SendsayPreferences
import ru.sendsay.sdk.util.currentTimeSeconds
import com.google.gson.Gson
import ru.sendsay.sdk.Sendsay
import ru.sendsay.sdk.util.Logger
import kotlin.math.abs

internal class CampaignRepositoryImpl(
    private val gson: Gson,
    private val preferences: SendsayPreferences
) : CampaignRepository {

    private val key = "SendsayCampaign"

    override fun set(campaignData: CampaignData) {
        if (Sendsay.isStopped) {
            Logger.e(this, "Campaign event not stored, SDK is stopping")
            return
        }
        val json = gson.toJson(campaignData)
        preferences.setString(key, json)
    }

    override fun clear(): Boolean {
        return preferences.remove(key)
    }

    override fun get(): CampaignData? {
        if (Sendsay.isStopped) {
            Logger.e(this, "Campaign event not loaded, SDK is stopping")
            return null
        }
        val data = gson.fromJson(preferences.getString(key, ""), CampaignData::class.java)
        if (data != null && abs(currentTimeSeconds() - data.createdAt) > Sendsay.campaignTTL) {
            clear()
            return null
        }
        return data
    }

    override fun onIntegrationStopped() {
        clear()
    }
}
