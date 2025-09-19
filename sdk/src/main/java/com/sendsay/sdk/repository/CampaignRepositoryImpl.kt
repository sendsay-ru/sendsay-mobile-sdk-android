package com.sendsay.sdk.repository

import com.sendsay.sdk.models.CampaignData
import com.sendsay.sdk.preferences.SendsayPreferences
import com.sendsay.sdk.util.currentTimeSeconds
import com.google.gson.Gson
import com.sendsay.sdk.Sendsay
import com.sendsay.sdk.util.Logger
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
