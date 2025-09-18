package ru.sendsay.sdk.manager

import androidx.test.core.app.ApplicationProvider
import ru.sendsay.sdk.Sendsay
import ru.sendsay.sdk.mockkConstructorFix
import ru.sendsay.sdk.models.Consent
import ru.sendsay.sdk.models.Constants
import ru.sendsay.sdk.models.CustomerIds
import ru.sendsay.sdk.models.CustomerRecommendation
import ru.sendsay.sdk.models.EventType
import ru.sendsay.sdk.models.SendsayConfiguration
import ru.sendsay.sdk.models.SendsayProject
import ru.sendsay.sdk.models.ExportedEvent
import ru.sendsay.sdk.models.FlushMode
import ru.sendsay.sdk.models.InAppContentBlock
import ru.sendsay.sdk.models.InAppContentBlockPersonalizedData
import ru.sendsay.sdk.models.InAppMessage
import ru.sendsay.sdk.models.InAppMessageTest
import ru.sendsay.sdk.models.MessageItem
import ru.sendsay.sdk.models.PropertiesList
import ru.sendsay.sdk.models.Result
import ru.sendsay.sdk.models.SegmentationCategories
import ru.sendsay.sdk.models.eventfilter.EventFilter
import ru.sendsay.sdk.network.SendsayServiceImpl
import ru.sendsay.sdk.repository.InAppMessageBitmapCacheImpl
import ru.sendsay.sdk.repository.InAppMessagesCacheImpl
import ru.sendsay.sdk.services.SendsayContextProvider
import ru.sendsay.sdk.testutil.SendsayMockServer
import ru.sendsay.sdk.testutil.SendsaySDKTest
import ru.sendsay.sdk.testutil.MockFile
import ru.sendsay.sdk.testutil.runInSingleThread
import ru.sendsay.sdk.util.Logger
import ru.sendsay.sdk.util.backgroundThreadDispatcher
import ru.sendsay.sdk.util.mainThreadDispatcher
import ru.sendsay.sdk.util.runOnBackgroundThread
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.slot
import io.mockk.verify
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mock.HttpCode
import okhttp3.mock.MockInterceptor
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.LooperMode

@RunWith(RobolectricTestRunner::class)
internal class InAppMessageManagerFlowTest : SendsaySDKTest() {

    @Before
    fun disableFetchData() {
        every { anyConstructed<FetchManagerImpl>().fetchConsents(any(), any(), any()) } answers {
            secondArg<(Result<ArrayList<Consent>>) -> Unit>().invoke(
                Result(true, arrayListOf())
            )
        }
        every { anyConstructed<FetchManagerImpl>().fetchRecommendation(any(), any(), any(), any()) } answers {
            thirdArg<(Result<ArrayList<CustomerRecommendation>>) -> Unit>().invoke(
                Result(true, arrayListOf())
            )
        }
        every { anyConstructed<FetchManagerImpl>().fetchAppInbox(any(), any(), any(), any(), any()) } answers {
            arg<(Result<ArrayList<MessageItem>?>) -> Unit>(3).invoke(
                Result(true, arrayListOf())
            )
        }
        every {
            anyConstructed<FetchManagerImpl>().fetchPersonalizedContentBlocks(any(), any(), any(), any(), any())
        } answers {
            arg<(Result<ArrayList<InAppContentBlockPersonalizedData>?>) -> Unit>(3).invoke(
                Result(true, arrayListOf())
            )
        }
        every { anyConstructed<FetchManagerImpl>().fetchStaticInAppContentBlocks(any(), any(), any()) } answers {
            arg<(Result<ArrayList<InAppContentBlock>?>) -> Unit>(1).invoke(
                Result(true, arrayListOf())
            )
        }
        every {
            anyConstructed<FetchManagerImpl>().markAppInboxAsRead(any(), any(), any(), any(), any(), any())
        } answers {
            arg<(Result<Any?>) -> Unit>(4).invoke(
                Result(true, null)
            )
        }
        every { anyConstructed<FetchManagerImpl>().fetchInAppMessages(any(), any(), any(), any()) } answers {
            arg<(Result<ArrayList<InAppMessage>>) -> Unit>(2).invoke(
                Result(true, arrayListOf())
            )
        }
        every { anyConstructed<FetchManagerImpl>().fetchSegments(any(), any(), any(), any()) } answers {
            arg<(Result<SegmentationCategories>) -> Unit>(2).invoke(
                Result(true, SegmentationCategories())
            )
        }
        mockkConstructorFix(FcmManagerImpl::class) {
            every { anyConstructed<FcmManagerImpl>().trackToken(any(), any(), any()) }
        }
        mockkConstructorFix(TimeLimitedFcmManagerImpl::class) {
            every { anyConstructed<TimeLimitedFcmManagerImpl>().trackToken(any(), any(), any()) }
        }
        every { anyConstructed<FcmManagerImpl>().trackToken(any(), any(), any()) } just Runs
        every { anyConstructed<TimeLimitedFcmManagerImpl>().trackToken(any(), any(), any()) } just Runs
    }

    @Before
    fun disableRealUpload() {
        val mockServer = SendsayMockServer.createServer()
        val mockUrl = mockServer.url("/").toString()
        val mockInterceptor = MockInterceptor().apply {
            addRule()
                .get().or().post().or().put()
                .url(mockUrl)
                .anyTimes()
                .respond(HttpCode.HTTP_200_OK, null)
        }
        val okHttpClient = OkHttpClient
            .Builder()
            .addInterceptor(mockInterceptor)
            .build()
        every { anyConstructed<SendsayServiceImpl>().doPost(any(), any<String>(), any()) } answers {
            okHttpClient.newCall(Request.Builder().url(mockUrl).get().build())
        }
    }

    @Before
    fun prepareInAppMocks() {
        mockkConstructorFix(InAppMessageBitmapCacheImpl::class) {
            every { anyConstructed<InAppMessageBitmapCacheImpl>().preload(any(), any()) }
        }
        mockkConstructorFix(InAppMessagesCacheImpl::class)
    }

    @Before
    fun disableSegmentsManager() {
        mockkConstructorFix(SegmentsManagerImpl::class)
        every { anyConstructed<SegmentsManagerImpl>().onEventUploaded(any()) } just Runs
        every { anyConstructed<SegmentsManagerImpl>().onSdkInit() } just Runs
        every { anyConstructed<SegmentsManagerImpl>().onCallbackAdded(any()) } just Runs
    }

    @Before
    fun disableInAppContentBlockManager() {
        mockkConstructorFix(InAppContentBlockManagerImpl::class)
        every { anyConstructed<InAppContentBlockManagerImpl>().onEventCreated(any(), any()) } just Runs
    }

    @Test
    fun `should preload and show for session_start for IMMEDIATE flush with delay`() {
        SendsayContextProvider.applicationIsForeground = true
        val threadAwaitSeconds = 10L
        Sendsay.flushMode = FlushMode.IMMEDIATE
        Sendsay.loggerLevel = Logger.Level.VERBOSE
        // allow process
        every {
            anyConstructed<InAppMessageManagerImpl>().detectReloadMode(any(), any(), any())
        } answers {
            callOriginal()
        }
        every { anyConstructed<InAppMessageManagerImpl>().pickAndShowMessage() } answers { callOriginal() }
        // disabled real In-app fetch
        val pendingMessage = InAppMessageTest.buildInAppMessage(
            trigger = EventFilter(Constants.EventTypes.sessionStart, arrayListOf()),
            imageUrl = "pending_image_url",
            priority = null,
            id = "12345"
        )
        prepareMessagesMocks(arrayListOf(pendingMessage))
        initSdk()
        // ensure that identifyCustomer upload is done after session_start process
        val sessionStartProcessed = CountDownLatch(1)
        val identifyCustomerProcessed = CountDownLatch(1)
        every {
            // only identifyCustomer could invoke this
            anyConstructed<InAppMessageManagerImpl>().onEventUploaded(any())
        } answers {
            val isCustomerUploaded = firstArg<ExportedEvent>().sdkEventType == EventType.TRACK_CUSTOMER.name
            if (isCustomerUploaded) {
                assertTrue(sessionStartProcessed.await(threadAwaitSeconds, TimeUnit.SECONDS))
            }
            callOriginal()
            if (isCustomerUploaded) {
                identifyCustomerProcessed.countDown()
            }
        }
        every {
            anyConstructed<InAppMessageManagerImpl>().onEventCreated(any(), any())
        } answers {
            callOriginal()
            if (arg<EventType>(1) == EventType.SESSION_START) {
                sessionStartProcessed.countDown()
            }
        }
        // invoke test scenario
        val customerIdsMap: HashMap<String, String?> = hashMapOf("registered" to "test001")
        identifyCustomerForTest(customerIdsMap)
        Sendsay.trackSessionStart()
        assertTrue(sessionStartProcessed.await(threadAwaitSeconds, TimeUnit.SECONDS))
        assertTrue(identifyCustomerProcessed.await(threadAwaitSeconds, TimeUnit.SECONDS))
        verify(exactly = 1) {
            anyConstructed<InAppMessageManagerImpl>().show(pendingMessage)
        }
    }

    @Test
    @LooperMode(LooperMode.Mode.LEGACY)
    fun `should preload and show for each session_start`() {
        SendsayContextProvider.applicationIsForeground = true
        mainThreadDispatcher = CoroutineScope(Dispatchers.Main)
        backgroundThreadDispatcher = CoroutineScope(Dispatchers.Main)
        Sendsay.flushMode = FlushMode.MANUAL
        // allow process
        every {
            anyConstructed<InAppMessageManagerImpl>().detectReloadMode(any(), any(), any())
        } answers {
            callOriginal()
        }
        every { anyConstructed<InAppMessageManagerImpl>().pickAndShowMessage() } answers { callOriginal() }
        // disabled real In-app fetch
        val pendingMessage = InAppMessageTest.buildInAppMessage(
            trigger = EventFilter(Constants.EventTypes.sessionStart, arrayListOf()),
            imageUrl = "pending_image_url",
            priority = null,
            id = "12345"
        )
        prepareMessagesMocks(arrayListOf(pendingMessage))
        initSdk()
        simulateCustomerUsage(hashMapOf("registered" to "test001"))
        simulateCustomerUsage(hashMapOf("registered" to "test002"))
        simulateCustomerUsage(hashMapOf("registered" to "test003"))
        verify(exactly = 3) {
            anyConstructed<InAppMessageManagerImpl>().show(any())
        }
    }

    @After
    fun resetThreadBehaviour() {
        mainThreadDispatcher = CoroutineScope(Dispatchers.Main)
        backgroundThreadDispatcher = CoroutineScope(Dispatchers.Default)
    }

    @Test
    fun `should show message only for last identifyCustomer for MANUAL flush`() {
        SendsayContextProvider.applicationIsForeground = true
        Sendsay.flushMode = FlushMode.MANUAL
        // allow process
        every {
            anyConstructed<InAppMessageManagerImpl>().detectReloadMode(any(), any(), any())
        } answers {
            callOriginal()
        }
        every { anyConstructed<InAppMessageManagerImpl>().pickAndShowMessage() } answers { callOriginal() }
        prepareMessagesMocks(arrayListOf())
        initSdk()

        // login customerA, message pendingMessageA has to be loaded
        val pendingMessageA = InAppMessageTest.buildInAppMessage(
            trigger = EventFilter(Constants.EventTypes.sessionStart, arrayListOf()),
            imageUrl = "pending_image_url",
            priority = null,
            id = "12345A"
        )
        Sendsay.anonymize()
        prepareMessagesMocks(arrayListOf(pendingMessageA))
        identifyCustomerForTest(hashMapOf("registered" to "customerA"))
        // login customerB, message pendingMessageB has to be loaded
        val pendingMessageB = InAppMessageTest.buildInAppMessage(
            trigger = EventFilter(Constants.EventTypes.sessionStart, arrayListOf()),
            imageUrl = "pending_image_url",
            priority = null,
            id = "12345B"
        )
        Sendsay.anonymize()
        prepareMessagesMocks(arrayListOf(pendingMessageB))
        identifyCustomerForTest(hashMapOf("registered" to "customerB"))
        // check that pendingMessageB is going to show for customerB
        val messageSlot = slot<InAppMessage>()
        every { anyConstructed<InAppMessageManagerImpl>().show(capture(messageSlot)) } just Runs
        Sendsay.trackSessionStart()
        Thread.sleep(2000)
        assert(messageSlot.isCaptured)
        assertNotNull(messageSlot.captured)
        assertEquals(pendingMessageB.id, messageSlot.captured.id)
    }

    @Test
    fun `should try to show message for unloaded image`() {
        SendsayContextProvider.applicationIsForeground = true
        Sendsay.flushMode = FlushMode.MANUAL
        // allow process
        every {
            anyConstructed<InAppMessageManagerImpl>().detectReloadMode(any(), any(), any())
        } answers {
            callOriginal()
        }
        every { anyConstructed<InAppMessageManagerImpl>().pickAndShowMessage() } answers { callOriginal() }
        // disabled real In-app fetch
        val pendingMessage = InAppMessageTest.buildInAppMessage(
            trigger = EventFilter(Constants.EventTypes.sessionStart, arrayListOf()),
            imageUrl = "pending_image_url",
            priority = null,
            id = "12345"
        )
        prepareMessagesMocks(arrayListOf(pendingMessage))
        disableBitmapCache()
        initSdk()
        simulateCustomerUsage(hashMapOf("registered" to "test001"))
        verify(exactly = 1) {
            anyConstructed<InAppMessageManagerImpl>().trackError(pendingMessage, "Images has not been preloaded")
        }
        verify(exactly = 0) {
            anyConstructed<InAppMessageManagerImpl>().show(any())
        }
    }

    @Test
    fun `should not show message for session_start if another customer identifies`() {
        val threadAwaitSeconds = 5L
        Sendsay.flushMode = FlushMode.MANUAL
        Sendsay.loggerLevel = Logger.Level.VERBOSE
        SendsayContextProvider.applicationIsForeground = true
        // allow process
        every {
            anyConstructed<InAppMessageManagerImpl>().detectReloadMode(any(), any(), any())
        } answers {
            callOriginal()
        }
        every { anyConstructed<InAppMessageManagerImpl>().pickAndShowMessage() } answers { callOriginal() }
        // disabled real In-app fetch
        val pendingMessage = InAppMessageTest.buildInAppMessage(
            trigger = EventFilter(Constants.EventTypes.sessionStart, arrayListOf()),
            imageUrl = "pending_image_url",
            priority = null,
            id = "12345"
        )
        prepareMessagesMocks(arrayListOf(pendingMessage))
        initSdk()
        identifyCustomerForTest(hashMapOf("registered" to "test001"))
        // ensure that session_start picks message but image load finishes after another customer login
        val sessionStartProcessed = CountDownLatch(1)
        val identifyCustomerProcessed = CountDownLatch(1)
        every {
            anyConstructed<InAppMessageManagerImpl>().preloadAndShow(any(), any())
        } answers {
            Logger.e(this, "[InApp] SessionStart preloadAndShow occurs")
            assertTrue(identifyCustomerProcessed.await(threadAwaitSeconds, TimeUnit.SECONDS))
            Logger.e(this, "[InApp] SessionStart waiting for identifyCustomerProcessed ends")
            sessionStartProcessed.countDown()
            callOriginal()
        }
        every {
            anyConstructed<InAppMessageManagerImpl>().onEventCreated(any(), any())
        } answers {
            if (arg<EventType>(1) == EventType.TRACK_CUSTOMER) {
                Logger.e(this, "[InApp] IdentifyCustomer onEventCreated occurs")
                identifyCustomerProcessed.countDown()
            }
            callOriginal()
        }
        // invoke test scenario
        Sendsay.trackSessionStart()
        runOnBackgroundThread {
            Sendsay.anonymize()
            identifyCustomerForTest(hashMapOf("registered" to "test002"))
        }
        assertTrue(sessionStartProcessed.await(threadAwaitSeconds, TimeUnit.SECONDS))
        assertTrue(identifyCustomerProcessed.await(threadAwaitSeconds, TimeUnit.SECONDS))
        verify(exactly = 0) {
            anyConstructed<InAppMessageManagerImpl>().show(pendingMessage)
        }
    }

    @Test
    fun `should not show message after control group message show was tracked`() = runInSingleThread { awaitThreads ->
        Sendsay.flushMode = FlushMode.MANUAL
        Sendsay.loggerLevel = Logger.Level.VERBOSE
        SendsayContextProvider.applicationIsForeground = true
        // allow process
        every {
            anyConstructed<InAppMessageManagerImpl>().detectReloadMode(any(), any(), any())
        } answers {
            callOriginal()
        }
        every { anyConstructed<InAppMessageManagerImpl>().pickAndShowMessage() } answers { callOriginal() }
        val controlGroupMessage = InAppMessageTest.getInAppMessage(
            trigger = EventFilter(Constants.EventTypes.sessionStart, arrayListOf()),
            id = "12345",
            variantId = -1,
            variantName = "Control Group",
            payloadHtml = null,
            payload = null
        )
        prepareMessagesMocks(arrayListOf(controlGroupMessage))
        initSdk()
        // invoke test scenario
        Sendsay.trackSessionStart()
        awaitThreads()
        verify {
            // session_start event handling
            anyConstructed<InAppMessageManagerImpl>().onEventCreated(any(), EventType.SESSION_START)
            anyConstructed<InAppMessageManagerImpl>().registerPendingShowRequest(any(), any(), any(), any())
            anyConstructed<InAppMessageManagerImpl>().detectReloadMode(EventType.SESSION_START, any(), any())
            anyConstructed<InAppMessageManagerImpl>().pickAndShowMessage()
            anyConstructed<InAppMessageManagerImpl>().preloadAndShow(any(), any())
            anyConstructed<InAppMessageManagerImpl>().show(controlGroupMessage)
            // banner+show event handling
            anyConstructed<InAppMessageManagerImpl>().onEventCreated(any(), EventType.BANNER)
            anyConstructed<InAppMessageManagerImpl>().registerPendingShowRequest(any(), any(), any(), any())
            anyConstructed<InAppMessageManagerImpl>().detectReloadMode(EventType.BANNER, any(), any())
            // and stops
        }
    }

    private fun disableBitmapCache() {
        every {
            anyConstructed<InAppMessageBitmapCacheImpl>().preload(any(), any())
        } answers {
            secondArg<((Boolean) -> Unit)?>()?.invoke(false)
        }
        every { anyConstructed<InAppMessageBitmapCacheImpl>().getFile(any()) } returns null
        every { anyConstructed<InAppMessageBitmapCacheImpl>().has(any()) } returns false
    }

    private fun prepareMessagesMocks(pendingMessages: ArrayList<InAppMessage>) {
        every { anyConstructed<FetchManagerImpl>().fetchInAppMessages(any(), any(), any(), any()) } answers {
            thirdArg<(Result<List<InAppMessage>>) -> Unit>().invoke(
                Result(true, pendingMessages)
            )
        }
        every {
            anyConstructed<InAppMessageBitmapCacheImpl>().preload(any(), any())
        } answers {
            secondArg<((Boolean) -> Unit)?>()?.invoke(true)
        }
        every {
            anyConstructed<InAppMessageBitmapCacheImpl>().getFile(any())
        } returns MockFile()
        every {
            anyConstructed<InAppMessageBitmapCacheImpl>().has(any())
        } answers {
            firstArg<String>() == "pending_image_url"
        }
        every { anyConstructed<InAppMessagesCacheImpl>().get() } returns pendingMessages
        every { anyConstructed<InAppMessagesCacheImpl>().set(any()) } just Runs
        every { anyConstructed<InAppMessagesCacheImpl>().clear() } returns true
        every { anyConstructed<InAppMessagesCacheImpl>().getTimestamp() } returns System.currentTimeMillis()
    }

    private fun simulateCustomerUsage(customerIdsMap: java.util.HashMap<String, String?>) {
        Sendsay.anonymize()
        identifyCustomerForTest(customerIdsMap)
        Sendsay.trackSessionStart()
        Sendsay.trackSessionEnd()
    }

    private fun identifyCustomerForTest(
        customerIdsMap: HashMap<String, String?>,
        properties: HashMap<String, Any> = hashMapOf()
    ) {
        val customerIds = CustomerIds(customerIdsMap).apply {
            cookie = customerIdsMap[CustomerIds.COOKIE]
        }
        Sendsay.identifyCustomer(
            customerIds = customerIds,
            properties = PropertiesList(properties)
        )
    }

    private fun initSdk() {
        skipInstallEvent()
        val initialProject = SendsayProject(
            "https://base-url.com",
            "project-token",
            "Token auth"
        )
        Sendsay.init(
            ApplicationProvider.getApplicationContext(),
            SendsayConfiguration(
                baseURL = initialProject.baseUrl,
                projectToken = initialProject.projectToken,
                authorization = initialProject.authorization,
                automaticSessionTracking = false
            )
        )
    }
}
