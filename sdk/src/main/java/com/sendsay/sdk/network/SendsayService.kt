package com.sendsay.sdk.network
import com.sendsay.sdk.models.CustomerAttributesRequest
import com.sendsay.sdk.models.CustomerIds
import com.sendsay.sdk.models.Event
import com.sendsay.sdk.models.SendsayProject
import com.sendsay.sdk.util.TokenType
import okhttp3.Call

internal interface SendsayService {
    fun postEvent(sendsayProject: SendsayProject, event: Event): Call
    fun postCustomer(sendsayProject: SendsayProject, event: Event): Call
    fun postFetchAttributes(sendsayProject: SendsayProject, attributesRequest: CustomerAttributesRequest): Call
    fun postFetchConsents(sendsayProject: SendsayProject): Call
    fun postCampaignClick(sendsayProject: SendsayProject, event: Event): Call
    fun postFetchInAppMessages(sendsayProject: SendsayProject, customerIds: CustomerIds): Call
    fun postFetchAppInbox(sendsayProject: SendsayProject, customerIds: CustomerIds, syncToken: String?): Call
    fun postReadFlagAppInbox(
        sendsayProject: SendsayProject,
        customerIds: CustomerIds,
        messageIds: List<String>,
        syncToken: String
    ): Call
    fun postPushSelfCheck(
        sendsayProject: SendsayProject,
        customerIds: CustomerIds,
        pushToken: String,
        tokenType: TokenType
    ): Call
    fun fetchInitConfig(sendsayProject: SendsayProject): Call
    fun fetchStaticInAppContentBlocks(sendsayProject: SendsayProject): Call
    fun fetchPersonalizedInAppContentBlocks(
        sendsayProject: SendsayProject,
        customerIds: CustomerIds,
        contentBlockIds: List<String>
    ): Call

    fun fetchSegments(
        sendsayProject: SendsayProject,
        engagementCookieId: String
    ): Call

    fun linkIdsToCookie(
        sendsayProject: SendsayProject,
        engagementCookieId: String,
        externalIds: HashMap<String, String?>
    ): Call
}
