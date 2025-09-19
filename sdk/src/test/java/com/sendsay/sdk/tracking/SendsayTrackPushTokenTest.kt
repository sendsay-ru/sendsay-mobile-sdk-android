package com.sendsay.sdk.tracking

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.sendsay.sdk.Sendsay
import com.sendsay.sdk.manager.EventManagerImpl
import com.sendsay.sdk.mockkConstructorFix
import com.sendsay.sdk.models.Event
import com.sendsay.sdk.models.EventType
import com.sendsay.sdk.models.SendsayConfiguration
import com.sendsay.sdk.models.FlushMode.MANUAL
import com.sendsay.sdk.repository.PushTokenRepositoryProvider
import com.sendsay.sdk.testutil.SendsaySDKTest
import com.sendsay.sdk.util.TokenType
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
