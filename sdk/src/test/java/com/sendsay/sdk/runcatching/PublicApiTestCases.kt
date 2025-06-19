@file:Suppress("DEPRECATION")

package com.sendsay.sdk.runcatching

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.sendsay.sdk.Sendsay
import com.sendsay.sdk.manager.InAppContentBlockManagerImplTest
import com.sendsay.sdk.models.Constants
import com.sendsay.sdk.models.CustomerIds
import com.sendsay.sdk.models.CustomerRecommendationOptions
import com.sendsay.sdk.models.SendsayConfiguration
import com.sendsay.sdk.models.InAppMessageTest
import com.sendsay.sdk.models.MessageItemAction
import com.sendsay.sdk.models.MessageItemAction.Type.BROWSER
import com.sendsay.sdk.models.PropertiesList
import com.sendsay.sdk.models.PurchasedItem
import com.sendsay.sdk.models.Segment
import com.sendsay.sdk.models.SegmentationDataCallback
import com.sendsay.sdk.repository.AppInboxCacheImplTest
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.reflect.KFunction
import kotlin.reflect.KFunction1
import kotlin.reflect.KFunction2
import kotlin.reflect.KFunction4

internal object PublicApiTestCases {
    val properties = arrayOf(
        Pair(Sendsay::campaignTTL, Constants.Campaign.defaultCampaignTTL),
        Pair(Sendsay::isAutomaticSessionTracking, Constants.Session.defaultAutomaticTracking),
        Pair(Sendsay::flushMode, Constants.Flush.defaultFlushMode),
        Pair(Sendsay::flushPeriod, Constants.Flush.defaultFlushPeriod),
        Pair(Sendsay::isAutoPushNotification, Constants.PushNotif.defaultAutomaticListening),
        Pair(Sendsay::loggerLevel, Constants.Logger.defaultLoggerLevel),
        Pair(Sendsay::sessionTimeout, Constants.Session.defaultTimeout),
        Pair(Sendsay::tokenTrackFrequency, Constants.Token.defaultTokenFrequency),
        Pair(Sendsay::defaultProperties, hashMapOf<String, Any>()),
        Pair(Sendsay::isInitialized, false),
        Pair(Sendsay::notificationDataCallback, null),
        Pair(Sendsay::inAppMessageActionCallback, Constants.InApps.defaultInAppMessageDelegate),
        Pair(Sendsay::customerCookie, null),
        Pair(Sendsay::checkPushSetup, false),
        Pair(Sendsay::appInboxProvider, Constants.AppInbox.defaulAppInboxProvider),
        Pair(Sendsay::safeModeEnabled, true),
        Pair(Sendsay::runDebugMode, false),
        Pair(Sendsay::segmentationDataCallbacks, CopyOnWriteArrayList<SegmentationDataCallback>()),
        Pair(Sendsay::pushNotificationsDelegate, null)
    )

    val initMethods: Array<Pair<KFunction<Any>, () -> Any>> = arrayOf(
        Pair<KFunction1<Context, Boolean>, () -> Any>(
            Sendsay::init
        ) { Sendsay.init(ApplicationProvider.getApplicationContext()) },
        Pair<KFunction2<Context, SendsayConfiguration, Unit>, () -> Any>(
            Sendsay::init
        ) { Sendsay.init(
            ApplicationProvider.getApplicationContext(),
            SendsayConfiguration(projectToken = "mock-token")
        ) },
        Pair<KFunction<Any>, () -> Any>(
            Sendsay::initFromFile
        ) { Sendsay.init(ApplicationProvider.getApplicationContext()) }
    )

    val methods = arrayOf(
        Pair(Sendsay::anonymize
        ) { Sendsay.anonymize() },
        Pair(
            Sendsay::identifyCustomer
        ) { Sendsay.identifyCustomer(CustomerIds(), PropertiesList(hashMapOf())) },
        Pair(
            Sendsay::flushData
        ) { Sendsay.flushData() },
        Pair(
            Sendsay::getConsents
        ) { Sendsay.getConsents({}, {}) },
        Pair(
            Sendsay::handleCampaignIntent
        ) {
            val campaignId = "http://example.com/route/to/campaing" +
                    "?utm_source=mock-utm-source" +
                    "&utm_campaign=mock-utm-campaign" +
                    "&utm_content=mock-utm-content" +
                    "&utm_medium=mock-utm-medium" +
                    "&utm_term=mock-utm-term" +
                    "&xnpe_cmp=mock-xnpe-xmp"
            val intent = Intent().apply {
                this.action = Intent.ACTION_VIEW
                this.addCategory(Intent.CATEGORY_DEFAULT)
                this.addCategory(Intent.CATEGORY_BROWSABLE)
                this.data = Uri.parse(campaignId)
            }
            Sendsay.handleCampaignIntent(intent, ApplicationProvider.getApplicationContext())
        },
        Pair<KFunction4<Context, Map<String, String>, NotificationManager, Boolean, Boolean>, () -> Any>(
            Sendsay::handleRemoteMessage
        ) {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val messageData = mapOf(Pair("source", Constants.PushNotif.source))
            Sendsay.handleRemoteMessage(context, messageData, notificationManager, false)
        },
        Pair<KFunction1<Map<String, String>, Boolean>, () -> Any>(
            Sendsay::isSendsayPushNotification
        ) {
            Sendsay.isSendsayPushNotification(null)
        },
        Pair(
            Sendsay::trackClickedPush
        ) { Sendsay.trackClickedPush() },
        Pair(
            Sendsay::trackClickedPushWithoutTrackingConsent
        ) { Sendsay.trackClickedPushWithoutTrackingConsent() },
        Pair(
            Sendsay::trackDeliveredPush
        ) { Sendsay.trackDeliveredPush() },
        Pair(
            Sendsay::trackDeliveredPushWithoutTrackingConsent
        ) { Sendsay.trackDeliveredPushWithoutTrackingConsent() },
        Pair(
            Sendsay::trackEvent
        ) { Sendsay.trackEvent(PropertiesList(hashMapOf()), null, null) },
        Pair(
            Sendsay::trackPaymentEvent
        ) {
            val purchasedItem = PurchasedItem(
                1.0,
                "eur",
                "mock-payment-system",
                "mock-product-id",
                "mock-product-title"
            )
            Sendsay.trackPaymentEvent(1.0, purchasedItem)
        },
        Pair<KFunction1<String, Unit>, () -> Any>(
            Sendsay::trackPushToken
        ) { Sendsay.trackPushToken("mock-push-token") },
        Pair<KFunction1<String, Unit>, () -> Any>(
            Sendsay::trackHmsPushToken
        ) { Sendsay.trackHmsPushToken("mock-push-token") },
        Pair(
            Sendsay::trackSessionEnd
        ) { Sendsay.trackSessionEnd() },
        Pair(
            Sendsay::trackSessionStart
        ) { Sendsay.trackSessionStart() },
        Pair(
            Sendsay::fetchRecommendation
        ) { Sendsay.fetchRecommendation(CustomerRecommendationOptions("", true), {}, {}) },
        Pair<KFunction2<Context, String, Unit>, () -> Any>(
            Sendsay::handleNewToken
        ) { Sendsay.handleNewToken(ApplicationProvider.getApplicationContext(), "mock-push-token") },
        Pair<KFunction2<Context, String, Unit>, () -> Any>(
            Sendsay::handleNewHmsToken
        ) { Sendsay.handleNewHmsToken(ApplicationProvider.getApplicationContext(), "mock-push-token") },
        Pair(
            Sendsay::trackInAppMessageClick
        ) { Sendsay.trackInAppMessageClick(
            InAppMessageTest.buildInAppMessage(),
            "mock-button-text",
            "mock-button-link") },
        Pair(
            Sendsay::trackInAppMessageClickWithoutTrackingConsent
        ) { Sendsay.trackInAppMessageClickWithoutTrackingConsent(
            InAppMessageTest.buildInAppMessage(),
            "mock-button-text",
            "mock-button-link") },
        Pair(
            Sendsay::trackInAppMessageClose
        ) { Sendsay.trackInAppMessageClose(InAppMessageTest.buildInAppMessage())
        },
        Pair(Sendsay::trackInAppMessageCloseWithoutTrackingConsent) {
            Sendsay.trackInAppMessageCloseWithoutTrackingConsent(InAppMessageTest.buildInAppMessage())
        },
        Pair(Sendsay::trackInAppMessageCloseWithoutTrackingConsent) {
            Sendsay.trackInAppMessageCloseWithoutTrackingConsent(InAppMessageTest.buildInAppMessage())
        },
        Pair(Sendsay::getAppInboxButton) {
            Sendsay.getAppInboxButton(ApplicationProvider.getApplicationContext())
        },
        Pair(Sendsay::getAppInboxListView) {
            Sendsay.getAppInboxListView(
                ApplicationProvider.getApplicationContext(),
                onItemClicked = { _, _ -> }
            )
        },
        Pair(Sendsay::getAppInboxListFragment) {
            Sendsay.getAppInboxListFragment(ApplicationProvider.getApplicationContext())
        },
        Pair(Sendsay::getAppInboxDetailFragment) {
            Sendsay.getAppInboxDetailFragment(ApplicationProvider.getApplicationContext(), "1")
        },
        Pair(Sendsay::getAppInboxDetailView) {
            Sendsay.getAppInboxDetailView(ApplicationProvider.getApplicationContext(), "1")
        },
        Pair(Sendsay::fetchAppInbox) {
            Sendsay.fetchAppInbox(callback = { _ -> })
        },
        Pair(Sendsay::fetchAppInboxItem) {
            Sendsay.fetchAppInboxItem("1") { _ -> }
        },
        Pair(Sendsay::trackAppInboxOpened) {
            Sendsay.trackAppInboxOpened(AppInboxCacheImplTest.buildMessage("1"))
        },
        Pair(Sendsay::trackAppInboxOpenedWithoutTrackingConsent) {
            Sendsay.trackAppInboxOpenedWithoutTrackingConsent(AppInboxCacheImplTest.buildMessage("1"))
        },
        Pair(Sendsay::trackAppInboxClick) {
            Sendsay.trackAppInboxClick(buildMessageItemAction(), AppInboxCacheImplTest.buildMessage("1"))
        },
        Pair(Sendsay::trackAppInboxClickWithoutTrackingConsent) {
            Sendsay.trackAppInboxClickWithoutTrackingConsent(
                buildMessageItemAction(),
                AppInboxCacheImplTest.buildMessage("1")
            )
        },
        Pair(Sendsay::markAppInboxAsRead) {
            Sendsay.markAppInboxAsRead(
                AppInboxCacheImplTest.buildMessage("1"),
                null
            )
        },
        Pair(Sendsay::getInAppContentBlocksPlaceholder) {
            Sendsay.getInAppContentBlocksPlaceholder("placeholder1", ApplicationProvider.getApplicationContext())
        },
        Pair(Sendsay::requestPushAuthorization) {
            Sendsay.requestPushAuthorization(ApplicationProvider.getApplicationContext()) { }
        },
        Pair(Sendsay::trackInAppContentBlockClick) {
            Sendsay.trackInAppContentBlockClick(
                    "placeholder1",
                    InAppContentBlockManagerImplTest.buildAction(),
                    InAppContentBlockManagerImplTest.buildMessage(dateFilter = null)
            )
        },
        Pair(Sendsay::trackInAppContentBlockClickWithoutTrackingConsent) {
            Sendsay.trackInAppContentBlockClickWithoutTrackingConsent(
                    "placeholder1",
                    InAppContentBlockManagerImplTest.buildAction(),
                    InAppContentBlockManagerImplTest.buildMessage(dateFilter = null)
            )
        },
        Pair(Sendsay::trackInAppContentBlockClose) {
            Sendsay.trackInAppContentBlockClose(
                    "placeholder1",
                    InAppContentBlockManagerImplTest.buildMessage(dateFilter = null)
            )
        },
        Pair(Sendsay::trackInAppContentBlockCloseWithoutTrackingConsent) {
            Sendsay.trackInAppContentBlockCloseWithoutTrackingConsent(
                    "placeholder1",
                    InAppContentBlockManagerImplTest.buildMessage(dateFilter = null)
            )
        },
        Pair(Sendsay::trackInAppContentBlockError) {
            Sendsay.trackInAppContentBlockError(
                    "placeholder1",
                    InAppContentBlockManagerImplTest.buildMessage(dateFilter = null),
                    "Failure message"
            )
        },
        Pair(Sendsay::trackInAppContentBlockErrorWithoutTrackingConsent) {
            Sendsay.trackInAppContentBlockErrorWithoutTrackingConsent(
                    "placeholder1",
                    InAppContentBlockManagerImplTest.buildMessage(dateFilter = null),
                    "Failure message"
            )
        },
        Pair(Sendsay::trackInAppContentBlockShown) {
            Sendsay.trackInAppContentBlockShown(
                    "placeholder1",
                    InAppContentBlockManagerImplTest.buildMessage(dateFilter = null)
            )
        },
        Pair(Sendsay::trackInAppContentBlockShownWithoutTrackingConsent) {
            Sendsay.trackInAppContentBlockShownWithoutTrackingConsent(
                    "placeholder1",
                    InAppContentBlockManagerImplTest.buildMessage(dateFilter = null)
            )
        },
        Pair(Sendsay::registerSegmentationDataCallback) {
            Sendsay.registerSegmentationDataCallback(object : SegmentationDataCallback() {
                override val exposingCategory = "discovery"
                override val includeFirstLoad = false
                override fun onNewData(segments: List<Segment>) {
                    // nothing
                }
            })
        },
        Pair(Sendsay::unregisterSegmentationDataCallback) {
            Sendsay.unregisterSegmentationDataCallback(object : SegmentationDataCallback() {
                override val exposingCategory = "discovery"
                override val includeFirstLoad = false
                override fun onNewData(segments: List<Segment>) {
                    // nothing
                }
            })
        },
        Pair(Sendsay::getSegments) {
            Sendsay.getSegments("discovery") {
                // nothing
            }
        },
        Pair(Sendsay::getInAppContentBlocksCarousel) {
            Sendsay.getInAppContentBlocksCarousel(
                ApplicationProvider.getApplicationContext(),
                "placeholder1"
            )
        }
    )

    private fun buildMessageItemAction(): MessageItemAction {
        return MessageItemAction().apply {
            url = "https://test.com"
            type = BROWSER
            title = "test"
        }
    }

    val awaitInitMethods = arrayOf(
        Sendsay::identifyCustomer,
        Sendsay::flushData,
        Sendsay::getConsents,
        Sendsay::trackClickedPush,
        Sendsay::trackClickedPushWithoutTrackingConsent,
        Sendsay::trackDeliveredPush,
        Sendsay::trackDeliveredPushWithoutTrackingConsent,
        Sendsay::trackEvent,
        Sendsay::trackPaymentEvent,
        Sendsay::trackPushToken,
        Sendsay::trackHmsPushToken,
        Sendsay::trackSessionEnd,
        Sendsay::trackSessionStart,
        Sendsay::fetchRecommendation,
        Sendsay::trackInAppMessageClick,
        Sendsay::trackInAppMessageClickWithoutTrackingConsent,
        Sendsay::trackInAppMessageClose,
        Sendsay::trackInAppMessageCloseWithoutTrackingConsent,
        Sendsay::fetchAppInbox,
        Sendsay::fetchAppInboxItem,
        Sendsay::trackAppInboxOpened,
        Sendsay::trackAppInboxOpenedWithoutTrackingConsent,
        Sendsay::trackAppInboxClick,
        Sendsay::trackAppInboxClickWithoutTrackingConsent,
        Sendsay::markAppInboxAsRead,
        Sendsay::getSegments
    )

    val sdkLessMethods = arrayOf(
        Sendsay::handleCampaignIntent,
        Sendsay::handleRemoteMessage,
        Sendsay::handleNewToken,
        Sendsay::handleNewHmsToken
    )
}
