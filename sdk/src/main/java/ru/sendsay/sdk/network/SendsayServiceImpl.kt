package ru.sendsay.sdk.network

import ru.sendsay.sdk.models.ApiEndPoint
import ru.sendsay.sdk.models.CampaignClickEvent
import ru.sendsay.sdk.models.CustomerAttributesRequest
import ru.sendsay.sdk.models.CustomerIds
import ru.sendsay.sdk.models.Event
import ru.sendsay.sdk.models.SendsayProject
import ru.sendsay.sdk.util.TokenType
import com.google.gson.Gson
import okhttp3.Call

internal class SendsayServiceImpl(
    private val gson: Gson,
    private val networkManager: NetworkHandler
) : SendsayService {

    override fun postCampaignClick(sendsayProject: SendsayProject, event: Event): Call {
        return doPost(
            sendsayProject,
            ApiEndPoint.EndPointName.TRACK_CAMPAIGN,
            CampaignClickEvent(event)
        )
    }

    override fun postEvent(sendsayProject: SendsayProject, event: Event): Call {
        return doPost(sendsayProject, ApiEndPoint.EndPointName.TRACK_EVENTS, event)
    }

    override fun postCustomer(sendsayProject: SendsayProject, event: Event): Call {
        return doPost(sendsayProject, ApiEndPoint.EndPointName.TRACK_CUSTOMERS, event)
    }

    override fun postFetchAttributes(
        sendsayProject: SendsayProject,
        attributesRequest: CustomerAttributesRequest
    ): Call {
        return doPost(
            sendsayProject,
            ApiEndPoint.EndPointName.CUSTOMERS_ATTRIBUTES,
            attributesRequest
        )
    }

    override fun postFetchConsents(sendsayProject: SendsayProject): Call {
        return doPost(sendsayProject, ApiEndPoint.EndPointName.CONSENTS, null)
    }

    override fun postFetchInAppMessages(
        sendsayProject: SendsayProject,
        customerIds: CustomerIds
    ): Call {
        return doPost(
            sendsayProject,
            ApiEndPoint.EndPointName.IN_APP_MESSAGES,
            hashMapOf(
                "customer_ids" to customerIds.toHashMap(),
                "device" to "android"
            )
        )
    }

    override fun fetchInitConfig(sendsayProject: SendsayProject): Call {
        return doGet(
            sendsayProject,
            ApiEndPoint.forName(ApiEndPoint.EndPointName.GET_CONFIG)
                .withToken(sendsayProject.projectToken)
                .toString(),
        )
    }

    override fun postFetchAppInbox(
        sendsayProject: SendsayProject,
        customerIds: CustomerIds,
        syncToken: String?
    ): Call {
        val reqBody = hashMapOf<String, Any>(
            "customer_ids" to customerIds.toHashMap()
        )
        if (syncToken != null) {
            reqBody.put("sync_token", syncToken)
        }
        return doPost(
            sendsayProject,
            ApiEndPoint.EndPointName.MESSAGE_INBOX,
            reqBody
        )
    }

    override fun postReadFlagAppInbox(
        sendsayProject: SendsayProject,
        customerIds: CustomerIds,
        messageIds: List<String>,
        syncToken: String
    ): Call {
        val reqBody = hashMapOf(
            "customer_ids" to customerIds.toHashMap(),
            "message_ids" to messageIds,
            "sync_token" to syncToken
        )
        return doPost(
            sendsayProject,
            ApiEndPoint.EndPointName.MESSAGE_INBOX_READ,
            reqBody
        )
    }

    override fun postPushSelfCheck(
        sendsayProject: SendsayProject,
        customerIds: CustomerIds,
        pushToken: String,
        tokenType: TokenType
    ): Call {
        return doPost(
            sendsayProject,
            ApiEndPoint.EndPointName.PUSH_SELF_CHECK,
            hashMapOf(
                "platform" to tokenType.selfCheckProperty,
                "customer_ids" to customerIds.toHashMap(),
                "push_notification_id" to pushToken
            )
        )
    }

    internal fun doPost(
        sendsayProject: SendsayProject,
        endpointTemplate: ApiEndPoint.EndPointName,
        bodyContent: Any?
    ): Call {
        return doPost(
            sendsayProject,
            ApiEndPoint.forName(endpointTemplate).withToken(sendsayProject.projectToken).toString(),
            bodyContent
        )
    }

    internal fun doPost(
        sendsayProject: SendsayProject,
        endpoint: String,
        bodyContent: Any?
    ): Call {
        val jsonBody = bodyContent?.let { gson.toJson(it) }
        return networkManager.post(
            sendsayProject.baseUrl + endpoint,
            sendsayProject.authorization,
            jsonBody
        )
    }

    internal fun doGet(
        sendsayProject: SendsayProject,
        endpoint: String,
    ): Call {
        return networkManager.get(sendsayProject.baseUrl + endpoint, sendsayProject.authorization)
    }

    override fun fetchStaticInAppContentBlocks(sendsayProject: SendsayProject): Call {
        return doPost(
            sendsayProject,
            ApiEndPoint.EndPointName.INAPP_CONTENT_BLOCKS_STATIC,
            null
        )
    }

    override fun fetchPersonalizedInAppContentBlocks(
        sendsayProject: SendsayProject,
        customerIds: CustomerIds,
        contentBlockIds: List<String>
    ): Call {
        val reqBody = hashMapOf(
            "customer_ids" to customerIds.toHashMap(),
            "content_block_ids" to contentBlockIds
        )
        return doPost(
            sendsayProject,
            ApiEndPoint.EndPointName.INAPP_CONTENT_BLOCKS_PERSONAL,
            reqBody
        )
    }

    override fun fetchSegments(
        sendsayProject: SendsayProject,
        engagementCookieId: String
    ): Call {
        return doPost(
            sendsayProject,
            ApiEndPoint.forName(ApiEndPoint.EndPointName.SEGMENTS)
                .withToken(sendsayProject.projectToken)
                .withQueryParam("cookie", engagementCookieId)
                .toString(),
            null
        )
    }

    override fun linkIdsToCookie(
        sendsayProject: SendsayProject,
        engagementCookieId: String,
        externalIds: HashMap<String, String?>
    ): Call {
        val reqBody = hashMapOf(
            "external_ids" to externalIds
        )
        return doPost(
            sendsayProject,
            ApiEndPoint.forName(ApiEndPoint.EndPointName.LINK_CUSTOMER_IDS)
                .withToken(sendsayProject.projectToken)
                .withPathParam(ApiEndPoint.COOKIE_ID_PATH_PARAM, engagementCookieId)
                .toString(),
            reqBody
        )
    }
}
