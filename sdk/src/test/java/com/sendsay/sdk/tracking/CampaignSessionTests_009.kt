package com.sendsay.sdk.tracking

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.test.platform.app.InstrumentationRegistry
import com.sendsay.sdk.Sendsay
import com.sendsay.sdk.models.Constants
import com.sendsay.sdk.repository.SendsayConfigRepository
import com.sendsay.sdk.testutil.componentForTesting
import com.sendsay.sdk.testutil.runInSingleThread
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Robolectric.buildActivity
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class CampaignSessionTests_009 : CampaignSessionTests_Base() {

    /**
     * Hot start with resume session, campaign click start, SDK init after onResume
     */
    @Test
    fun testBehavior_009() = runInSingleThread { idleThreads ->
        SendsayConfigRepository.set(InstrumentationRegistry.getInstrumentation().context, configuration)
        // first run will initialize SDK
        val firstRun = buildActivity(TestActivity::class.java)
        firstRun.create()
        firstRun.start()
        firstRun.postCreate(null)
        firstRun.resume()
        firstRun.postResume()
        firstRun.visible()
        firstRun.topActivityResumed(true)
        idleThreads()
        val sessionStartRecord = Sendsay.componentForTesting.eventRepository.all().last {
            it.type == Constants.EventTypes.sessionStart
        }
        firstRun.pause()
        firstRun.stop()
        firstRun.destroy()
        idleThreads()

        // second run will handle Campaign Intent, but session will be resumed
        val campaignIntent = createDeeplinkIntent()
        val secondRun = Robolectric.buildActivity(TestActivity::class.java, campaignIntent)
        secondRun.create()
        idleThreads()

        assertTrue(Sendsay.isInitialized)
        val campaignEvent = Sendsay.componentForTesting.campaignRepository.get()
        assertNotNull(campaignEvent)
        assertTrue(Sendsay.componentForTesting.eventRepository.all().any { it.type == Constants.EventTypes.push })

        secondRun.start()
        secondRun.postCreate(null)
        secondRun.resume()
        secondRun.postResume()
        secondRun.visible()
        secondRun.topActivityResumed(true)
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
        assertNull(sessionEvent.properties!!["location"])
        assertNull(sessionEvent.properties!!["utm_source"])
        assertNull(sessionEvent.properties!!["utm_campaign"])
        assertNull(sessionEvent.properties!!["utm_content"])
        assertNull(sessionEvent.properties!!["utm_term"])

        val hasAnySessionEnd = Sendsay.componentForTesting.eventRepository.all().any {
            it.type == Constants.EventTypes.sessionEnd
        }
        assertFalse(hasAnySessionEnd)
        val sessionStartEvents = Sendsay.componentForTesting.eventRepository.all().filter {
            it.type == Constants.EventTypes.sessionStart
        }
        assertEquals(1, sessionStartEvents.size)
        assertEquals(sessionStartRecord.id, sessionStartEvents.first().id)
    }

    /**
     * Used by test testBehavior_009 (Hot start with resume session, campaign click start, SDK init after onResume)
     */
    class TestActivity : AppCompatActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setTheme(androidx.appcompat.R.style.Theme_AppCompat)
            Sendsay.handleCampaignIntent(intent, applicationContext)
        }

        override fun onPostResume() {
            super.onPostResume()
            initSendsay(context = this)
        }
    }
}
