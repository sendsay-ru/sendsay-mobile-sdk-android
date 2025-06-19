package com.sendsay.sdk.manager

import com.sendsay.sdk.models.CampaignData

internal interface CampaignManager {
    fun trackCampaignClick(campaignData: CampaignData): Boolean
}
