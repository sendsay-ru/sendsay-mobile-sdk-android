package com.sendsay.sdk.manager

import com.sendsay.sdk.models.Consent
import com.sendsay.sdk.models.CustomerIds
import com.sendsay.sdk.models.CustomerRecommendation
import com.sendsay.sdk.models.CustomerRecommendationRequest
import com.sendsay.sdk.models.SendsayProject
import com.sendsay.sdk.models.FetchError
import com.sendsay.sdk.models.InAppContentBlock
import com.sendsay.sdk.models.InAppContentBlockPersonalizedData
import com.sendsay.sdk.models.InAppMessage
import com.sendsay.sdk.models.MessageItem
import com.sendsay.sdk.models.Result
import com.sendsay.sdk.models.SegmentationCategories

internal interface FetchManager {
    fun fetchConsents(
        sendsayProject: SendsayProject,
        onSuccess: (Result<ArrayList<Consent>>) -> Unit,
        onFailure: (Result<FetchError>) -> Unit
    )

    fun fetchRecommendation(
        sendsayProject: SendsayProject,
        recommendationRequest: CustomerRecommendationRequest,
        onSuccess: (Result<ArrayList<CustomerRecommendation>>) -> Unit,
        onFailure: (Result<FetchError>) -> Unit
    )

//    fun fetchInAppMessages(
//        sendsayProject: SendsayProject,
//        customerIds: CustomerIds,
//        onSuccess: (Result<ArrayList<InAppMessage>>) -> Unit,
//        onFailure: (Result<FetchError>) -> Unit
//    )

    fun fetchAppInbox(
        sendsayProject: SendsayProject,
        customerIds: CustomerIds,
        syncToken: String?,
        onSuccess: (Result<ArrayList<MessageItem>?>) -> Unit,
        onFailure: (Result<FetchError>) -> Unit
    )

    fun markAppInboxAsRead(
        sendsayProject: SendsayProject,
        customerIds: CustomerIds,
        syncToken: String,
        messageIds: List<String>,
        onSuccess: (Result<Any?>) -> Unit,
        onFailure: (Result<FetchError>) -> Unit
    )

    fun fetchStaticInAppContentBlocks(
        sendsayProject: SendsayProject,
        onSuccess: (Result<ArrayList<InAppContentBlock>?>) -> Unit,
        onFailure: (Result<FetchError>) -> Unit
    )

    fun fetchPersonalizedContentBlocks(
        sendsayProject: SendsayProject,
        customerIds: CustomerIds,
        contentBlockIds: List<String>,
        onSuccess: (Result<ArrayList<InAppContentBlockPersonalizedData>?>) -> Unit,
        onFailure: (Result<FetchError>) -> Unit
    )

    fun fetchSegments(
        sendsayProject: SendsayProject,
        customerIds: CustomerIds,
        onSuccess: (Result<SegmentationCategories>) -> Unit,
        onFailure: (Result<FetchError>) -> Unit
    )

    fun linkCustomerIdsSync(
        sendsayProject: SendsayProject,
        customerIds: CustomerIds
    ): Result<out Any?>
}
