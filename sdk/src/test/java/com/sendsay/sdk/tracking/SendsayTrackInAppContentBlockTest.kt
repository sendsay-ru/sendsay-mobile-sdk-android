package com.sendsay.sdk.tracking

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.sendsay.sdk.Sendsay
import com.sendsay.sdk.manager.EventManagerImpl
import com.sendsay.sdk.manager.FetchManagerImpl
import com.sendsay.sdk.manager.InAppContentBlockManagerImpl
import com.sendsay.sdk.manager.InAppContentBlockManagerImplTest
import com.sendsay.sdk.mockkConstructorFix
import com.sendsay.sdk.models.CustomerIds
import com.sendsay.sdk.models.Event
import com.sendsay.sdk.models.EventType
import com.sendsay.sdk.models.SendsayConfiguration
import com.sendsay.sdk.models.FlushMode
import com.sendsay.sdk.models.InAppContentBlock
import com.sendsay.sdk.models.InAppContentBlockPersonalizedData
import com.sendsay.sdk.models.Result
import com.sendsay.sdk.repository.CustomerIdsRepositoryImpl
import com.sendsay.sdk.testutil.SendsaySDKTest
import com.sendsay.sdk.testutil.componentForTesting
import com.sendsay.sdk.util.backgroundThreadDispatcher
import com.sendsay.sdk.util.mainThreadDispatcher
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.slot
import io.mockk.verify
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.LooperMode

@RunWith(RobolectricTestRunner::class)
internal class SendsayTrackInAppContentBlockTest : SendsaySDKTest() {
    @Before
    fun before() {
        mockkConstructorFix(EventManagerImpl::class) {
            every { anyConstructed<EventManagerImpl>().addEventToQueue(any(), any(), any()) }
        }
        mockkConstructorFix(FetchManagerImpl::class) {
            every { anyConstructed<FetchManagerImpl>().fetchSegments(any(), any(), any(), any()) }
        }
        mockkConstructorFix(CustomerIdsRepositoryImpl::class)
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

    @Before
    fun overrideThreadBehaviour() {
        mainThreadDispatcher = CoroutineScope(Dispatchers.Main)
        backgroundThreadDispatcher = CoroutineScope(Dispatchers.Main)
    }

    @After
    fun restoreThreadBehaviour() {
        mainThreadDispatcher = CoroutineScope(Dispatchers.Main)
        backgroundThreadDispatcher = CoroutineScope(Dispatchers.Default)
    }

    @Test
    @LooperMode(LooperMode.Mode.LEGACY)
    fun `should track action message for original InAppContentBlock`() {
        val eventSlot = slot<Event>()
        val eventTypeSlot = slot<EventType>()
        every {
            anyConstructed<EventManagerImpl>().addEventToQueue(capture(eventSlot), capture(eventTypeSlot), any())
        } just Runs
        val firstCustomerIds = CustomerIds("brownie").withId("registered", "test1")
        every { anyConstructed<CustomerIdsRepositoryImpl>().get() } returns firstCustomerIds
        val placeholderId = "ph1"
        val messageId = "id1"
        every {
            anyConstructed<FetchManagerImpl>().fetchStaticInAppContentBlocks(any(), any(), any())
        } answers {
            arg<(Result<ArrayList<InAppContentBlock>?>) -> Unit>(1).invoke(Result(true, arrayListOf(
                InAppContentBlockManagerImplTest.buildMessage(
                    messageId,
                    placeholders = listOf(placeholderId),
                    dateFilter = null
                )
            )))
        }
        every {
            anyConstructed<FetchManagerImpl>().fetchPersonalizedContentBlocks(any(), any(), any(), any(), any())
        } answers {
            arg<(Result<ArrayList<InAppContentBlockPersonalizedData>?>) -> Unit>(3).invoke(Result(true, arrayListOf(
                // htmlContent
                InAppContentBlockManagerImplTest.buildMessageData(
                    messageId,
                    type = "html",
                    hasTrackingConsent = true,
                    data = mapOf(
                        "html" to InAppContentBlockManagerImplTest.buildHtmlMessageContent()
                    )
                )
            )))
        }
        // turn init-load on
        every { anyConstructed<InAppContentBlockManagerImpl>().loadInAppContentBlockPlaceholders() } answers {
            callOriginal()
        }
        Sendsay.componentForTesting.inAppContentBlockManager.loadInAppContentBlockPlaceholders()
        assertTrue(
            (Sendsay.componentForTesting.inAppContentBlockManager as InAppContentBlockManagerImpl)
                .contentBlocksData
                .isNotEmpty()
        )
        // get view and simulate action click
        val view = Sendsay.getInAppContentBlocksPlaceholder(placeholderId, ApplicationProvider.getApplicationContext())
        assertNotNull(view)
        val controller = view.controller
        assertNotNull(controller)
        // validate show event
        verify(exactly = 1) {
            anyConstructed<EventManagerImpl>().addEventToQueue(any(), any(), any())
        }
        controller.onUrlClick("https://sendsay.com")
        // validate click event and show next
        verify(exactly = 2) {
            anyConstructed<EventManagerImpl>().addEventToQueue(any(), any(), any())
        }
        assertEquals("banner", eventSlot.captured.type)
        assertEquals(EventType.BANNER, eventTypeSlot.captured)
        assertEquals(firstCustomerIds.toHashMap(), eventSlot.captured.customerIds)
        val secondCustomerIds = CustomerIds("brownie2").withId("registered", "test2")
        every { anyConstructed<CustomerIdsRepositoryImpl>().get() } returns secondCustomerIds
        Sendsay.identifyCustomer(secondCustomerIds, hashMapOf())
        // validate that customerIDs not changed
        controller.onUrlClick("https://sendsay.com")
        assertEquals("banner", eventSlot.captured.type)
        assertEquals(EventType.BANNER, eventTypeSlot.captured)
        assertEquals(firstCustomerIds.toHashMap(), eventSlot.captured.customerIds)
    }

    @Test
    @LooperMode(LooperMode.Mode.LEGACY)
    fun `should NOT track action message for FALSE tracking consent`() {
        every {
            anyConstructed<EventManagerImpl>().addEventToQueue(any(), any(), any())
        } just Runs
        val firstCustomerIds = CustomerIds("brownie").withId("registered", "test1")
        every { anyConstructed<CustomerIdsRepositoryImpl>().get() } returns firstCustomerIds
        val placeholderId = "ph1"
        val messageId = "id1"
        every {
            anyConstructed<FetchManagerImpl>().fetchStaticInAppContentBlocks(any(), any(), any())
        } answers {
            arg<(Result<ArrayList<InAppContentBlock>?>) -> Unit>(1).invoke(Result(true, arrayListOf(
                InAppContentBlockManagerImplTest.buildMessage(
                    messageId,
                    placeholders = listOf(placeholderId),
                    dateFilter = null
                )
            )))
        }
        every {
            anyConstructed<FetchManagerImpl>().fetchPersonalizedContentBlocks(any(), any(), any(), any(), any())
        } answers {
            arg<(Result<ArrayList<InAppContentBlockPersonalizedData>?>) -> Unit>(3).invoke(Result(true, arrayListOf(
                // htmlContent
                InAppContentBlockManagerImplTest.buildMessageData(
                    messageId,
                    type = "html",
                    hasTrackingConsent = false,
                    data = mapOf(
                        "html" to InAppContentBlockManagerImplTest.buildHtmlMessageContent()
                    )
                )
            )))
        }
        // turn init-load on
        every {
            anyConstructed<InAppContentBlockManagerImpl>().loadInAppContentBlockPlaceholders()
        } answers {
            callOriginal()
        }
        Sendsay.componentForTesting.inAppContentBlockManager.loadInAppContentBlockPlaceholders()
        assertTrue(
            (Sendsay.componentForTesting.inAppContentBlockManager as InAppContentBlockManagerImpl)
                .contentBlocksData
                .isNotEmpty()
        )
        // get view and simulate action click
        val view = Sendsay.getInAppContentBlocksPlaceholder(placeholderId, ApplicationProvider.getApplicationContext())
        assertNotNull(view)
        val controller = view.controller
        assertNotNull(controller)
        controller.onUrlClick("https://sendsay.com")
        // validate
        verify(exactly = 0) {
            anyConstructed<EventManagerImpl>().addEventToQueue(any(), any(), true)
        }
    }

    @Test
    @LooperMode(LooperMode.Mode.LEGACY)
    fun `should track action message for TRUE tracking consent`() {
        every {
            anyConstructed<EventManagerImpl>().addEventToQueue(any(), any(), any())
        } just Runs
        val firstCustomerIds = CustomerIds("brownie").withId("registered", "test1")
        every { anyConstructed<CustomerIdsRepositoryImpl>().get() } returns firstCustomerIds
        val placeholderId = "ph1"
        val messageId = "id1"
        every {
            anyConstructed<FetchManagerImpl>().fetchStaticInAppContentBlocks(any(), any(), any())
        } answers {
            arg<(Result<ArrayList<InAppContentBlock>?>) -> Unit>(1).invoke(Result(true, arrayListOf(
                InAppContentBlockManagerImplTest.buildMessage(
                    messageId,
                    placeholders = listOf(placeholderId),
                    dateFilter = null
                )
            )))
        }
        every {
            anyConstructed<FetchManagerImpl>().fetchPersonalizedContentBlocks(any(), any(), any(), any(), any())
        } answers {
            arg<(Result<ArrayList<InAppContentBlockPersonalizedData>?>) -> Unit>(3)
                .invoke(Result(true, arrayListOf(
                    // htmlContent
                    InAppContentBlockManagerImplTest.buildMessageData(
                        messageId,
                        type = "html",
                        hasTrackingConsent = true,
                        data = mapOf(
                            "html" to InAppContentBlockManagerImplTest.buildHtmlMessageContent()
                        )
                    )
            )))
        }
        // turn init-load on
        every {
            anyConstructed<InAppContentBlockManagerImpl>().loadInAppContentBlockPlaceholders()
        } answers {
            callOriginal()
        }
        Sendsay.componentForTesting.inAppContentBlockManager.loadInAppContentBlockPlaceholders()
        assertTrue(
            (Sendsay.componentForTesting.inAppContentBlockManager as InAppContentBlockManagerImpl)
                .contentBlocksData
                .isNotEmpty()
        )
        // get view and simulate action click
        val view = Sendsay.getInAppContentBlocksPlaceholder(placeholderId, ApplicationProvider.getApplicationContext())
        assertNotNull(view)
        val controller = view.controller
        assertNotNull(controller)
        // validate show
        verify(exactly = 1) {
            anyConstructed<EventManagerImpl>().addEventToQueue(any(), any(), true)
        }
        controller.onUrlClick("https://sendsay.com")
        // validate click and show next
        verify(exactly = 2) {
            anyConstructed<EventManagerImpl>().addEventToQueue(any(), any(), true)
        }
    }

    @Test
    @LooperMode(LooperMode.Mode.LEGACY)
    fun `should DO track action message for FALSE tracking consent but forced action`() {
        every {
            anyConstructed<EventManagerImpl>().addEventToQueue(any(), any(), any())
        } just Runs
        val firstCustomerIds = CustomerIds("brownie").withId("registered", "test1")
        every { anyConstructed<CustomerIdsRepositoryImpl>().get() } returns firstCustomerIds
        val placeholderId = "ph1"
        val messageId = "id1"
        every {
            anyConstructed<FetchManagerImpl>().fetchStaticInAppContentBlocks(any(), any(), any())
        } answers {
            arg<(Result<ArrayList<InAppContentBlock>?>) -> Unit>(1).invoke(Result(true, arrayListOf(
                InAppContentBlockManagerImplTest.buildMessage(
                    messageId,
                    placeholders = listOf(placeholderId),
                    dateFilter = null
                )
            )))
        }
        every {
            anyConstructed<FetchManagerImpl>().fetchPersonalizedContentBlocks(any(), any(), any(), any(), any())
        } answers {
            arg<(Result<ArrayList<InAppContentBlockPersonalizedData>?>) -> Unit>(3)
                .invoke(Result(true, arrayListOf(
                    // htmlContent
                    InAppContentBlockManagerImplTest.buildMessageData(
                        messageId,
                        type = "html",
                        hasTrackingConsent = false,
                        data = mapOf(
                            "html" to InAppContentBlockManagerImplTest.buildHtmlMessageContent()
                        )
                    )
            )))
        }
        // turn init-load on
        every {
            anyConstructed<InAppContentBlockManagerImpl>().loadInAppContentBlockPlaceholders()
        } answers {
            callOriginal()
        }
        Sendsay.componentForTesting.inAppContentBlockManager.loadInAppContentBlockPlaceholders()
        assertTrue(
            (Sendsay.componentForTesting.inAppContentBlockManager as InAppContentBlockManagerImpl)
                .contentBlocksData
                .isNotEmpty()
        )
        // get view and simulate action click
        val view = Sendsay.getInAppContentBlocksPlaceholder(placeholderId, ApplicationProvider.getApplicationContext())
        assertNotNull(view)
        val controller = view.controller
        assertNotNull(controller)
        // validate show without tracking
        verify(exactly = 1) {
            anyConstructed<EventManagerImpl>().addEventToQueue(any(), any(), false)
        }
        controller.onUrlClick("https://sendsay.com?xnpe_force_track=true")
        // validate click with forced tracking
        verify(exactly = 1) {
            anyConstructed<EventManagerImpl>().addEventToQueue(any(), any(), true)
        }
    }
}
