package com.sendsay.sdk.manager

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.sendsay.sdk.Sendsay
import com.sendsay.sdk.mockkConstructorFix
import com.sendsay.sdk.models.SendsayConfiguration
import com.sendsay.sdk.models.FlushMode
import com.sendsay.sdk.models.NotificationPayload
import com.sendsay.sdk.preferences.SendsayPreferencesImpl
import com.sendsay.sdk.repository.SendsayConfigRepository
import com.sendsay.sdk.repository.PushNotificationRepository
import com.sendsay.sdk.repository.PushNotificationRepositoryImpl
import com.sendsay.sdk.repository.PushTokenRepositoryProvider
import com.sendsay.sdk.services.SendsayPushTrackingActivity
import com.sendsay.sdk.testutil.SendsaySDKTest
import com.sendsay.sdk.testutil.data.NotificationTestPayloads
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockkClass
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import java.util.Date
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.Shadows

@RunWith(ParameterizedRobolectricTestRunner::class)
internal class FcmManagerImplTrackingTest(
    @Suppress("UNUSED_PARAMETER")
    name: String,
    private val deliveredTimestamp: Double,
    private val currentTimestamp: Double,
    private val intentGetter: (notification: Notification) -> Intent

) : SendsaySDKTest() {
    private lateinit var manager: FcmManager
    private lateinit var notificationManager: NotificationManager
    private lateinit var context: Context
    private lateinit var trackingConsentManager: TrackingConsentManager
    private lateinit var pushNotificationRepository: PushNotificationRepository

    companion object {
        data class TestCase(
            val name: String,
            val deliveredTimestamp: Double,
            val currentTimestamp: Double,
            val intentGetter: (notification: Notification) -> Intent
        )

        private const val SENT_TIMESTAMP = 1614585422.20

        private val testCases = arrayListOf(
            TestCase(
                name = "sent -> delivered -> clicked",
                deliveredTimestamp = SENT_TIMESTAMP + 2,
                currentTimestamp = SENT_TIMESTAMP + 4,
                intentGetter = { Shadows.shadowOf(it.contentIntent).savedIntent }
            ),
            TestCase(
                name = "sent -> clicked -> delivered",
                deliveredTimestamp = SENT_TIMESTAMP + 4,
                currentTimestamp = SENT_TIMESTAMP + 2,
                intentGetter = { Shadows.shadowOf(it.contentIntent).savedIntent }
            ),
            TestCase(
                name = "delivered -> sent -> clicked",
                deliveredTimestamp = SENT_TIMESTAMP - 2,
                currentTimestamp = SENT_TIMESTAMP + 2,
                intentGetter = { Shadows.shadowOf(it.contentIntent).savedIntent }
            ),
            TestCase(
                name = "clicked -> sent -> delivered",
                deliveredTimestamp = SENT_TIMESTAMP + 2,
                currentTimestamp = SENT_TIMESTAMP - 2,
                intentGetter = { Shadows.shadowOf(it.contentIntent).savedIntent }
            ),
            TestCase(
                name = "delivered -> clicked -> sent",
                deliveredTimestamp = SENT_TIMESTAMP - 4,
                currentTimestamp = SENT_TIMESTAMP - 2,
                intentGetter = { Shadows.shadowOf(it.contentIntent).savedIntent }
            ),
            TestCase(
                name = "clicked -> delivered -> sent",
                deliveredTimestamp = SENT_TIMESTAMP - 2,
                currentTimestamp = SENT_TIMESTAMP - 4,
                intentGetter = { Shadows.shadowOf(it.contentIntent).savedIntent }
            ),
            TestCase(
                name = "clicked == delivered == sent",
                deliveredTimestamp = SENT_TIMESTAMP,
                currentTimestamp = SENT_TIMESTAMP,
                intentGetter = { Shadows.shadowOf(it.contentIntent).savedIntent }
            )
        )

        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "Handling {0}")
        fun data(): List<Array<out Any?>> {
            return testCases.map {
                arrayOf(
                    it.name,
                    it.deliveredTimestamp,
                    it.currentTimestamp,
                    it.intentGetter
                )
            }
        }
    }

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        Sendsay.flushMode = FlushMode.MANUAL
        SendsayConfigRepository.set(context, SendsayConfiguration())
        trackingConsentManager = mockkClass(TrackingConsentManagerImpl::class)
        every {
            trackingConsentManager.trackDeliveredPush(any(), any(), any(), any(), any())
        } just Runs
        every {
            trackingConsentManager.trackClickedPush(any(), any(), any(), any())
        } just Runs
        manager = FcmManagerImpl(
            context,
            SendsayConfiguration(),
            mockkClass(EventManagerImpl::class),
            PushTokenRepositoryProvider.get(context),
            trackingConsentManager
        )
        notificationManager = spyk(context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
        mockkObject(Sendsay)
        every { Sendsay.trackDeliveredPush(any(), any()) } just Runs
        every { Sendsay.trackClickedPush(any(), any(), any()) } just Runs
        pushNotificationRepository = PushNotificationRepositoryImpl(SendsayPreferencesImpl(context))
        every { Sendsay.getPushNotificationRepository() } returns pushNotificationRepository
    }

    @Test
    fun `should track delivered and clicked events with correct timestamps`() {
        val notification = NotificationTestPayloads.PRODUCTION_NOTIFICATION
        val deliveredTimestampSlot = slot<Double>()
        val clickedTimestampSlot = slot<Double>()
        val notificationSlot = slot<Notification>()
        every { notificationManager.notify(any(), capture(notificationSlot)) } just Runs

        manager.handleRemoteMessage(notification, notificationManager, true, timestamp = deliveredTimestamp)
        verify(exactly = 1) {
            trackingConsentManager.trackDeliveredPush(any(), capture(deliveredTimestampSlot), any(), any(), any())
        }
        mockkConstructorFix(Date::class)
        every { anyConstructed<Date>().time } returns (currentTimestamp * 1000).toLong()
        SendsayPushTrackingActivity().processPushClick(
            context,
            intentGetter(notificationSlot.captured)
        )
        verify(exactly = 1) {
            Sendsay.trackClickedPush(any(), any(), capture(clickedTimestampSlot))
        }
        assertTrue { clickedTimestampSlot.captured > deliveredTimestampSlot.captured }
        assertTrue { deliveredTimestampSlot.captured > SENT_TIMESTAMP }
    }

    @Test
    fun `should store delivered and clicked push data`() {
        val notification = NotificationTestPayloads.PRODUCTION_NOTIFICATION
        val notificationSlot = slot<Notification>()
        every { notificationManager.notify(any(), capture(notificationSlot)) } just Runs

        manager.handleRemoteMessage(notification, notificationManager, true, timestamp = deliveredTimestamp)
        verify(exactly = 1) {
            trackingConsentManager.trackDeliveredPush(any(), any(), any(), any(), any())
        }
        SendsayPushTrackingActivity().processPushClick(
            context,
            intentGetter(notificationSlot.captured)
        )
        verify(exactly = 1) {
            Sendsay.trackClickedPush(any(), any(), any())
        }
        val expectedNotificationData = NotificationPayload(notification).attributes
        assertEquals(expectedNotificationData, pushNotificationRepository.getExtraData())
        val storedReceivedPushes = pushNotificationRepository.popDeliveredPushData()
        assertEquals(1, storedReceivedPushes.size)
        val storedAsPayload = NotificationPayload(HashMap(storedReceivedPushes[0] as Map<String, String>))
        assertEquals(expectedNotificationData, storedAsPayload.attributes)
        assertEquals(0, pushNotificationRepository.popDeliveredPushData().size)
        val storedClickedPushes = pushNotificationRepository.popClickedPushData()
        assertEquals(1, storedClickedPushes.size)
        val storedClickAsPayload = NotificationPayload(HashMap(storedClickedPushes[0].extraData))
        assertEquals(expectedNotificationData, storedClickAsPayload.attributes)
        assertEquals(0, pushNotificationRepository.popClickedPushData().size)
    }

    @Test
    fun `should track delivered and clicked events correctly when send_timestamp is missing`() {
        val notification = NotificationTestPayloads.PRODUCTION_NOTIFICATION_WITHOUT_SENT_TIME_AND_TYPE
        val deliveredTimestampSlot = slot<Double>()
        val clickedTimestampSlot = slot<Double>()
        val notificationSlot = slot<Notification>()
        every { notificationManager.notify(any(), capture(notificationSlot)) } just Runs

        manager.handleRemoteMessage(notification, notificationManager, true, timestamp = deliveredTimestamp)
        verify(exactly = 1) {
            trackingConsentManager.trackDeliveredPush(any(), capture(deliveredTimestampSlot), any(), any(), any())
        }
        mockkConstructorFix(Date::class)
        every { anyConstructed<Date>().time } returns (currentTimestamp * 1000).toLong()
        SendsayPushTrackingActivity().processPushClick(
            context,
            intentGetter(notificationSlot.captured)
        )
        verify(exactly = 1) {
            Sendsay.trackClickedPush(any(), any(), capture(clickedTimestampSlot))
        }
        // when set_timestamp is not sent from server, we can not guarantee
        // sent -> delivered -> clicked order, only delivered -> clicked order
        assertTrue { clickedTimestampSlot.captured > deliveredTimestampSlot.captured }
    }

    @Test
    fun `should NOT track delivered and clicked events when consent is not given`() {
        val notification = NotificationTestPayloads.NOTIFICATION_WITH_DENIED_CONSENT
        val deliveredTimestampSlot = slot<Double>()
        val clickedTimestampSlot = slot<Double>()
        val notificationSlot = slot<Notification>()
        every { notificationManager.notify(any(), capture(notificationSlot)) } just Runs

        manager.handleRemoteMessage(notification, notificationManager, true, timestamp = deliveredTimestamp)
        verify(exactly = 0) {
            trackingConsentManager.trackDeliveredPush(any(), capture(deliveredTimestampSlot), any(), any(), any())
        }
        mockkConstructorFix(Date::class)
        every { anyConstructed<Date>().time } returns (currentTimestamp * 1000).toLong()
        SendsayPushTrackingActivity().processPushClick(
            context,
            intentGetter(notificationSlot.captured)
        )
        verify(exactly = 0) {
            Sendsay.trackClickedPush(any(), any(), capture(clickedTimestampSlot))
        }
    }

    @Test
    fun `should NOT track delivered event and DO track clicked event when consent is not given but action forced`() {
        val notification = NotificationTestPayloads.NOTIFICATION_WITH_DENIED_CONSENT_BUT_ACTION_FORCED
        val deliveredTimestampSlot = slot<Double>()
        val clickedTimestampSlot = slot<Double>()
        val notificationSlot = slot<Notification>()
        every { notificationManager.notify(any(), capture(notificationSlot)) } just Runs

        manager.handleRemoteMessage(notification, notificationManager, true, timestamp = deliveredTimestamp)
        verify(exactly = 0) {
            trackingConsentManager.trackDeliveredPush(any(), capture(deliveredTimestampSlot), any(), any(), any())
        }
        mockkConstructorFix(Date::class)
        every { anyConstructed<Date>().time } returns (currentTimestamp * 1000).toLong()
        SendsayPushTrackingActivity().processPushClick(
            context,
            intentGetter(notificationSlot.captured)
        )
        verify(exactly = 1) {
            Sendsay.trackClickedPush(any(), any(), capture(clickedTimestampSlot))
        }
    }

    @Test
    fun `should store delivered and clicked push data when consent is not given`() {
        val notification = NotificationTestPayloads.NOTIFICATION_WITH_DENIED_CONSENT
        val notificationSlot = slot<Notification>()
        every { notificationManager.notify(any(), capture(notificationSlot)) } just Runs

        manager.handleRemoteMessage(notification, notificationManager, true, timestamp = deliveredTimestamp)
        verify(exactly = 0) {
            Sendsay.trackDeliveredPush(any(), any())
        }
        SendsayPushTrackingActivity().processPushClick(
            context,
            intentGetter(notificationSlot.captured)
        )
        verify(exactly = 0) {
            Sendsay.trackClickedPush(any(), any(), any())
        }
        val expectedNotificationData = NotificationPayload(notification).attributes
        assertEquals(expectedNotificationData, pushNotificationRepository.getExtraData())
        val storedReceivedPushes = pushNotificationRepository.popDeliveredPushData()
        assertEquals(1, storedReceivedPushes.size)
        val storedAsPayload = NotificationPayload(HashMap(storedReceivedPushes[0] as Map<String, String>))
        assertEquals(expectedNotificationData, storedAsPayload.attributes)
        assertEquals(0, pushNotificationRepository.popDeliveredPushData().size)
        val storedClickedPushes = pushNotificationRepository.popClickedPushData()
        assertEquals(1, storedClickedPushes.size)
        val storedClickAsPayload = NotificationPayload(HashMap(storedClickedPushes[0].extraData))
        assertEquals(expectedNotificationData, storedClickAsPayload.attributes)
        assertEquals(0, pushNotificationRepository.popClickedPushData().size)
    }
}
