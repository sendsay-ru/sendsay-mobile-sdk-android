package ru.sendsay.sdk

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import ru.sendsay.sdk.models.SendsayConfiguration
import ru.sendsay.sdk.models.FlushMode
import ru.sendsay.sdk.testutil.SendsaySDKTest
import kotlin.test.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class TokenTrackingFrequencyTest : SendsaySDKTest() {

    private fun setupConfiguration(configuration: SendsayConfiguration) {
        val context = ApplicationProvider.getApplicationContext<Context>()
        Sendsay.flushMode = FlushMode.MANUAL
        Sendsay.init(context, configuration)
    }

    @Test
    fun dailyToken() {
        val config = SendsayConfiguration(projectToken = "mock-token")
        config.tokenTrackFrequency = SendsayConfiguration.TokenFrequency.DAILY
        setupConfiguration(config)
        assertEquals(Sendsay.tokenTrackFrequency, SendsayConfiguration.TokenFrequency.DAILY)
    }

    @Test
    fun everyLaunchToken() {
        val config = SendsayConfiguration(projectToken = "mock-token")
        config.tokenTrackFrequency = SendsayConfiguration.TokenFrequency.EVERY_LAUNCH
        setupConfiguration(config)
        assertEquals(Sendsay.tokenTrackFrequency, SendsayConfiguration.TokenFrequency.EVERY_LAUNCH)
    }

    @Test
    fun onTokenChangeToken() {
        val config = SendsayConfiguration(projectToken = "mock-token")
        config.tokenTrackFrequency = SendsayConfiguration.TokenFrequency.ON_TOKEN_CHANGE
        setupConfiguration(config)
        assertEquals(Sendsay.tokenTrackFrequency, SendsayConfiguration.TokenFrequency.ON_TOKEN_CHANGE)
    }
}
