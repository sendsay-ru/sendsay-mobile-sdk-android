package ru.sendsay.sdk.repository

import ru.sendsay.sdk.models.CampaignData
import ru.sendsay.sdk.services.OnIntegrationStoppedCallback
/**
 * Repository for storing a single CampaignData.
 */
internal interface CampaignRepository : OnIntegrationStoppedCallback {
    /**
     * Returns CampaignData if exists and lives shorter than Sendsay.campaignTTL
     */
    fun get(): CampaignData?

    /**
     * Store (and replace if already exists) a CampaignData.
     */
    fun set(campaignData: CampaignData)

    /**
     * Remove CampaignData from repository if exists. Returns TRUE on success, FALSE on any error.
     */
    fun clear(): Boolean
    override fun onIntegrationStopped()
}
