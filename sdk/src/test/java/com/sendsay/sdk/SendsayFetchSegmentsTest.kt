package ru.sendsay.sdk

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import ru.sendsay.sdk.manager.FetchManagerImpl
import ru.sendsay.sdk.manager.SegmentsManagerImpl
import ru.sendsay.sdk.models.SendsayConfiguration
import ru.sendsay.sdk.models.FlushMode
import ru.sendsay.sdk.models.Result
import ru.sendsay.sdk.models.SegmentTest
import ru.sendsay.sdk.models.SegmentationCategories
import ru.sendsay.sdk.repository.CustomerIdsRepositoryImpl
import ru.sendsay.sdk.testutil.SendsaySDKTest
import ru.sendsay.sdk.testutil.waitForIt
import io.mockk.every
import kotlin.test.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class SendsayFetchSegmentsTest : SendsaySDKTest() {

    internal val ACCEPTED_INIT_DURATION_MILLIS = 2000L

    @Before
    fun before() {
        mockkConstructorFix(FetchManagerImpl::class) {
            every { anyConstructed<FetchManagerImpl>().fetchSegments(any(), any(), any(), any()) }
        }
        every { anyConstructed<FetchManagerImpl>().linkCustomerIdsSync(any(), any()) } answers {
            Result<Any?>(true, null)
        }
        mockkConstructorFix(CustomerIdsRepositoryImpl::class)
        every { anyConstructed<CustomerIdsRepositoryImpl>().get() } returns SegmentTest.getCustomerIds()
        skipInstallEvent()
        val context = ApplicationProvider.getApplicationContext<Context>()
        val configuration = SendsayConfiguration(
            projectToken = "mock-token",
            automaticSessionTracking = false,
            authorization = "Token mock-auth"
        )
        Sendsay.flushMode = FlushMode.MANUAL
        Sendsay.init(context, configuration)
    }

    @Test
    fun `should fetch some segmentation data`() {
        every { anyConstructed<FetchManagerImpl>().fetchSegments(any(), any(), any(), any()) } answers {
            arg<(Result<SegmentationCategories>) -> Unit>(2).invoke(Result(
                true,
                SegmentTest.buildSingleSegmentWithData(mapOf("prop" to "mock-val"))
            ))
        }
        waitForIt(timeoutMS = SegmentsManagerImpl.CHECK_DEBOUNCE_MILLIS + ACCEPTED_INIT_DURATION_MILLIS) { done ->
            Sendsay.getSegments("discovery") { segments ->
                assertEquals(1, segments.size)
                assertEquals("mock-val", segments[0]["prop"])
                done()
            }
        }
    }

    @Test
    fun `should fetch no segmentation data for different category`() {
        every { anyConstructed<FetchManagerImpl>().fetchSegments(any(), any(), any(), any()) } answers {
            arg<(Result<SegmentationCategories>) -> Unit>(2).invoke(Result(
                true,
                SegmentTest.buildSingleSegmentWithData(mapOf("prop" to "mock-val"))
            ))
        }
        waitForIt(timeoutMS = SegmentsManagerImpl.CHECK_DEBOUNCE_MILLIS + ACCEPTED_INIT_DURATION_MILLIS) { done ->
            Sendsay.getSegments("content") { segments ->
                assertEquals(0, segments.size)
                done()
            }
        }
    }

    @Test
    fun `should left no callback instance after successful get`() {
        every { anyConstructed<FetchManagerImpl>().fetchSegments(any(), any(), any(), any()) } answers {
            arg<(Result<SegmentationCategories>) -> Unit>(2).invoke(Result(
                true,
                SegmentTest.buildSingleSegmentWithData(mapOf("prop" to "mock-val"))
            ))
        }
        waitForIt(timeoutMS = SegmentsManagerImpl.CHECK_DEBOUNCE_MILLIS + ACCEPTED_INIT_DURATION_MILLIS) { done ->
            Sendsay.getSegments("discovery") { segments ->
                done()
            }
        }
        assertEquals(0, Sendsay.segmentationDataCallbacks.size)
    }

    @Test
    fun `should left no callback instance after empty get`() {
        every { anyConstructed<FetchManagerImpl>().fetchSegments(any(), any(), any(), any()) } answers {
            arg<(Result<SegmentationCategories>) -> Unit>(2).invoke(Result(
                true,
                SegmentTest.buildSingleSegmentWithData(mapOf("prop" to "mock-val"))
            ))
        }
        waitForIt(timeoutMS = SegmentsManagerImpl.CHECK_DEBOUNCE_MILLIS + ACCEPTED_INIT_DURATION_MILLIS) { done ->
            Sendsay.getSegments("content") { segments ->
                done()
            }
        }
        assertEquals(0, Sendsay.segmentationDataCallbacks.size)
    }
}
