package ru.sendsay.sdk.runcatching

import androidx.test.core.app.ApplicationProvider
import ru.sendsay.sdk.Sendsay
import ru.sendsay.sdk.manager.EventManagerImpl
import ru.sendsay.sdk.mockkConstructorFix
import ru.sendsay.sdk.models.EventType
import ru.sendsay.sdk.models.SendsayConfiguration
import ru.sendsay.sdk.repository.SendsayConfigRepository
import ru.sendsay.sdk.testutil.SendsaySDKTest
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.slot
import kotlin.reflect.KFunction
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner

@RunWith(ParameterizedRobolectricTestRunner::class)
internal class SendsayColdStartPublicApiTests(
    val method: KFunction<Any>,
    val methodInvoke: () -> Any
) : SendsaySDKTest() {

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun data(): List<Array<out Any?>> {
            return PublicApiTestCases.methods
                .filter { PublicApiTestCases.sdkLessMethods.contains(it.first) }
                .map { arrayOf(it.first, it.second) }
        }
    }

    @Before
    fun disallowSafeMode() {
        // Throws exception while invoking any public API
        Sendsay.safeModeEnabled = false
    }

    @Test
    fun invokePublicMethod_withoutCachedSendsayConfiguration() {
        assertFalse(Sendsay.isInitialized,
            "Ensure non-initialized SDK for method ${method.name}")
        mockkConstructorFix(EventManagerImpl::class) {
            every { anyConstructed<EventManagerImpl>().addEventToQueue(any(), any(), any()) }
        }
        val processTrackSlot = slot<EventType>()
        every {
            anyConstructed<EventManagerImpl>()
                .processTrack(any(), any(), any(), capture(processTrackSlot), any())
        } just Runs
        methodInvoke()
        assertFalse(processTrackSlot.isCaptured,
            "Tracking should not be invoked without config for method ${method.name}")
    }

    @Test
    fun invokePublicMethod_withCachedSendsayConfiguration() {
        assertFalse(Sendsay.isInitialized,
            "Ensure non-initialized SDK for method ${method.name}")
        SendsayConfigRepository.set(
            ApplicationProvider.getApplicationContext(),
            SendsayConfiguration(
                projectToken = "project-token",
                authorization = "Token mock-auth",
                baseURL = "https://api.sendsay.com"
            )
        )
        mockkConstructorFix(EventManagerImpl::class) {
            every { anyConstructed<EventManagerImpl>().addEventToQueue(any(), any(), any()) }
        }
        val processTrackSlot = slot<EventType>()
        every {
            anyConstructed<EventManagerImpl>()
                .processTrack(any(), any(), any(), capture(processTrackSlot), any())
        } just Runs
        methodInvoke()
        assertTrue(processTrackSlot.isCaptured,
            "Tracking should be invoked with config for method ${method.name}")
    }
}
