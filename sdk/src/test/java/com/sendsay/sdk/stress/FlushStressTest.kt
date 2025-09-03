package com.sendsay.sdk.stress

import android.content.Context
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.sendsay.sdk.Sendsay
import com.sendsay.sdk.manager.ConnectionManager
import com.sendsay.sdk.manager.EventManagerImpl
import com.sendsay.sdk.manager.FlushManager
import com.sendsay.sdk.manager.FlushManagerImpl
import com.sendsay.sdk.manager.InAppMessageManagerImpl
import com.sendsay.sdk.mockkConstructorFix
import com.sendsay.sdk.models.Constants
import com.sendsay.sdk.models.DeviceProperties
import com.sendsay.sdk.models.SendsayConfiguration
import com.sendsay.sdk.models.FlushMode
import com.sendsay.sdk.models.PropertiesAdapter
import com.sendsay.sdk.repository.EventRepository
import com.sendsay.sdk.repository.EventRepositoryImpl
import com.sendsay.sdk.repository.PushTokenRepositoryImpl
import com.sendsay.sdk.testutil.SendsayMockServer
import com.sendsay.sdk.testutil.SendsaySDKTest
import com.sendsay.sdk.testutil.componentForTesting
import com.sendsay.sdk.testutil.mocks.SendsayMockService
import com.sendsay.sdk.testutil.reset
import com.sendsay.sdk.testutil.waitForIt
import com.sendsay.sdk.util.currentTimeSeconds
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import java.util.Random
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelChildren
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
internal class FlushStressTest : SendsaySDKTest() {
    companion object {
        val configuration = SendsayConfiguration().apply {
            automaticSessionTracking = false
        }
        val server = SendsayMockServer.createServer()
        const val stressCount = 500

        @BeforeClass
        @JvmStatic
        fun setup() {
            configuration.baseURL = server.url("/").toString()
            configuration.projectToken = "projectToken"
            configuration.authorization = "Token projectAuthorization"
        }

        @AfterClass
        @JvmStatic
        fun tearDown() {
            server.shutdown()
        }
    }

    private lateinit var properties: PropertiesAdapter
    private lateinit var manager: FlushManager
    private lateinit var repo: EventRepository
    private lateinit var service: SendsayMockService

    @Before
    fun init() {
        mockkConstructorFix(EventRepositoryImpl::class)
        mockkConstructorFix(InAppMessageManagerImpl::class)
        every {
            anyConstructed<InAppMessageManagerImpl>().show(any())
        } just Runs
        every {
            anyConstructed<InAppMessageManagerImpl>().onEventUploaded(any())
        } just Runs
        every {
            anyConstructed<InAppMessageManagerImpl>().onEventCreated(any(), any())
        } just Runs
        mockkConstructorFix(EventManagerImpl::class) {
            every {
                anyConstructed<EventManagerImpl>().notifyEventCreated(any(), any())
            }
        }
        every {
            anyConstructed<EventManagerImpl>().notifyEventCreated(any(), any())
        } just Runs
        mockkConstructorFix(PushTokenRepositoryImpl::class)
        every {
            anyConstructed<PushTokenRepositoryImpl>().get()
        } returns null
        val context = ApplicationProvider.getApplicationContext<Context>()
        properties = PropertiesAdapter(properties = DeviceProperties(context).toHashMap())
        val connectedManager = mockk<ConnectionManager>()
        every { connectedManager.isConnectedToInternet() } returns true
        Sendsay.reset()
        skipInstallEvent()
        Sendsay.flushMode = FlushMode.MANUAL
        Sendsay.init(context, configuration)
        waitForIt {
            Sendsay.initGate.waitForInitialize { it() }
        }
        repo = Sendsay.componentForTesting.eventRepository
        service = SendsayMockService(true)
        manager = FlushManagerImpl(configuration, repo, service, connectedManager, {})
        repo.clear()
        Dispatchers.Main.cancelChildren()
        Dispatchers.Default.cancelChildren()
    }

    @Test
    fun testFlushingStressed() {
        val r = Random()
        var insertedCount = 0
        for (i in 0 until stressCount) {

            SendsayMockServer.setResponseSuccess(server, "tracking/track_event_success.json")

            val eventType = when {
                i % 7 == 0 -> Constants.EventTypes.sessionEnd
                i % 5 == 0 -> Constants.EventTypes.installation
                i % 3 == 0 -> Constants.EventTypes.sessionStart
                i % 2 == 0 -> Constants.EventTypes.payment
                else -> Constants.EventTypes.push
            }

            val allEventsBeforeTrack = repo.all()
            assertEquals(allEventsBeforeTrack.size, insertedCount, "Found events $allEventsBeforeTrack")
            waitForIt {
                every {
                    anyConstructed<EventRepositoryImpl>().add(any())
                } answers {
                    callOriginal()
                    it()
                }
                Sendsay.trackEvent(
                    eventType = eventType,
                    timestamp = currentTimeSeconds(),
                    properties = properties
                )
                shadowOf(Looper.getMainLooper()).idle()
                for (i in 0..10) {
                    ShadowLooper.idleMainLooper()
                }
            }
            insertedCount++
            val allEventsAfterTrack = repo.all()
            assertEquals(allEventsAfterTrack.size, insertedCount, "Found events $allEventsAfterTrack")

            if (r.nextInt(3) == 0) {
                waitForIt {
                    manager.flushData { flushResult ->
                        assertTrue(
                            flushResult.isSuccess,
                            "Flush failed, error: ${flushResult.exceptionOrNull()?.localizedMessage}"
                        )
                        val allEventsAfterFlush = repo.all()
                        it.assertEquals(
                            0,
                            allEventsAfterFlush.size,
                            "Found $allEventsAfterFlush after flush of $insertedCount events"
                        )
                        insertedCount = 0
                        it()
                    }
                }
            }
        }
    }
}
