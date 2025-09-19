package com.sendsay.sdk.services

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.sendsay.sdk.Sendsay
import com.sendsay.sdk.manager.EventManagerImpl
import com.sendsay.sdk.mockkConstructorFix
import com.sendsay.sdk.models.EventType
import com.sendsay.sdk.models.SendsayConfiguration
import com.sendsay.sdk.models.FlushMode
import com.sendsay.sdk.repository.SendsayConfigRepository
import com.sendsay.sdk.repository.PushTokenRepositoryProvider
import com.sendsay.sdk.testutil.SendsaySDKTest
import com.sendsay.sdk.testutil.componentForTesting
import com.sendsay.sdk.util.TokenType
import io.mockk.every
import io.mockk.verify
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class TokenTrackingTest() : SendsaySDKTest() {
    lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        mockkConstructorFix(EventManagerImpl::class) {
            every { anyConstructed<EventManagerImpl>().addEventToQueue(any(), any(), any()) }
        }
        Sendsay.flushMode = FlushMode.MANUAL
    }

    @Test
    fun `should track fcm token when Sendsay is initialized`() {
        Sendsay.init(context, SendsayConfiguration(projectToken = "mock-token"))
        Sendsay.handleNewToken(context, "mock token")
        verify {
            Sendsay.componentForTesting.eventManager.track(
                "campaign",
                any(),
                hashMapOf(
                    "google_push_notification_id" to "mock token"
                ),
                EventType.PUSH_TOKEN
            )
        }
        val tokenRepository = PushTokenRepositoryProvider.get(context)
        assertNotNull(tokenRepository.get())
        assertEquals("mock token", tokenRepository.get())
        assertEquals(TokenType.FCM, tokenRepository.getLastTokenType())
        assertNotNull(tokenRepository.getLastTrackDateInMilliseconds())
    }

    @Test
    fun `should track fcm token when Sendsay Config is available`() {
        SendsayConfigRepository.set(context, SendsayConfiguration(projectToken = "mock-token"))
        Sendsay.handleNewToken(context, "mock token")
        verify {
            anyConstructed<EventManagerImpl>().track(
                "campaign",
                any(),
                hashMapOf(
                    "google_push_notification_id" to "mock token"
                ),
                EventType.PUSH_TOKEN
            )
        }
        val tokenRepository = PushTokenRepositoryProvider.get(context)
        assertNotNull(tokenRepository.get())
        assertEquals("mock token", tokenRepository.get())
        assertEquals(TokenType.FCM, tokenRepository.getLastTokenType())
        assertNotNull(tokenRepository.getLastTrackDateInMilliseconds())
    }

    @Test
    fun `should store fcm token when Sendsay Config is NOT available`() {
        val tokenRepository = PushTokenRepositoryProvider.get(context)
        val pushToken = "mock token"
        Sendsay.handleNewToken(context, pushToken)
        // verify that token is stored
        assertNotNull(tokenRepository.get())
        assertEquals(pushToken, tokenRepository.get())
        assertEquals(TokenType.FCM, tokenRepository.getLastTokenType())
        assertNull(tokenRepository.getLastTrackDateInMilliseconds())
        // but verify that tracking was not called
        verify(exactly = 0) {
            anyConstructed<EventManagerImpl>().track(
                "campaign",
                any(),
                any(),
                EventType.PUSH_TOKEN
            )
        }
    }

    @Test
    fun `should store HMS token when Sendsay Config is NOT available`() {
        val tokenRepository = PushTokenRepositoryProvider.get(context)
        val pushToken = "mock token"
        Sendsay.handleNewHmsToken(context, pushToken)
        // verify that token is stored
        assertNotNull(tokenRepository.get())
        assertEquals(pushToken, tokenRepository.get())
        assertEquals(TokenType.HMS, tokenRepository.getLastTokenType())
        // but verify that tracking was not called
        verify(exactly = 0) {
            anyConstructed<EventManagerImpl>().track(
                "campaign",
                any(),
                any(),
                EventType.PUSH_TOKEN
            )
        }
    }

    @Test
    fun `should track fcm token after Sendsay is initialized`() {
        Sendsay.init(context, SendsayConfiguration(projectToken = "mock-token"))
        Sendsay.handleNewToken(context, "mock token")
        assertEquals("mock token", PushTokenRepositoryProvider.get(context).get())
        assertNotNull(PushTokenRepositoryProvider.get(context).getLastTrackDateInMilliseconds())
        verify {
            Sendsay.componentForTesting.eventManager.track(
                "campaign",
                any(),
                hashMapOf(
                    "google_push_notification_id" to "mock token"
                ),
                EventType.PUSH_TOKEN
            )
        }
        assertEquals("mock token", PushTokenRepositoryProvider.get(context).get())
        assertNotNull(PushTokenRepositoryProvider.get(context).getLastTrackDateInMilliseconds())
    }

    /**
     * Purpose of this test is to re-track push token for fresh install of app.
     * Freshly installed app does not contains a stored SDK config
     * and developer may init SDK later than Application::onCreate.
     * Normally, Firebase will not update token on next app run and developer is not be aware of it,
     * so token will not be sent to BE without additional token-getter implementation
     */
    @Test
    fun `should post-track fcm token on first Sendsay init`() {
        // this is required for RN and Flutter wrappers or native SDK delayed init (not Application::onCreate)
        // Firebase sends a new token, but no SDK config is stored
        Sendsay.handleNewToken(context, "mock token")
        // And SDK is initialized later
        Sendsay.init(context, SendsayConfiguration(projectToken = "mock-token"))
        // should re-track a stored token
        verify {
            Sendsay.componentForTesting.eventManager.track(
                "campaign",
                any(),
                hashMapOf(
                    "google_push_notification_id" to "mock token"
                ),
                EventType.PUSH_TOKEN
            )
        }
        assertEquals("mock token", PushTokenRepositoryProvider.get(context).get())
        assertNotNull(PushTokenRepositoryProvider.get(context).getLastTrackDateInMilliseconds())
    }

    /**
     * Purpose of this test is to re-track push token for fresh install of app.
     * Freshly installed app does not contains a stored SDK config
     * and developer may init SDK later than Application::onCreate.
     * Normally, HMS will not update token on next app run and developer is not be aware of it,
     * so token will not be sent to BE without additional token-getter implementation
     */
    @Test
    fun `should post-track HMS token on first Sendsay init`() {
        // this is required for RN and Flutter wrappers or native SDK delayed init (not Application::onCreate)
        // HMS sends a new token, but no SDK config is stored
        Sendsay.handleNewHmsToken(context, "mock token")
        // And SDK is initialized later
        Sendsay.init(context, SendsayConfiguration(projectToken = "mock-token"))
        // should re-track a stored token
        verify {
            Sendsay.componentForTesting.eventManager.track(
                "campaign",
                any(),
                hashMapOf(
                    "huawei_push_notification_id" to "mock token"
                ),
                EventType.PUSH_TOKEN
            )
        }
        assertEquals("mock token", PushTokenRepositoryProvider.get(context).get())
        assertNotNull(PushTokenRepositoryProvider.get(context).getLastTrackDateInMilliseconds())
    }

    @Test
    fun `should track hms token when Sendsay is initialized`() {
        Sendsay.init(context, SendsayConfiguration(projectToken = "mock-token"))
        Sendsay.handleNewHmsToken(context, "mock token")
        verify {
            Sendsay.componentForTesting.eventManager.track(
                "campaign",
                any(),
                hashMapOf(
                    "huawei_push_notification_id" to "mock token"
                ),
                EventType.PUSH_TOKEN
            )
        }
        val tokenRepository = PushTokenRepositoryProvider.get(context)
        assertNotNull(tokenRepository.get())
        assertEquals("mock token", tokenRepository.get())
        assertEquals(TokenType.HMS, tokenRepository.getLastTokenType())
        assertNotNull(tokenRepository.getLastTrackDateInMilliseconds())
    }

    @Test
    fun `should track hms token when Sendsay Config is available`() {
        SendsayConfigRepository.set(context, SendsayConfiguration(projectToken = "mock-token"))
        Sendsay.handleNewHmsToken(context, "mock token")
        verify {
            anyConstructed<EventManagerImpl>().track(
                "campaign",
                any(),
                hashMapOf(
                    "huawei_push_notification_id" to "mock token"
                ),
                EventType.PUSH_TOKEN
            )
        }
        val tokenRepository = PushTokenRepositoryProvider.get(context)
        assertNotNull(tokenRepository.get())
        assertEquals("mock token", tokenRepository.get())
        assertEquals(TokenType.HMS, tokenRepository.getLastTokenType())
        assertNotNull(tokenRepository.getLastTrackDateInMilliseconds())
    }

    @Test
    fun `should track hms token after Sendsay is initialized`() {
        Sendsay.init(context, SendsayConfiguration(projectToken = "mock-token"))
        Sendsay.handleNewHmsToken(context, "mock token")
        assertEquals("mock token", PushTokenRepositoryProvider.get(context).get())
        assertNotNull(PushTokenRepositoryProvider.get(context).getLastTrackDateInMilliseconds())
        verify {
            Sendsay.componentForTesting.eventManager.track(
                "campaign",
                any(),
                hashMapOf(
                    "huawei_push_notification_id" to "mock token"
                ),
                EventType.PUSH_TOKEN
            )
        }
        assertEquals("mock token", PushTokenRepositoryProvider.get(context).get())
        assertNotNull(PushTokenRepositoryProvider.get(context).getLastTrackDateInMilliseconds())
    }
}
