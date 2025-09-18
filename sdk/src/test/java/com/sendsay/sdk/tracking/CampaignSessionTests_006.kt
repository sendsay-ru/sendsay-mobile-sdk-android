package ru.sendsay.sdk.tracking

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ru.sendsay.sdk.Sendsay
import ru.sendsay.sdk.models.Constants
import ru.sendsay.sdk.testutil.componentForTesting
import ru.sendsay.sdk.testutil.runInSingleThread
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class CampaignSessionTests_006 : CampaignSessionTests_Base() {

    /**
     * Hot Start with Resumed Session, Campaign click start, SDK init in onResume
     */
    @Test
    fun testBehavior_006() = runInSingleThread { idleThreads ->
        // first run will initialize SDK
        val firstRun = Robolectric.buildActivity(TestActivity::class.java)
        firstRun.create(Bundle.EMPTY)
        firstRun.start()
        firstRun.postCreate(null)
        firstRun.resume()
        firstRun.pause()
        firstRun.stop()
        firstRun.destroy()

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
    }

    /**
     * Used by test testBehavior_006 (Hot Start with Resumed Session, Campaign click start, SDK init in onResume)
     */
    class TestActivity : AppCompatActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setTheme(androidx.appcompat.R.style.Theme_AppCompat)
            Sendsay.handleCampaignIntent(intent, applicationContext)
        }

        override fun onResume() {
            super.onResume()
            initSendsay(applicationContext)
        }
    }
}
