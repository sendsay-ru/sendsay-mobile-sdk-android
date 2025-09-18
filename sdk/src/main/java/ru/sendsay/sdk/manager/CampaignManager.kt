package ru.sendsay.sdk.manager

import ru.sendsay.sdk.models.CampaignData

internal interface CampaignManager {
    fun trackCampaignClick(campaignData: CampaignData): Boolean
}
