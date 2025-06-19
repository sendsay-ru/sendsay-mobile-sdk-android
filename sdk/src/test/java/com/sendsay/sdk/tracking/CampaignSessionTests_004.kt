package com.sendsay.sdk.tracking

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.test.platform.app.InstrumentationRegistry
import com.sendsay.sdk.Sendsay
import com.sendsay.sdk.models.Constants
import com.sendsay.sdk.models.FlushMode.MANUAL
import com.sendsay.sdk.preferences.SendsayPreferencesImpl
import com.sendsay.sdk.repository.CampaignRepositoryImpl
import com.sendsay.sdk.repository.EventRepositoryImpl
import com.sendsay.sdk.repository.SendsayConfigRepository
import com.sendsay.sdk.testutil.componentForTesting
import com.sendsay.sdk.testutil.runInSingleThread
import com.sendsay.sdk.util.SendsayGson
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.LooperMode

@RunWith(RobolectricTestRunner::class)
internal class CampaignSessionTests_004 : CampaignSessionTests_Base() {

    /**
     * Cold start, campaign click Start, SDK init in Resume
     */
    @Test
    @LooperMode(LooperMode.Mode.PAUSED)
    fun testBehavior_004() = runInSingleThread { idleThreads ->
        val applicationContext = InstrumentationRegistry.getInstrumentation().context
        SendsayConfigRepository.set(applicationContext, configuration)
        val campaignIntent = createDeeplinkIntent()
        val controller = Robolectric.buildActivity(TestActivity::class.java, campaignIntent)
        Sendsay.flushMode = MANUAL
        controller.create()
        controller.start()
        idleThreads()
        assertFalse(Sendsay.isInitialized)
        val preferences = SendsayPreferencesImpl(applicationContext)
        val campaignRepository = CampaignRepositoryImpl(SendsayGson.instance, preferences)
        val eventRepository = EventRepositoryImpl(applicationContext)
        idleThreads()
        val campaignEvent = campaignRepository.get()
        assertNotNull(campaignEvent)
        assertTrue(eventRepository.all().any { it.type == Constants.EventTypes.push })

        controller.resume()
        idleThreads()
        assertTrue(Sendsay.isInitialized)
        controller.pause()
        controller.stop()
        controller.destroy()

        idleThreads()
        assertNull(Sendsay.componentForTesting.campaignRepository.get())
        assertEquals(1, Sendsay.componentForTesting.eventRepository.all().count {
            it.type == Constants.EventTypes.sessionStart
        }, "Only single session_start has to exists")
        idleThreads()
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
     * Used by test testBehavior_004 (Cold start, campaign click Start, SDK init in Resume)
     */
    class TestActivity : AppCompatActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setTheme(androidx.appcompat.R.style.Theme_AppCompat)
            Sendsay.handleCampaignIntent(intent, applicationContext)
        }

        override fun onResume() {
            super.onResume()
            initSendsay(context = this)
        }
    }
}
