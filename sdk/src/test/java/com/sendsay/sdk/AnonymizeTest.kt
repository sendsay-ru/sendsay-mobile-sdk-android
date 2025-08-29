package com.sendsay.sdk

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.sendsay.sdk.manager.FetchManagerImpl
import com.sendsay.sdk.manager.SegmentsManagerImpl
import com.sendsay.sdk.models.Constants
import com.sendsay.sdk.models.SendsayConfiguration
import com.sendsay.sdk.models.SendsayProject
import com.sendsay.sdk.models.ExportedEvent
import com.sendsay.sdk.models.FlushMode
import com.sendsay.sdk.models.Result
import com.sendsay.sdk.models.Segment
import com.sendsay.sdk.models.SegmentTest
import com.sendsay.sdk.models.SegmentationCategories
import com.sendsay.sdk.models.SegmentationDataCallback
import com.sendsay.sdk.testutil.SendsaySDKTest
import com.sendsay.sdk.testutil.componentForTesting
import com.sendsay.sdk.testutil.runInSingleThread
import com.sendsay.sdk.util.currentTimeSeconds
import io.mockk.every
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class AnonymizeTest : SendsaySDKTest() {
    companion object {
        private const val PUSH_KEY = "google_push_notification_id"
        private const val HUAWEI_PUSH_KEY = "huawei_push_notification_id"
    }

    private fun checkEvent(
        event: ExportedEvent,
        expectedEventType: String?,
        expectedProject: SendsayProject,
        expectedUserId: String,
        expectedProperties: HashMap<String, Any>? = null
    ) {
        assertEquals(expectedEventType, event.type)
        assertEquals(expectedProject, event.sendsayProject)
        assertEquals(hashMapOf<String, String?>("cookie" to expectedUserId), event.customerIds)
        if (expectedProperties != null) assertEquals(expectedProperties, event.properties)
    }

    @Test
    fun `should anonymize sdk and switch projects`() = runInSingleThread { idleThreads ->
        val context = ApplicationProvider.getApplicationContext<Context>()
        val initialProject = SendsayProject("https://base-url.com", "project-token", "Token auth")
        Sendsay.flushMode = FlushMode.MANUAL
        Sendsay.init(context, SendsayConfiguration(
            baseURL = initialProject.baseUrl,
            projectToken = initialProject.projectToken,
            authorization = initialProject.authorization)
        )
        val testFirebaseToken = "push_token"
        val userId = Sendsay.componentForTesting.customerIdsRepository.get().cookie

        Sendsay.trackEvent(
                eventType = "test",
                properties = hashMapOf("name" to "test"),
                timestamp = currentTimeSeconds()
        )
        Sendsay.trackPushToken(testFirebaseToken)

        val newProject = SendsayProject("https://other-base-url.com", "new_project_token", "Token other-auth")
        Sendsay.anonymize(sendsayProject = newProject)
        Sendsay.trackEvent(
            eventType = "test",
            properties = hashMapOf("name" to "test"),
            timestamp = currentTimeSeconds()
        )
        idleThreads()
        val newUserId = Sendsay.componentForTesting.customerIdsRepository.get().cookie
        val events = Sendsay.componentForTesting.eventRepository.all()
        events.sortedBy { it.timestamp }
        assertEquals(10, events.size)
        checkEvent(events[0], Constants.EventTypes.installation, initialProject, userId!!, null)
        checkEvent(events[1], "test", initialProject, userId, hashMapOf("name" to "test"))
        checkEvent(events[2], Constants.EventTypes.push, initialProject, userId, hashMapOf(
            PUSH_KEY to "push_token"
        ))
        checkEvent(events[3], Constants.EventTypes.sessionEnd, initialProject, userId, null)
        // anonymize is called. We clear push token in old user and track initial events for new user
        checkEvent(events[4], Constants.EventTypes.push, initialProject, userId, hashMapOf(
            PUSH_KEY to " "
        ))
        checkEvent(events[5], Constants.EventTypes.push, initialProject, userId, hashMapOf(
            HUAWEI_PUSH_KEY to " "
        ))
        checkEvent(events[6], Constants.EventTypes.installation, newProject, newUserId!!, null)
        checkEvent(events[7], Constants.EventTypes.sessionStart, newProject, newUserId, null)
        checkEvent(events[8], Constants.EventTypes.push, newProject, newUserId, hashMapOf(
            PUSH_KEY to "push_token"
        ))
        checkEvent(events[9], "test", newProject, newUserId, hashMapOf("name" to "test"))
    }

    @Test
    fun `should not track session start on anonymize when automaticSessionTracking is off`() {
        runInSingleThread { idleThreads ->
            val context = ApplicationProvider.getApplicationContext<Context>()
            val initialProject = SendsayProject("https://base-url.com", "project-token", "Token auth")
            Sendsay.flushMode = FlushMode.MANUAL
            Sendsay.init(context, SendsayConfiguration(
                baseURL = initialProject.baseUrl,
                projectToken = initialProject.projectToken,
                authorization = initialProject.authorization,
                automaticSessionTracking = false)
            )
            val userId = Sendsay.componentForTesting.customerIdsRepository.get().cookie
            Sendsay.trackEvent(
                eventType = "test",
                properties = hashMapOf("name" to "test"),
                timestamp = currentTimeSeconds()
            )
            val newProject = SendsayProject("https://other-base-url.com", "new_project_token", "Token other-auth")
            Sendsay.anonymize(sendsayProject = newProject)
            val newUserId = Sendsay.componentForTesting.customerIdsRepository.get().cookie
            Sendsay.trackEvent(
                eventType = "test",
                properties = hashMapOf("name" to "test"),
                timestamp = currentTimeSeconds()
            )
            idleThreads()
            val events = Sendsay.componentForTesting.eventRepository.all()
            events.sortedBy { it.timestamp }
            assertEquals(events.size, 6)
            checkEvent(events[0], Constants.EventTypes.installation, initialProject, userId!!, null)
            checkEvent(events[1], "test", initialProject, userId, hashMapOf("name" to "test"))
            checkEvent(events[2], Constants.EventTypes.push, initialProject, userId, hashMapOf(
                PUSH_KEY to " "
            ))
            checkEvent(events[3], Constants.EventTypes.push, initialProject, userId, hashMapOf(
                HUAWEI_PUSH_KEY to " "
            ))
            checkEvent(events[4], Constants.EventTypes.installation, newProject, newUserId!!, null)
            checkEvent(events[5], "test", newProject, newUserId, hashMapOf("name" to "test"))
        }
    }

    @Test
    fun `should track session start and end on anonymize when automaticSessionTracking is on`() {
        runInSingleThread { idleThreads ->
            val context = ApplicationProvider.getApplicationContext<Context>()
            val initialProject = SendsayProject("https://base-url.com", "project-token", "Token auth")
            Sendsay.flushMode = FlushMode.MANUAL
            Sendsay.init(context, SendsayConfiguration(
                baseURL = initialProject.baseUrl,
                projectToken = initialProject.projectToken,
                authorization = initialProject.authorization,
                automaticSessionTracking = true)
            )
            val userId = Sendsay.componentForTesting.customerIdsRepository.get().cookie
            Sendsay.trackEvent(
                eventType = "test",
                properties = hashMapOf("name" to "test"),
                timestamp = currentTimeSeconds()
            )
            val newProject = SendsayProject("https://other-base-url.com", "new_project_token", "Token other-auth")
            Sendsay.anonymize(sendsayProject = newProject)
            val newUserId = Sendsay.componentForTesting.customerIdsRepository.get().cookie
            Sendsay.trackEvent(
                eventType = "test",
                properties = hashMapOf("name" to "test"),
                timestamp = currentTimeSeconds()
            )
            idleThreads()
            val events = Sendsay.componentForTesting.eventRepository.all()
            events.sortedBy { it.timestamp }
            assertEquals(events.size, 8)
            checkEvent(events[0], Constants.EventTypes.installation, initialProject, userId!!, null)
            checkEvent(events[1], "test", initialProject, userId, hashMapOf("name" to "test"))
            checkEvent(events[2], Constants.EventTypes.sessionEnd, initialProject, userId, null)
            checkEvent(events[3], Constants.EventTypes.push, initialProject, userId, hashMapOf(
                PUSH_KEY to " "
            ))
            checkEvent(events[4], Constants.EventTypes.push, initialProject, userId, hashMapOf(
                HUAWEI_PUSH_KEY to " "
            ))
            checkEvent(events[5], Constants.EventTypes.installation, newProject, newUserId!!, null)
            checkEvent(events[6], Constants.EventTypes.sessionStart, newProject, newUserId, null)
            checkEvent(events[7], "test", newProject, newUserId, hashMapOf("name" to "test"))
        }
    }

    @Test
    fun `should clear segmentation cache and processes`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        Sendsay.flushMode = FlushMode.MANUAL
        val project = SendsayProject("https://base-url.com", "project-token", "Token auth")
        Sendsay.init(context, SendsayConfiguration(
                baseURL = project.baseUrl,
                projectToken = project.projectToken,
                authorization = project.authorization,
                automaticSessionTracking = false)
        )
        every { anyConstructed<FetchManagerImpl>().fetchSegments(any(), any(), any(), any()) } answers {
            arg<(Result<SegmentationCategories>) -> Unit>(2).invoke(
                Result(true, SegmentTest.getSegmentations())
            )
        }
        Sendsay.registerSegmentationDataCallback(object : SegmentationDataCallback() {
            override val exposingCategory = "discovery"
            override val includeFirstLoad = true
            override fun onNewData(segments: List<Segment>) {
                // be there
            }
        })
        val segmentsManager = Sendsay.componentForTesting.segmentsManager as SegmentsManagerImpl
        assertNotNull(segmentsManager.checkSegmentsJob)
        assertEquals(1, segmentsManager.newbieCallbacks.size)
        Sendsay.anonymize()
        assertNull(Sendsay.componentForTesting.segmentsCache.get())
        assertNull(segmentsManager.checkSegmentsJob)
        assertEquals(0, segmentsManager.newbieCallbacks.size)
        assertNull(Sendsay.componentForTesting.segmentsCache.get())
        Thread.sleep(SegmentsManagerImpl.CHECK_DEBOUNCE_MILLIS + 10)
    }
}
