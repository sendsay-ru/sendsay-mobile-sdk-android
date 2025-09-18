package ru.sendsay.sdk.tracking

import android.content.Context
import android.content.Intent
import android.net.Uri
import ru.sendsay.sdk.Sendsay
import ru.sendsay.sdk.manager.FlushManagerImpl
import ru.sendsay.sdk.mockkConstructorFix
import ru.sendsay.sdk.models.SendsayConfiguration
import ru.sendsay.sdk.models.FlushMode
import ru.sendsay.sdk.testutil.SendsayMockServer
import ru.sendsay.sdk.testutil.SendsaySDKTest
import ru.sendsay.sdk.testutil.reset
import ru.sendsay.sdk.testutil.shutdown
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass

internal open class CampaignSessionTests_Base : SendsaySDKTest() {

    companion object {

        val UTM_SOURCE = "testSource"
        val UTM_CAMPAIGN = "campaign001"
        val UTM_CONTENT = "campaignTestContent"
        val UTM_MEDIUM = "medium_98765rfghjmnb"
        val UTM_TERM = "term_098765rtyuk"
        val XNPE_CMP = "3456476768iu-ilkujyfgcvbi7gukgvbnp-oilgvjkjyhgdxcvbiu"
        val CAMPAIGN_URL = "http://example.com/route/to/campaing" +
                "?utm_source=" + UTM_SOURCE +
                "&utm_campaign=" + UTM_CAMPAIGN +
                "&utm_content=" + UTM_CONTENT +
                "&utm_medium=" + UTM_MEDIUM +
                "&utm_term=" + UTM_TERM +
                "&xnpe_cmp=" + XNPE_CMP

        val configuration = SendsayConfiguration()
        val server = SendsayMockServer.createServer()

        @BeforeClass
        @JvmStatic
        fun beforeClass() {
            configuration.projectToken = "TestToken"
            configuration.authorization = "Token TestTokenAuthentication"
            configuration.baseURL = server.url("").toString().substringBeforeLast("/")
            configuration.maxTries = 10
            configuration.automaticSessionTracking = true
        }

        @AfterClass
        @JvmStatic
        fun afterClass() {
            server.shutdown()
        }

        fun initSendsay(context: Context) {
            skipInstallEvent()
            Sendsay.flushMode = FlushMode.MANUAL
            Sendsay.init(context, configuration)
        }

        fun createDeeplinkIntent() = Intent().apply {
            this.action = Intent.ACTION_VIEW
            this.addCategory(Intent.CATEGORY_DEFAULT)
            this.addCategory(Intent.CATEGORY_BROWSABLE)
            this.data = Uri.parse(CAMPAIGN_URL)
        }
    }

    @Before
    fun deinitSendsay() {
        Sendsay.reset()
        Sendsay.shutdown()
    }

    @Before
    fun mockFlush() {
        mockkConstructorFix(FlushManagerImpl::class)
        // flush data does nothing - we want to observe the events in DB
        every { anyConstructed<FlushManagerImpl>().flushData() } just Runs
    }
}
