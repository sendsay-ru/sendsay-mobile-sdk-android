package ru.sendsay.sdk.tracking

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.test.platform.app.InstrumentationRegistry
import ru.sendsay.sdk.Sendsay
import ru.sendsay.sdk.models.Constants
import ru.sendsay.sdk.testutil.componentForTesting
import ru.sendsay.sdk.testutil.runInSingleThread
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class CampaignSessionTests_002 : CampaignSessionTests_Base() {

    /**
     * Hot start with new session, Campaign click start, SDK init before onResume
     */
    @Test
    fun testbehavior_002() = runInSingleThread { idleThreads ->
        initSendsay(InstrumentationRegistry.getInstrumentation().context)
        val campaignIntent = createDeeplinkIntent()
        val controller = Robolectric.buildActivity(TestActivity::class.java, campaignIntent)
        controller.create()
        idleThreads()

        assertTrue(Sendsay.isInitialized)
        val campaignEvent = Sendsay.componentForTesting.campaignRepository.get()
        assertNotNull(campaignEvent)
        assertTrue(Sendsay.componentForTesting.eventRepository.all().any { it.type == Constants.EventTypes.push })

        controller.start()
        controller.postCreate(null)
        controller.resume()
        idleThreads()
        assertNull(Sendsay.componentForTesting.campaignRepository.get())
        assertEquals(1, Sendsay.componentForTesting.eventRepository.all().count {
            it.type == Constants.EventTypes.sessionStart
        }, "Only single session_start has to exists")
        val sessionEvent = Sendsay.componentForTesting.eventRepository.all().find {
            it.type == Constants.EventTypes.sessionStart
        }
        assertNotNull(sessionEvent)
        assertNotNull(sessionEvent.properties)
        assertEquals(campaignEvent.completeUrl, sessionEvent.properties!!["location"])
        assertEquals(campaignEvent.source, sessionEvent.properties!!["utm_source"])
        assertEquals(campaignEvent.campaign, sessionEvent.properties!!["utm_campaign"])
        assertEquals(campaignEvent.content, sessionEvent.properties!!["utm_content"])
        assertEquals(campaignEvent.term, sessionEvent.properties!!["utm_term"])
    }

    /**
     * Used by test testBehavior_002 (Hot start with new session, Campaign click start, SDK init before onResume)
     */
    class TestActivity : AppCompatActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setTheme(androidx.appcompat.R.style.Theme_AppCompat)
            initSendsay(applicationContext)
            Sendsay.handleCampaignIntent(intent, applicationContext)
        }
    }
}
