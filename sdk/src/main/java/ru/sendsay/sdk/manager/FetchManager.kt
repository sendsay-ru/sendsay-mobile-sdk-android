package ru.sendsay.sdk.manager

import ru.sendsay.sdk.models.ConfigItem
import ru.sendsay.sdk.models.Consent
import ru.sendsay.sdk.models.CustomerIds
import ru.sendsay.sdk.models.CustomerRecommendation
import ru.sendsay.sdk.models.CustomerRecommendationRequest
import ru.sendsay.sdk.models.SendsayProject
import ru.sendsay.sdk.models.FetchError
import ru.sendsay.sdk.models.InAppContentBlock
import ru.sendsay.sdk.models.InAppContentBlockPersonalizedData
import ru.sendsay.sdk.models.InAppMessage
import ru.sendsay.sdk.models.MessageItem
import ru.sendsay.sdk.models.Result
import ru.sendsay.sdk.models.SegmentationCategories

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

    fun fetchInAppMessages(
        sendsayProject: SendsayProject,
        customerIds: CustomerIds,
        onSuccess: (Result<ArrayList<InAppMessage>>) -> Unit,
        onFailure: (Result<FetchError>) -> Unit
    )

    fun fetchInitConfig(
        sendsayProject: SendsayProject,
        onSuccess: (Result<ArrayList<ConfigItem>?>) -> Unit,
        onFailure: (Result<FetchError>) -> Unit
    )

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
