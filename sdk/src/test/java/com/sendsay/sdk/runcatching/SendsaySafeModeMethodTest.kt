package ru.sendsay.sdk.runcatching

import android.os.Build
import androidx.test.core.app.ApplicationProvider
import ru.sendsay.sdk.Sendsay
import ru.sendsay.sdk.models.SendsayConfiguration
import ru.sendsay.sdk.models.FlushMode
import ru.sendsay.sdk.runcatching.SendsayExceptionThrowing.TestPurposeException
import ru.sendsay.sdk.testutil.SendsaySDKTest
import kotlin.reflect.KFunction
import kotlin.test.assertFalse
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

@RunWith(ParameterizedRobolectricTestRunner::class)
internal class SendsaySafeModeMethodTest(
    val method: KFunction<Any>,
    val lambda: () -> Any
) : SendsaySDKTest() {
    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun data(): List<Array<out Any?>> {
            return PublicApiTestCases.methods.map { arrayOf(it.first, it.second) }
        }
    }

    @Before
    fun before() {
        SendsayExceptionThrowing.prepareSendsayToThrow()
    }

    @Test
    fun callBeforeInit() {
        Sendsay.safeModeEnabled = true
        assertFalse { Sendsay.isInitialized }
        lambda()
    }

    @Test
    fun callAfterInitWithSafeModeEnabled() {
        skipInstallEvent()
        Sendsay.flushMode = FlushMode.MANUAL
        Sendsay.init(ApplicationProvider.getApplicationContext(), SendsayConfiguration(projectToken = "mock-token"))
        Sendsay.safeModeEnabled = true
        SendsayExceptionThrowing.makeSendsayThrow()
        lambda()
    }

    @Test(expected = SendsayExceptionThrowing.TestPurposeException::class)
    @Config(sdk = [Build.VERSION_CODES.P])
    @LooperMode(LooperMode.Mode.LEGACY)
    fun callAfterInitWithSafeModeDisabled() {
        skipInstallEvent()
        Sendsay.flushMode = FlushMode.MANUAL
        Sendsay.init(ApplicationProvider.getApplicationContext(), SendsayConfiguration(projectToken = "mock-token"))
        Sendsay.safeModeEnabled = false
        SendsayExceptionThrowing.makeSendsayThrow()
        lambda()
        if (method == Sendsay::isSendsayPushNotification ||
            method == Sendsay::unregisterSegmentationDataCallback) {
            // Note: cannot throw TestPurposeException because it is not accessing SDK in any way
            // we kept invocation of method in this test for check of any other exception/error possibility
            // but TestPurposeException has to be simulated for test pass
            throw TestPurposeException()
        }
    }
}
