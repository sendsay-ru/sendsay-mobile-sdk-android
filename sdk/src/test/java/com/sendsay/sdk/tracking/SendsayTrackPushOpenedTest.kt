package ru.sendsay.sdk.tracking

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import ru.sendsay.sdk.Sendsay
import ru.sendsay.sdk.manager.EventManagerImpl
import ru.sendsay.sdk.mockkConstructorFix
import ru.sendsay.sdk.models.Constants
import ru.sendsay.sdk.models.Event
import ru.sendsay.sdk.models.EventType
import ru.sendsay.sdk.models.SendsayConfiguration
import ru.sendsay.sdk.models.FlushMode
import ru.sendsay.sdk.models.NotificationAction
import ru.sendsay.sdk.models.NotificationData
import ru.sendsay.sdk.testutil.SendsaySDKTest
import ru.sendsay.sdk.testutil.componentForTesting
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.slot
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner

@RunWith(ParameterizedRobolectricTestRunner::class)
internal class SendsayTrackPushOpenedTest(
    private val name: String,
    private val expectEvent: Boolean,
    private val notificationData: NotificationData?,
    private val actionData: NotificationAction?,
    private val eventName: String,
    private val eventProperties: HashMap<String, Any>,
    private val eventType: EventType
) : SendsaySDKTest() {
    companion object {
        data class TestCase(
            val name: String,
            val expectEvent: Boolean,
            val notificationData: NotificationData?,
            val actionData: NotificationAction?,
            val eventName: String,
            val eventProperties: HashMap<String, Any>,
            val eventType: EventType
        )

        private val testCases = arrayListOf(
            TestCase(
                "empty push data",
                true,
                null,
                null,
                Constants.EventTypes.push,
                hashMapOf(
                    "status" to "clicked",
                    "platform" to "android",
                    "cta" to "notification",
                    "url" to "app"
                ),
                EventType.PUSH_OPENED
            ),
            TestCase(
                "custom event type",
                true,
                NotificationData(hashMapOf("event_type" to "my_push_event")),
                null,
                "my_push_event",
                hashMapOf(
                    "status" to "clicked",
                    "platform" to "android",
                    "cta" to "notification",
                    "url" to "app"
                ),
                EventType.TRACK_EVENT
            ),
            TestCase(
                "empty event type",
                true,
                NotificationData(hashMapOf("event_type" to "")),
                null,
                Constants.EventTypes.push,
                hashMapOf(
                    "status" to "clicked",
                    "platform" to "android",
                    "cta" to "notification",
                    "url" to "app"
                ),
                EventType.PUSH_OPENED
            ),
            TestCase(
                "custom action name",
                true,
                null,
                NotificationAction(actionType = "mock type", actionName = "my action name"),
                Constants.EventTypes.push,
                hashMapOf(
                    "status" to "clicked",
                    "platform" to "android",
                    "cta" to "my action name",
                    "url" to "app"
                ),
                EventType.PUSH_OPENED
            ),
            TestCase(
                "custom action url",
                true,
                null,
                NotificationAction(actionType = "mock type", url = "my action url"),
                Constants.EventTypes.push,
                hashMapOf(
                    "status" to "clicked",
                    "platform" to "android",
                    "cta" to "notification",
                    "url" to "my action url"
                ),
                EventType.PUSH_OPENED
            ),
            TestCase(
                "custom platform",
                true,
                NotificationData(hashMapOf("platform" to "custom platform")),
                null,
                Constants.EventTypes.push,
                hashMapOf(
                    "status" to "clicked",
                    "platform" to "custom platform",
                    "cta" to "notification",
                    "url" to "app"
                ),
                EventType.PUSH_OPENED
            ),
            TestCase(
                "full data",
                true,
                    NotificationData(hashMapOf(
                            "campaign_id" to "mock campaign id",
                            "campaign_name" to "mock campaign name",
                            "action_id" to 123456,
                            "action_name" to "mock action name",
                            "action_type" to "mock action type",
                            "campaign_policy" to "mock campaign policy",
                            "platform" to "mock platform",
                            "language" to "mock language",
                            "recipient" to "mock recipient",
                            "subject" to "mock title"
                    )),
                NotificationAction("mock action type", "mock action name", "mock action url"),
                Constants.EventTypes.push,
                hashMapOf(
                    "status" to "clicked",
                    "cta" to "mock action name",
                    "url" to "mock action url",
                    "campaign_id" to "mock campaign id",
                    "campaign_name" to "mock campaign name",
                    "action_id" to 123456,
                    "action_name" to "mock action name",
                    "action_type" to "mock action type",
                    "campaign_policy" to "mock campaign policy",
                    "platform" to "mock platform",
                    "language" to "mock language",
                    "recipient" to "mock recipient",
                    "subject" to "mock title"
                ),
                EventType.PUSH_OPENED
            ),
            TestCase(
                "nested attributes",
                true,
                NotificationData(hashMapOf(
                        "campaign_id" to "mock campaign id",
                        "campaign_name" to "mock campaign name",
                        "action_id" to 123456,
                        "action_name" to "mock action name",
                        "action_type" to "mock action type",
                        "campaign_policy" to "mock campaign policy",
                        "platform" to "mock platform",
                        "language" to "mock language",
                        "recipient" to "mock recipient",
                        "subject" to "mock title",
                        "product_list" to arrayListOf(
                                hashMapOf(
                                        "item_id" to "1234",
                                        "item_quantity" to 3
                                ),
                                hashMapOf(
                                        "item_id" to "2345",
                                        "item_quantity" to 2
                                ),
                                hashMapOf(
                                        "item_id" to "6789",
                                        "item_quantity" to 1
                                )
                        ),
                        "product_ids" to arrayListOf("1234", "2345", "6789"),
                        "push_content" to hashMapOf(
                                "title" to "Hey!",
                                "actions" to arrayListOf(
                                        hashMapOf(
                                                "title" to "Action 1 title",
                                                "action" to "app"
                                        )
                                ),
                                "message" to "We have a great deal for you today, don't miss it!"
                        )
                )),
                NotificationAction("mock action type", "mock action name", "mock action url"),
                Constants.EventTypes.push,
                hashMapOf(
                        "status" to "clicked",
                        "cta" to "mock action name",
                        "url" to "mock action url",
                        "campaign_id" to "mock campaign id",
                        "campaign_name" to "mock campaign name",
                        "action_id" to 123456,
                        "action_name" to "mock action name",
                        "action_type" to "mock action type",
                        "campaign_policy" to "mock campaign policy",
                        "platform" to "mock platform",
                        "language" to "mock language",
                        "recipient" to "mock recipient",
                        "subject" to "mock title",
                        "product_list" to arrayListOf(
                                hashMapOf(
                                        "item_id" to "1234",
                                        "item_quantity" to 3
                                ),
                                hashMapOf(
                                        "item_id" to "2345",
                                        "item_quantity" to 2
                                ),
                                hashMapOf(
                                        "item_id" to "6789",
                                        "item_quantity" to 1
                                )
                        ),
                        "product_ids" to arrayListOf("1234", "2345", "6789"),
                        "push_content" to hashMapOf(
                                "title" to "Hey!",
                                "actions" to arrayListOf(
                                        hashMapOf(
                                                "title" to "Action 1 title",
                                                "action" to "app"
                                        )
                                ),
                                "message" to "We have a great deal for you today, don't miss it!"
                        )
                ),
                EventType.PUSH_OPENED
        ),
            TestCase(
                "push without consent",
                false,
                NotificationData(hasTrackingConsent = false),
                null,
                Constants.EventTypes.push,
                hashMapOf(),
                EventType.PUSH_OPENED
            ),
            TestCase(
                "push without consent but action forced by url",
                true,
                NotificationData(hasTrackingConsent = false),
                NotificationAction("mock type", "Action", "https://sendsay.com/action?xnpe_force_track=true"),
                Constants.EventTypes.push,
                hashMapOf(
                    "status" to "clicked",
                    "platform" to "android",
                    "cta" to "Action",
                    "url" to "https://sendsay.com/action?xnpe_force_track=true",
                    "tracking_forced" to true
                ),
                EventType.PUSH_OPENED
            )
        )

        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "Tracking {0}")
        fun data(): List<Array<out Any?>> {
            return testCases.map {
                arrayOf(
                    it.name,
                    it.expectEvent,
                    it.notificationData,
                    it.actionData,
                    it.eventName,
                    it.eventProperties,
                    it.eventType
                )
            }
        }
    }
    @Before
    fun before() {
        mockkConstructorFix(EventManagerImpl::class) {
            every { anyConstructed<EventManagerImpl>().addEventToQueue(any(), any(), any()) }
        }
        skipInstallEvent()
        val context = ApplicationProvider.getApplicationContext<Context>()
        val configuration = SendsayConfiguration(projectToken = "mock-token", automaticSessionTracking = false)
        Sendsay.flushMode = FlushMode.MANUAL
        Sendsay.init(context, configuration)
    }

    @Test
    fun `should track push opened`() {
        val eventSlot = slot<Event>()
        val eventTypeSlot = slot<EventType>()
        every {
            anyConstructed<EventManagerImpl>().addEventToQueue(capture(eventSlot), capture(eventTypeSlot), true)
        } just Runs
        Sendsay.trackClickedPush(notificationData, actionData)
        if (expectEvent) {
            assertEquals(notificationData?.campaignData, Sendsay.componentForTesting.campaignRepository.get())
            assertEquals(eventName, eventSlot.captured.type)
            assertEquals(eventProperties, eventSlot.captured.properties)
            assertEquals(eventType, eventTypeSlot.captured)
        } else {
            assertFalse { eventSlot.isCaptured }
        }
    }
}
