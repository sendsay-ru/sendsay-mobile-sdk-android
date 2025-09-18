package ru.sendsay.sdk.runcatching

import ru.sendsay.sdk.Sendsay
import ru.sendsay.sdk.mockkConstructorFix
import ru.sendsay.sdk.models.FlushMode
import ru.sendsay.sdk.telemetry.TelemetryManager
import ru.sendsay.sdk.testutil.SendsaySDKTest
import io.mockk.every
import kotlin.reflect.KFunction
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner

@RunWith(ParameterizedRobolectricTestRunner::class)
internal class SendsaySafeModeInitTest(
    @Suppress("UNUSED_PARAMETER")
    method: KFunction<Any>,
    val lambda: () -> Any
) : SendsaySDKTest() {
    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun data(): List<Array<out Any?>> {
            return PublicApiTestCases.initMethods.map { arrayOf(it.first, it.second) }
        }
    }

    @Test
    fun callInitWithoutExceptionWithSafeModeEnabled() {
        Sendsay.flushMode = FlushMode.MANUAL
        Sendsay.safeModeEnabled = true
        lambda()
        assertTrue(Sendsay.isInitialized)
    }

    @Test
    fun callInitWithExceptionWithSafeModeEnabled() {
        mockkConstructorFix(TelemetryManager::class)
        every {
            anyConstructed<TelemetryManager>().start()
        } throws SendsayExceptionThrowing.TestPurposeException()
        Sendsay.flushMode = FlushMode.MANUAL
        Sendsay.safeModeEnabled = true
        lambda()
        assertFalse(Sendsay.isInitialized)
    }

    @Test(expected = SendsayExceptionThrowing.TestPurposeException::class)
    fun callInitWithExceptionWithSafeModeDisabled() {
        mockkConstructorFix(TelemetryManager::class)
        every {
            anyConstructed<TelemetryManager>().start()
        } throws SendsayExceptionThrowing.TestPurposeException()
        Sendsay.flushMode = FlushMode.MANUAL
        Sendsay.safeModeEnabled = false
        assertFalse(Sendsay.isInitialized)
        lambda()
        assertFalse(Sendsay.isInitialized)
    }
}
