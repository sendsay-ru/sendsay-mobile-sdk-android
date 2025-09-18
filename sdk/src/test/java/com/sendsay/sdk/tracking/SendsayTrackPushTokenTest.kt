package ru.sendsay.sdk.tracking

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import ru.sendsay.sdk.Sendsay
import ru.sendsay.sdk.manager.EventManagerImpl
import ru.sendsay.sdk.mockkConstructorFix
import ru.sendsay.sdk.models.Event
import ru.sendsay.sdk.models.EventType
import ru.sendsay.sdk.models.SendsayConfiguration
import ru.sendsay.sdk.models.FlushMode.MANUAL
import ru.sendsay.sdk.repository.PushTokenRepositoryProvider
import ru.sendsay.sdk.testutil.SendsaySDKTest
import ru.sendsay.sdk.util.TokenType
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.slot
import io.mockk.verify
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class SendsayTrackPushTokenTest : SendsaySDKTest() {
    @Before
    fun before() {
        mockkConstructorFix(EventManagerImpl::class) {
            every { anyConstructed<EventManagerImpl>().addEventToQueue(any(), any(), any()) }
        }
        skipInstallEvent()
    }

    private fun initSdk(automaticPushNotification: Boolean = true) {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val configuration = SendsayConfiguration(
            projectToken = "mock-token",
            automaticSessionTracking = false,
            automaticPushNotification = automaticPushNotification
        )
        Sendsay.flushMode = MANUAL
        Sendsay.init(context, configuration)
    }

    @Test
    fun `should track FCM push token after SDK init`() {
        initSdk()
        val eventSlot = slot<Event>()
        val eventTypeSlot = slot<EventType>()
        every {
            anyConstructed<EventManagerImpl>().addEventToQueue(capture(eventSlot), capture(eventTypeSlot), any())
        } just Runs
        Sendsay.trackPushToken(token = "test-google-push-token")
        verify(exactly = 1) {
            anyConstructed<EventManagerImpl>().addEventToQueue(any(), any(), any())
        }

        assertEquals("campaign", eventSlot.captured.type)
        assertEquals(
            hashMapOf<String, Any>(
                "google_push_notification_id" to "test-google-push-token"
            ),
            eventSlot.captured.properties
        )
        assertEquals(EventType.PUSH_TOKEN, eventTypeSlot.captured)
    }

    @Test
    fun `should track FCM push token after SDK init with disabled automaticPushNotification`() {
        initSdk(false)
        Sendsay.trackPushToken(token = "test-google-push-token")
        verify(exactly = 1) {
            anyConstructed<EventManagerImpl>().addEventToQueue(any(), any(), any())
        }
    }

    @Test
    fun `should track push HMS token after SDK init`() {
        initSdk()
        val eventSlot = slot<Event>()
        val eventTypeSlot = slot<EventType>()
        every {
            anyConstructed<EventManagerImpl>().addEventToQueue(capture(eventSlot), capture(eventTypeSlot), any())
        } just Runs
        Sendsay.trackHmsPushToken(token = "test-google-push-token")
        verify(exactly = 1) {
            anyConstructed<EventManagerImpl>().addEventToQueue(any(), any(), any())
        }

        assertEquals("campaign", eventSlot.captured.type)
        assertEquals(
            hashMapOf<String, Any>(
                "huawei_push_notification_id" to "test-google-push-token"
            ),
            eventSlot.captured.properties
        )
        assertEquals(EventType.PUSH_TOKEN, eventTypeSlot.captured)
    }

    @Test
    fun `should track HMS push token after SDK init with disabled automaticPushNotification`() {
        initSdk(false)
        Sendsay.trackHmsPushToken(token = "test-google-push-token")
        verify(exactly = 1) {
            anyConstructed<EventManagerImpl>().addEventToQueue(any(), any(), any())
        }
    }

    @Test
    fun `should store FCM push token after SDK init`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val tokenRepository = PushTokenRepositoryProvider.get(context)
        initSdk()
        val pushToken = "test-google-push-token"
        Sendsay.trackPushToken(token = pushToken)
        assertNotNull(tokenRepository.get())
        assertEquals(pushToken, tokenRepository.get())
        assertEquals(TokenType.FCM, tokenRepository.getLastTokenType())
    }

    @Test
    fun `should store HMS push token after SDK init`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val tokenRepository = PushTokenRepositoryProvider.get(context)
        initSdk()
        val pushToken = "test-google-push-token"
        Sendsay.trackHmsPushToken(token = pushToken)
        assertNotNull(tokenRepository.get())
        assertEquals(pushToken, tokenRepository.get())
        assertEquals(TokenType.HMS, tokenRepository.getLastTokenType())
    }

    @Test
    fun `should store FCM push token after SDK init with disabled automaticPushNotification`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val tokenRepository = PushTokenRepositoryProvider.get(context)
        initSdk(automaticPushNotification = false)
        val pushToken = "test-google-push-token"
        Sendsay.trackPushToken(token = pushToken)
        assertNotNull(tokenRepository.get())
        assertEquals(pushToken, tokenRepository.get())
        assertEquals(TokenType.FCM, tokenRepository.getLastTokenType())
    }

    @Test
    fun `should store HMS push token after SDK init with disabled automaticPushNotification`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val tokenRepository = PushTokenRepositoryProvider.get(context)
        initSdk(automaticPushNotification = false)
        val pushToken = "test-google-push-token"
        Sendsay.trackHmsPushToken(token = pushToken)
        assertNotNull(tokenRepository.get())
        assertEquals(pushToken, tokenRepository.get())
        assertEquals(TokenType.HMS, tokenRepository.getLastTokenType())
    }
}
