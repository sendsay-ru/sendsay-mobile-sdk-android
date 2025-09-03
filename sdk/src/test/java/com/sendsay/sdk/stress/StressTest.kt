package com.sendsay.sdk.stress

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.sendsay.sdk.Sendsay
import com.sendsay.sdk.models.Constants
import com.sendsay.sdk.models.CustomerIds
import com.sendsay.sdk.models.DeviceProperties
import com.sendsay.sdk.models.SendsayConfiguration
import com.sendsay.sdk.models.ExportedEvent
import com.sendsay.sdk.models.FlushMode
import com.sendsay.sdk.models.PropertiesList
import com.sendsay.sdk.repository.EventRepository
import com.sendsay.sdk.testutil.SendsayMockServer
import com.sendsay.sdk.testutil.SendsaySDKTest
import com.sendsay.sdk.testutil.componentForTesting
import com.sendsay.sdk.testutil.runInSingleThread
import com.sendsay.sdk.util.currentTimeSeconds
import java.util.Random
import kotlin.coroutines.CoroutineContext
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class StressTest : SendsaySDKTest() {

    companion object {
        val configuration = SendsayConfiguration(automaticSessionTracking = false)
        val server = SendsayMockServer.createServer()
        const val stressCount = 1000

        @BeforeClass
        @JvmStatic
        fun setup() {
            configuration.projectToken = "TestTokem"
            configuration.authorization = "Token TestTokenAuthentication"
            configuration.baseURL = server.url("").toString().substringBeforeLast("/")
            configuration.maxTries = 10
        }

        @AfterClass
        @JvmStatic
        fun tearDown() {
            server.shutdown()
        }
    }

    private lateinit var repo: EventRepository
    private lateinit var properties: HashMap<String, Any>

    @Before
    fun prepareForTest() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        properties = DeviceProperties(context).toHashMap()
        skipInstallEvent()
        Sendsay.flushMode = FlushMode.MANUAL
        Sendsay.init(context, configuration)
        repo = Sendsay.componentForTesting.eventRepository
    }

    @Test
    fun testTrackEventStressed() = runInSingleThread { idleThreads ->

        val eventList = mutableListOf<String>()
        for (i in 0 until stressCount) {

            val eventType = when {
                i % 7 == 0 -> Constants.EventTypes.sessionEnd
                i % 5 == 0 -> Constants.EventTypes.installation
                i % 3 == 0 -> Constants.EventTypes.sessionStart
                i % 2 == 0 -> Constants.EventTypes.payment
                else -> Constants.EventTypes.push
            }
            eventList += eventType
            Sendsay.trackEvent(
                eventType = eventType,
                timestamp = currentTimeSeconds(),
                properties = PropertiesList(properties = properties).toHashMap()
            )
        }
        idleThreads()

        val installCount = eventList.filter { it == Constants.EventTypes.installation }.size
        val sessionEndCount = eventList.filter { it == Constants.EventTypes.sessionEnd }.size
        val sessionStartCount = eventList.filter { it == Constants.EventTypes.sessionStart }.size
        val paymentCount = eventList.filter { it == Constants.EventTypes.sessionStart }.size
        val pushCount = eventList.filter { it == Constants.EventTypes.push }.size

        var pushRepoCount = 0
        var paymentRepoCount = 0
        var sessionStartRepoCount = 0
        var sessionEndRepoCount = 0
        var installRepoCount = 0
        val unknownList = mutableListOf<ExportedEvent>()
        repo.all().forEach {
            when (it.type) {
                Constants.EventTypes.installation -> installRepoCount++
                Constants.EventTypes.sessionEnd -> sessionEndRepoCount++
                Constants.EventTypes.sessionStart -> sessionStartRepoCount++
                Constants.EventTypes.payment -> paymentRepoCount++
                Constants.EventTypes.push -> pushRepoCount++
                else -> unknownList += it
            }
        }

        assertEquals(installCount, installRepoCount)
        assertEquals(sessionEndCount, sessionEndRepoCount)
        assertEquals(sessionStartCount, sessionStartRepoCount)
        assertEquals(paymentCount, paymentRepoCount)
        assertEquals(pushCount, pushRepoCount)
        assertTrue(unknownList.isEmpty())
    }

    @Test
    fun testTrackCustomerStressed() = runInSingleThread { idleThreads ->
        // Track event
        for (i in 0 until stressCount) {
            Sendsay.identifyCustomer(
                customerIds = CustomerIds().withId("registered", "john@doe.com"),
                properties = hashMapOf("first_name" to "NewName")
            )
        }
        idleThreads()
        assertEquals(stressCount, repo.all().size)
    }

    @ObsoleteCoroutinesApi
    @Test
    fun testTrackThreadsStressed() {
        val coroutineList = mutableListOf<CoroutineContext>()
        for (i in 0 until 5) {
            coroutineList += newSingleThreadContext(i.toString())
        }
        val r = Random()
        runBlocking {
            for (i in 0 until stressCount) {
                val eventType = when {
                    i % 7 == 0 -> Constants.EventTypes.sessionEnd
                    i % 5 == 0 -> Constants.EventTypes.installation
                    i % 3 == 0 -> Constants.EventTypes.sessionStart
                    i % 2 == 0 -> Constants.EventTypes.push
                    else -> Constants.EventTypes.payment
                }
                addEvent(
                    coroutineContext = coroutineList[r.nextInt(coroutineList.size)],
                    eventType = eventType,
                    timestamp = currentTimeSeconds(),
                    properties = properties
                )
            }
        }
        assertEquals(stressCount, repo.all().size)
    }

    private suspend fun addEvent(
        coroutineContext: CoroutineContext,
        properties: HashMap<String, Any>,
        timestamp: Double? = currentTimeSeconds(),
        eventType: String?
    ) {
        withContext(coroutineContext) {
            Sendsay.trackEvent(
                eventType = eventType,
                timestamp = timestamp,
                properties = properties
            )
        }
    }
}
