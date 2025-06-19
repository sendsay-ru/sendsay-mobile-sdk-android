package com.sendsay.sdk.services

import androidx.test.core.app.ApplicationProvider
import androidx.work.Configuration
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.sendsay.sdk.Sendsay
import com.sendsay.sdk.manager.FlushFinishedCallback
import com.sendsay.sdk.manager.FlushManagerImpl
import com.sendsay.sdk.mockkConstructorFix
import com.sendsay.sdk.models.SendsayConfiguration
import com.sendsay.sdk.models.FlushMode
import com.sendsay.sdk.models.FlushPeriod
import com.sendsay.sdk.testutil.SendsaySDKTest
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.slot
import java.util.concurrent.TimeUnit
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class PeriodicFlushTest : SendsaySDKTest() {

    @Before
    fun before() {
        WorkManagerTestInitHelper.initializeTestWorkManager(
            ApplicationProvider.getApplicationContext(),
            Configuration.Builder().setExecutor(SynchronousExecutor()).build()
        )
    }

    @After
    fun after() {
        clearAllMocks()
    }

    private fun getUniqueWorkInfo(workName: String): WorkInfo? {
        val workInfos = WorkManager.getInstance(ApplicationProvider.getApplicationContext())
            .getWorkInfosForUniqueWork(workName).get()
        return workInfos.firstOrNull()
    }

    private fun executeWork(): WorkInfo {
        val workManager = WorkManager.getInstance(ApplicationProvider.getApplicationContext())
        val request = OneTimeWorkRequest.Builder(SendsayPeriodicFlushWorker::class.java).build()
        workManager.enqueue(request).result.get()

        return workManager.getWorkInfoById(request.id).get()
    }

    @Test
    fun `worker should fail when Sendsay SDK is not initialized`() {
        assertEquals(WorkInfo.State.FAILED, executeWork().state)
    }

    @Test
    fun `worker should flush in periodic mode`() {
        mockkConstructorFix(FlushManagerImpl::class)
        Sendsay.flushMode = FlushMode.PERIOD
        Sendsay.init(ApplicationProvider.getApplicationContext(), SendsayConfiguration(projectToken = "mock-token"))

        var flushCalled = false
        val slot = slot<FlushFinishedCallback>()
        every { anyConstructed<FlushManagerImpl>().flushData(capture(slot)) } answers {
            flushCalled = true
            slot.captured.invoke(Result.success(Unit))
        }

        assertEquals(WorkInfo.State.SUCCEEDED, executeWork().state)
        assertTrue(flushCalled)
    }

    @Test
    fun `worker should fail and not flush in manual mode`() {
        mockkConstructorFix(FlushManagerImpl::class)
        Sendsay.flushMode = FlushMode.MANUAL
        Sendsay.init(ApplicationProvider.getApplicationContext(), SendsayConfiguration(projectToken = "mock-token"))

        var flushCalled = false
        val slot = slot<FlushFinishedCallback>()
        every { anyConstructed<FlushManagerImpl>().flushData(capture(slot)) } answers {
            flushCalled = true
            slot.captured.invoke(Result.success(Unit))
        }

        assertEquals(WorkInfo.State.FAILED, executeWork().state)
        assertFalse(flushCalled)
    }

    @Test
    fun `should start periodic flush when flushMode is period`() {
        Sendsay.flushMode = FlushMode.PERIOD
        Sendsay.init(ApplicationProvider.getApplicationContext(), SendsayConfiguration(projectToken = "mock-token"))

        assertNotNull(getUniqueWorkInfo(SendsayPeriodicFlushWorker.WORK_NAME))
    }

    @Test
    fun `should not start periodic flush when flushMode isn't period`() {
        Sendsay.flushMode = FlushMode.MANUAL
        Sendsay.init(ApplicationProvider.getApplicationContext(), SendsayConfiguration(projectToken = "mock-token"))

        FlushMode.values().forEach { flushMode ->
            if (flushMode == FlushMode.PERIOD) return@forEach
            Sendsay.flushMode = flushMode
            assertNull(getUniqueWorkInfo(SendsayPeriodicFlushWorker.WORK_NAME))
        }
    }

    @Test
    fun `should stop periodic flush`() {
        Sendsay.flushMode = FlushMode.PERIOD
        Sendsay.init(ApplicationProvider.getApplicationContext(), SendsayConfiguration(projectToken = "mock-token"))

        assertNotNull(getUniqueWorkInfo(SendsayPeriodicFlushWorker.WORK_NAME))
        assertEquals(getUniqueWorkInfo(SendsayPeriodicFlushWorker.WORK_NAME)?.state, WorkInfo.State.ENQUEUED)

        Sendsay.flushMode = FlushMode.IMMEDIATE

        assertEquals(getUniqueWorkInfo(SendsayPeriodicFlushWorker.WORK_NAME)?.state, WorkInfo.State.CANCELLED)
    }

    @Test
    fun `should run flush periodically`() {
        val testDriver = WorkManagerTestInitHelper.getTestDriver(ApplicationProvider.getApplicationContext())!!

        mockkConstructorFix(FlushManagerImpl::class)
        var flushCalls = 0
        val slot = slot<FlushFinishedCallback>()
        every { anyConstructed<FlushManagerImpl>().flushData(capture(slot)) } answers {
            flushCalls++
            slot.captured.invoke(Result.success(Unit))
        }

        Sendsay.flushMode = FlushMode.PERIOD
        Sendsay.init(ApplicationProvider.getApplicationContext(), SendsayConfiguration(projectToken = "mock-token"))
        flushCalls = 0

        for (i in 1..5) {
            testDriver.setAllConstraintsMet(getUniqueWorkInfo(SendsayPeriodicFlushWorker.WORK_NAME)!!.id)
            testDriver.setPeriodDelayMet(getUniqueWorkInfo(SendsayPeriodicFlushWorker.WORK_NAME)!!.id)
            assertEquals(i, flushCalls)
        }
    }

    @Test // it's not possible to check periodicity of tasks in work manager, let's just check new task was queued
    fun `should enqueue new task when flushPeriod changes`() {
        Sendsay.flushMode = FlushMode.PERIOD
        Sendsay.init(ApplicationProvider.getApplicationContext(), SendsayConfiguration(projectToken = "mock-token"))
        val oldTaskId = getUniqueWorkInfo(SendsayPeriodicFlushWorker.WORK_NAME)!!.id
        Sendsay.flushPeriod = FlushPeriod(1, TimeUnit.DAYS)
        val newTaskId = getUniqueWorkInfo(SendsayPeriodicFlushWorker.WORK_NAME)!!.id
        assertNotEquals(oldTaskId, newTaskId)
    }
}
