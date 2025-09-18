package ru.sendsay.sdk.testutil.mocks

import ru.sendsay.sdk.models.CustomerAttributesRequest
import ru.sendsay.sdk.models.CustomerIds
import ru.sendsay.sdk.models.Event
import ru.sendsay.sdk.models.SendsayProject
import ru.sendsay.sdk.network.SendsayService
import ru.sendsay.sdk.testutil.SendsayMockServer
import ru.sendsay.sdk.util.TokenType
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import okhttp3.mock.HttpCode.HTTP_200_OK
import okhttp3.mock.HttpCode.HTTP_400_BAD_REQUEST
import okhttp3.mock.MockInterceptor

internal class SendsayMockService(
    private val success: Boolean,
    private val response: ResponseBody? = null
) : SendsayService {

    private val server = SendsayMockServer.createServer()
    private val dummyUrl = server.url("/").toString()

    override fun postCampaignClick(sendsayProject: SendsayProject, event: Event): Call {
        return if (success) mockSuccessCall() else mockFailCall()
    }

    override fun postEvent(sendsayProject: SendsayProject, event: Event): Call {
        return if (success) mockSuccessCall() else mockFailCall()
    }

    override fun postCustomer(sendsayProject: SendsayProject, event: Event): Call {
        return if (success) mockSuccessCall() else mockFailCall()
    }

    override fun postFetchConsents(sendsayProject: SendsayProject): Call {
        return if (success) mockSuccessCall() else mockFailCall()
    }

    override fun postFetchAttributes(
        sendsayProject: SendsayProject,
        attributesRequest: CustomerAttributesRequest
    ): Call {
        return if (success) mockSuccessCall() else mockFailCall()
    }

    override fun postFetchInAppMessages(sendsayProject: SendsayProject, customerIds: CustomerIds): Call {
        return if (success) mockSuccessCall() else mockFailCall()
    }

    private fun mockFailCall(): Call {
        val mockInterceptor = MockInterceptor().apply {
            addRule()
                .get().or().post().or().put()
                .url(dummyUrl)
                .anyTimes()
                .respond(HTTP_400_BAD_REQUEST, response)
        }
        val okHttpClient = OkHttpClient
            .Builder()
            .addInterceptor(mockInterceptor)
            .build()

        return okHttpClient.newCall(Request.Builder().url(dummyUrl).get().build())
    }

    private fun mockSuccessCall(): Call {
        val mockInterceptor = MockInterceptor().apply {
            addRule()
                .get().or().post().or().put()
                .url(dummyUrl)
                .anyTimes()
                .respond(HTTP_200_OK, response)
        }
        val okHttpClient = OkHttpClient
            .Builder()
            .addInterceptor(mockInterceptor)
            .build()

        return okHttpClient.newCall(Request.Builder().url(dummyUrl).get().build())
    }

    override fun postPushSelfCheck(
        sendsayProject: SendsayProject,
        customerIds: CustomerIds,
        pushToken: String,
        tokenType: TokenType
    ): Call {
        return if (success) mockSuccessCall() else mockFailCall()
    }

    override fun fetchStaticInAppContentBlocks(sendsayProject: SendsayProject): Call {
        return if (success) mockSuccessCall() else mockFailCall()
    }

    override fun fetchPersonalizedInAppContentBlocks(
        sendsayProject: SendsayProject,
        customerIds: CustomerIds,
        contentBlockIds: List<String>
    ): Call {
        return if (success) mockSuccessCall() else mockFailCall()
    }

    override fun postFetchAppInbox(
        sendsayProject: SendsayProject,
        customerIds: CustomerIds,
        syncToken: String?
    ): Call {
        return if (success) mockSuccessCall() else mockFailCall()
    }

    override fun postReadFlagAppInbox(
        sendsayProject: SendsayProject,
        customerIds: CustomerIds,
        messageIds: List<String>,
        syncToken: String
    ): Call {
        return if (success) mockSuccessCall() else mockFailCall()
    }

    override fun fetchSegments(sendsayProject: SendsayProject, engagementCookieId: String): Call {
        return if (success) mockSuccessCall() else mockFailCall()
    }

    override fun linkIdsToCookie(
        sendsayProject: SendsayProject,
        engagementCookieId: String,
        externalIds: HashMap<String, String?>
    ): Call {
        return if (success) mockSuccessCall() else mockFailCall()
    }
}
