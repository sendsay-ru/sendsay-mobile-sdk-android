package ru.sendsay.sdk.stress

import android.content.Context
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import ru.sendsay.sdk.Sendsay
import ru.sendsay.sdk.manager.ConnectionManager
import ru.sendsay.sdk.manager.EventManagerImpl
import ru.sendsay.sdk.manager.FlushManager
import ru.sendsay.sdk.manager.FlushManagerImpl
import ru.sendsay.sdk.manager.InAppMessageManagerImpl
import ru.sendsay.sdk.mockkConstructorFix
import ru.sendsay.sdk.models.Constants
import ru.sendsay.sdk.models.DeviceProperties
import ru.sendsay.sdk.models.SendsayConfiguration
import ru.sendsay.sdk.models.FlushMode
import ru.sendsay.sdk.models.PropertiesList
import ru.sendsay.sdk.repository.EventRepository
import ru.sendsay.sdk.repository.EventRepositoryImpl
import ru.sendsay.sdk.repository.PushTokenRepositoryImpl
import ru.sendsay.sdk.testutil.SendsayMockServer
import ru.sendsay.sdk.testutil.SendsaySDKTest
import ru.sendsay.sdk.testutil.componentForTesting
import ru.sendsay.sdk.testutil.mocks.SendsayMockService
import ru.sendsay.sdk.testutil.reset
import ru.sendsay.sdk.testutil.waitForIt
import ru.sendsay.sdk.util.currentTimeSeconds
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

    private lateinit var properties: PropertiesList
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
        properties = PropertiesList(properties = DeviceProperties(context).toHashMap())
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
