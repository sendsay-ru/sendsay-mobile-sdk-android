package com.sendsay.sdk.view

import android.content.Context
import androidx.browser.customtabs.CustomTabsClient
import androidx.test.core.app.ApplicationProvider
import com.sendsay.sdk.Sendsay
import com.sendsay.sdk.manager.EventManagerImpl
import com.sendsay.sdk.manager.FetchManagerImpl
import com.sendsay.sdk.manager.InAppContentBlockManagerImpl
import com.sendsay.sdk.manager.InAppContentBlockManagerImplTest.Companion.buildHtmlMessageContent
import com.sendsay.sdk.manager.InAppContentBlockManagerImplTest.Companion.buildMessage
import com.sendsay.sdk.mockkConstructorFix
import com.sendsay.sdk.models.ContentBlockCarouselCallback
import com.sendsay.sdk.models.ContentBlockSelector
import com.sendsay.sdk.models.Event
import com.sendsay.sdk.models.SendsayConfiguration
import com.sendsay.sdk.models.SendsayProject
import com.sendsay.sdk.models.FlushMode
import com.sendsay.sdk.models.HtmlActionType
import com.sendsay.sdk.models.InAppContentBlock
import com.sendsay.sdk.models.InAppContentBlockAction
import com.sendsay.sdk.models.Result
import com.sendsay.sdk.services.inappcontentblock.ContentBlockCarouselAdapter
import com.sendsay.sdk.telemetry.TelemetryManager
import com.sendsay.sdk.telemetry.model.EventType
import com.sendsay.sdk.testutil.SendsaySDKTest
import com.sendsay.sdk.testutil.componentForTesting
import com.sendsay.sdk.testutil.reset
import com.sendsay.sdk.testutil.runInSingleThread
import com.sendsay.sdk.testutil.shutdown
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.verify
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.LooperMode

@RunWith(RobolectricTestRunner::class)
internal class InAppContentBlockCarouselViewTest : SendsaySDKTest() {

    @Before
    fun before() {
        // Cold start
        Sendsay.reset()
        Sendsay.shutdown()
        // CustomTabsClient service off
        mockkStatic(CustomTabsClient::class)
        every {
            CustomTabsClient.bindCustomTabsService(any(), any(), any())
        } returns true
        mockkConstructorFix(FetchManagerImpl::class) {
            every { anyConstructed<FetchManagerImpl>().fetchSegments(any(), any(), any(), any()) }
        }
        mockkConstructorFix(InAppContentBlockManagerImpl::class)
    }

    @Test
    @LooperMode(LooperMode.Mode.LEGACY)
    fun `should load message assigned to placeholder ID`() = runInSingleThread { idleThreads ->
        prepareContentBlockMessages(
            arrayListOf(
                buildMessage(
                    "id1",
                    type = "html",
                    data = mapOf("html" to buildHtmlMessageContent())
                )
            ))
        initSdk()
        idleThreads()
        Sendsay.componentForTesting.inAppContentBlockManager.loadInAppContentBlockPlaceholders()
        idleThreads()
        mockkConstructorFix(TelemetryManager::class)
        val telemetryEventTypeSlot = slot<EventType>()
        val telemetryPropertiesSlot = slot<MutableMap<String, String>>()
        every {
            anyConstructed<TelemetryManager>().reportEvent(
                capture(telemetryEventTypeSlot),
                capture(telemetryPropertiesSlot)
            )
        } just Runs
        Sendsay.telemetry = TelemetryManager(ApplicationProvider.getApplicationContext())
        val carousel = Sendsay.getInAppContentBlocksCarousel(
            ApplicationProvider.getApplicationContext(),
            "placeholder_1"
        )
        assertNotNull(carousel)
        carousel.reload()
        idleThreads()
        assertTrue(telemetryEventTypeSlot.isCaptured)
        val capturedEventType = telemetryEventTypeSlot.captured
        assertNotNull(capturedEventType)
        assertEquals(EventType.SHOW_IN_APP_MESSAGE, capturedEventType)
        assertTrue(telemetryPropertiesSlot.isCaptured)
        val capturedProps = telemetryPropertiesSlot.captured
        assertNotNull(capturedProps)
        assertEquals("content_block_carousel", capturedProps["messageType"])
    }

    @Test
    @LooperMode(LooperMode.Mode.LEGACY)
    fun `should notify shown message assigned to placeholder ID`() = runInSingleThread { idleThreads ->
        prepareContentBlockMessages(
            arrayListOf(
                buildMessage(
                    "id1",
                    type = "html",
                    data = mapOf("html" to buildHtmlMessageContent())
                )
            ))
        initSdk()
        idleThreads()
        Sendsay.componentForTesting.inAppContentBlockManager.loadInAppContentBlockPlaceholders()
        idleThreads()
        val carousel = Sendsay.getInAppContentBlocksCarousel(
            ApplicationProvider.getApplicationContext(),
            "placeholder_1"
        )
        assertNotNull(carousel)
        var shownPlaceholderId: String? = null
        var shownContentBlock: InAppContentBlock? = null
        carousel.behaviourCallback = object : EmptyCarouselBehaviourCallback() {
            override fun onMessageShown(
                placeholderId: String,
                contentBlock: InAppContentBlock,
                index: Int,
                count: Int
            ) {
                shownPlaceholderId = placeholderId
                shownContentBlock = contentBlock
            }
        }
        carousel.reload()
        idleThreads()
        assertEquals("placeholder_1", shownPlaceholderId)
        assertNotNull(shownContentBlock)
        assertEquals("id1", shownContentBlock?.id)
    }

    @Test
    @LooperMode(LooperMode.Mode.LEGACY)
    fun `should notify not-shown message`() = runInSingleThread { idleThreads ->
        prepareContentBlockMessages(
            arrayListOf(
                buildMessage(
                    "id1",
                    type = "html",
                    data = mapOf("html" to buildHtmlMessageContent())
                )
            ))
        initSdk()
        idleThreads()
        Sendsay.componentForTesting.inAppContentBlockManager.loadInAppContentBlockPlaceholders()
        idleThreads()
        val carousel = Sendsay.getInAppContentBlocksCarousel(
            ApplicationProvider.getApplicationContext(),
            "placeholder_2"
        )
        assertNotNull(carousel)
        var notifiedPlaceholderId: String? = null
        carousel.behaviourCallback = object : EmptyCarouselBehaviourCallback() {
            override fun onNoMessageFound(placeholderId: String) {
                notifiedPlaceholderId = placeholderId
            }
        }
        carousel.reload()
        idleThreads()
        assertEquals("placeholder_2", notifiedPlaceholderId)
    }

    @Test
    fun `should sort messages by name as for same priority`() = runInSingleThread { idleThreads ->
        val htmlMessageContent = mapOf("html" to buildHtmlMessageContent())
        val messages = arrayListOf(
            buildMessage("id1", priority = 10, name = "D", data = htmlMessageContent, type = "html"),
            buildMessage("id2", priority = 10, name = "B", data = htmlMessageContent, type = "html"),
            buildMessage("id3", priority = 10, name = "C", data = htmlMessageContent, type = "html"),
            buildMessage("id4", priority = 10, name = "A", data = htmlMessageContent, type = "html")
        )
        prepareContentBlockMessages(messages)
        initSdk()
        idleThreads()
        Sendsay.componentForTesting.inAppContentBlockManager.loadInAppContentBlockPlaceholders()
        idleThreads()
        val carousel = Sendsay.getInAppContentBlocksCarousel(
            ApplicationProvider.getApplicationContext(),
            "placeholder_1"
        )
        assertNotNull(carousel)
        carousel.reload()
        idleThreads()
        val loadedMessages = carousel.viewController.contentBlockCarouselAdapter.getLoadedData()
        assertEquals(4, loadedMessages.size)
        assertEquals("id4", loadedMessages[0].id)
        assertEquals("id2", loadedMessages[1].id)
        assertEquals("id3", loadedMessages[2].id)
        assertEquals("id1", loadedMessages[3].id)
    }

    @Test
    fun `should sort messages by priority primary`() = runInSingleThread { idleThreads ->
        val htmlMessageContent = mapOf("html" to buildHtmlMessageContent())
        val messages = arrayListOf(
            buildMessage("id1", priority = 10, name = "D", data = htmlMessageContent, type = "html"),
            buildMessage("id2", priority = 20, name = "C", data = htmlMessageContent, type = "html"),
            buildMessage("id3", priority = 30, name = "B", data = htmlMessageContent, type = "html"),
            buildMessage("id4", priority = 40, name = "A", data = htmlMessageContent, type = "html")
        )
        prepareContentBlockMessages(messages)
        initSdk()
        idleThreads()
        Sendsay.componentForTesting.inAppContentBlockManager.loadInAppContentBlockPlaceholders()
        idleThreads()
        val carousel = Sendsay.getInAppContentBlocksCarousel(
            ApplicationProvider.getApplicationContext(),
            "placeholder_1"
        )
        assertNotNull(carousel)
        carousel.reload()
        idleThreads()
        val loadedMessages = carousel.viewController.contentBlockCarouselAdapter.getLoadedData()
        assertEquals(4, loadedMessages.size)
        assertEquals("id4", loadedMessages[0].id)
        assertEquals("id3", loadedMessages[1].id)
        assertEquals("id2", loadedMessages[2].id)
        assertEquals("id1", loadedMessages[3].id)
    }

    @Test
    fun `should sort messages by custom impl`() = runInSingleThread { idleThreads ->
        val htmlMessageContent = mapOf("html" to buildHtmlMessageContent())
        val messages = arrayListOf(
            buildMessage("id1", priority = 10, name = "D", data = htmlMessageContent, type = "html"),
            buildMessage("id2", priority = 20, name = "C", data = htmlMessageContent, type = "html"),
            buildMessage("id3", priority = 30, name = "B", data = htmlMessageContent, type = "html"),
            buildMessage("id4", priority = 40, name = "A", data = htmlMessageContent, type = "html")
        )
        prepareContentBlockMessages(messages)
        initSdk()
        idleThreads()
        Sendsay.componentForTesting.inAppContentBlockManager.loadInAppContentBlockPlaceholders()
        idleThreads()
        val carousel = Sendsay.getInAppContentBlocksCarousel(
            ApplicationProvider.getApplicationContext(),
            "placeholder_1"
        )
        carousel?.contentBlockSelector = object : ContentBlockSelector() {
            override fun sortContentBlocks(source: List<InAppContentBlock>): List<InAppContentBlock> {
                // default result is id4, id3, id2, id1
                val defaultSort = super.sortContentBlocks(source)
                // shuffle and miss index 2 by purpose
                return arrayListOf(defaultSort[1], defaultSort[3], defaultSort[0])
            }
        }
        assertNotNull(carousel)
        carousel.reload()
        idleThreads()
        val loadedMessages = carousel.viewController.contentBlockCarouselAdapter.getLoadedData()
        assertEquals(3, loadedMessages.size)
        assertEquals("id3", loadedMessages[0].id)
        assertEquals("id1", loadedMessages[1].id)
        assertEquals("id4", loadedMessages[2].id)
    }

    @Test
    fun `should filter messages by custom impl`() = runInSingleThread { idleThreads ->
        val htmlMessageContent = mapOf("html" to buildHtmlMessageContent())
        val messages = arrayListOf(
            buildMessage("id1", priority = 10, name = "D", data = htmlMessageContent, type = "html"),
            buildMessage("id2", priority = 20, name = "C", data = htmlMessageContent, type = "html"),
            buildMessage("id3", priority = 30, name = "B", data = htmlMessageContent, type = "html"),
            buildMessage("id4", priority = 40, name = "A", data = htmlMessageContent, type = "html")
        )
        prepareContentBlockMessages(messages)
        initSdk()
        idleThreads()
        Sendsay.componentForTesting.inAppContentBlockManager.loadInAppContentBlockPlaceholders()
        idleThreads()
        val carousel = Sendsay.getInAppContentBlocksCarousel(
            ApplicationProvider.getApplicationContext(),
            "placeholder_1"
        )
        carousel?.contentBlockSelector = object : ContentBlockSelector() {
            override fun filterContentBlocks(source: List<InAppContentBlock>): List<InAppContentBlock> {
                return source.filter { it.id == "id3" }
            }
        }
        assertNotNull(carousel)
        carousel.reload()
        idleThreads()
        val loadedMessages = carousel.viewController.contentBlockCarouselAdapter.getLoadedData()
        assertEquals(1, loadedMessages.size)
        assertEquals("id3", loadedMessages[0].id)
    }

    @Test
    fun `should shown valid counter info after reload`() = runInSingleThread { idleThreads ->
        val htmlMessageContent = mapOf("html" to buildHtmlMessageContent())
        val messages = arrayListOf(
            buildMessage("id1", priority = 10, name = "D", data = htmlMessageContent, type = "html"),
            buildMessage("id2", priority = 20, name = "C", data = htmlMessageContent, type = "html"),
            buildMessage("id3", priority = 30, name = "B", data = htmlMessageContent, type = "html"),
            buildMessage("id4", priority = 40, name = "A", data = htmlMessageContent, type = "html")
        )
        prepareContentBlockMessages(arrayListOf())
        initSdk()
        idleThreads()
        val carousel = Sendsay.getInAppContentBlocksCarousel(
            ApplicationProvider.getApplicationContext(),
            "placeholder_1"
        )
        assertNotNull(carousel)
        carousel.reload()
        idleThreads()
        assertNull(carousel.getShownContentBlock())
        assertEquals(-1, carousel.getShownIndex())
        assertEquals(0, carousel.getShownCount())
        prepareContentBlockMessages(messages)
        Sendsay.componentForTesting.inAppContentBlockManager.loadInAppContentBlockPlaceholders()
        idleThreads()
        carousel.reload()
        idleThreads()
        assertNotNull(carousel.getShownContentBlock())
        assertEquals(0, carousel.getShownIndex())
        assertEquals(4, carousel.getShownCount())
    }

    @Test
    fun `should track show only once per message`() = runInSingleThread { idleThreads ->
        mockkConstructorFix(EventManagerImpl::class) {
            every { anyConstructed<EventManagerImpl>().addEventToQueue(any(), any(), any()) }
        }
        val htmlMessageContent = mapOf("html" to buildHtmlMessageContent())
        val messages = arrayListOf(
            buildMessage("id1", priority = 30, name = "D", data = htmlMessageContent, type = "html"),
            buildMessage("id2", priority = 20, name = "C", data = htmlMessageContent, type = "html"),
            buildMessage("id3", priority = 10, name = "B", data = htmlMessageContent, type = "html")
        )
        prepareContentBlockMessages(messages)
        initSdk()
        idleThreads()
        val eventSlot = slot<Event>()
        val eventTypeSlot = slot<com.sendsay.sdk.models.EventType>()
        every {
            anyConstructed<EventManagerImpl>().addEventToQueue(capture(eventSlot), capture(eventTypeSlot), any())
        } just Runs
        val carousel = Sendsay.getInAppContentBlocksCarousel(
            ApplicationProvider.getApplicationContext(),
            "placeholder_1"
        )
        assertNotNull(carousel)
        idleThreads()
        carousel.reload()
        idleThreads()
        for (i in 0..100) {
            carousel.viewController.moveToIndex(0, false)
            idleThreads()
            carousel.viewController.moveToIndex(1, false)
            idleThreads()
            carousel.viewController.moveToIndex(2, false)
            idleThreads()
        }
        verify(exactly = 3) {
            anyConstructed<EventManagerImpl>().addEventToQueue(any(), any(), any())
        }
        assertEquals("banner", eventSlot.captured.type)
        assertEquals(com.sendsay.sdk.models.EventType.BANNER, eventTypeSlot.captured)
    }

    @Test
    @LooperMode(LooperMode.Mode.LEGACY)
    fun `should notify error for message`() = runInSingleThread { idleThreads ->
        prepareContentBlockMessages(
            arrayListOf(
                buildMessage(
                    "id1",
                    type = "html",
                    data = mapOf("html" to buildHtmlMessageContent())
                )
            ))
        initSdk()
        idleThreads()
        Sendsay.componentForTesting.inAppContentBlockManager.loadInAppContentBlockPlaceholders()
        idleThreads()
        val carousel = Sendsay.getInAppContentBlocksCarousel(
            ApplicationProvider.getApplicationContext(),
            "placeholder_1"
        )
        assertNotNull(carousel)
        var notifiedPlaceholderId: String? = null
        var notifiedContentBlock: InAppContentBlock? = null
        var notifiedError: String? = null
        carousel.behaviourCallback = object : EmptyCarouselBehaviourCallback() {
            override fun onError(placeholderId: String, contentBlock: InAppContentBlock?, errorMessage: String) {
                notifiedPlaceholderId = placeholderId
                notifiedContentBlock = contentBlock
                notifiedError = errorMessage
            }
        }
        var createdCbView: InAppContentBlockPlaceholderView? = null
        carousel.viewController.contentBlockCarouselAdapter = ContentBlockCarouselAdapter(
            placeholderId = "placeholder_1",
            onPlaceholderCreated = {
                carousel.viewController.modifyPlaceholderBehaviour(it)
                createdCbView = it
            }
        )
        carousel.reload()
        idleThreads()
        val cbViewHolder = carousel.viewController.contentBlockCarouselAdapter.createViewHolder(carousel, 0)
        cbViewHolder.updateContent(carousel.getShownContentBlock())
        assertNotNull(createdCbView)
        createdCbView?.controller?.loadContent(false)
        createdCbView?.controller?.onUrlClick("invalid_url")
        assertEquals("placeholder_1", notifiedPlaceholderId)
        assertNotNull(notifiedContentBlock)
        assertEquals("id1", notifiedContentBlock?.id)
        assertEquals("Invalid action definition", notifiedError)
    }

    @Test
    @LooperMode(LooperMode.Mode.LEGACY)
    fun `should notify click for message`() = runInSingleThread { idleThreads ->
        prepareContentBlockMessages(
            arrayListOf(
                buildMessage(
                    "id1",
                    type = "html",
                    data = mapOf("html" to buildHtmlMessageContent())
                )
            ))
        initSdk()
        idleThreads()
        Sendsay.componentForTesting.inAppContentBlockManager.loadInAppContentBlockPlaceholders()
        idleThreads()
        val carousel = Sendsay.getInAppContentBlocksCarousel(
            ApplicationProvider.getApplicationContext(),
            "placeholder_1"
        )
        assertNotNull(carousel)
        var notifiedPlaceholderId: String? = null
        var notifiedContentBlock: InAppContentBlock? = null
        var notifiedAction: InAppContentBlockAction? = null
        carousel.behaviourCallback = object : EmptyCarouselBehaviourCallback() {
            override val overrideDefaultBehavior = true
            override fun onActionClicked(
                placeholderId: String,
                contentBlock: InAppContentBlock,
                action: InAppContentBlockAction
            ) {
                notifiedPlaceholderId = placeholderId
                notifiedContentBlock = contentBlock
                notifiedAction = action
            }
        }
        var createdCbView: InAppContentBlockPlaceholderView? = null
        carousel.viewController.contentBlockCarouselAdapter = ContentBlockCarouselAdapter(
            placeholderId = "placeholder_1",
            onPlaceholderCreated = {
                carousel.viewController.modifyPlaceholderBehaviour(it)
                createdCbView = it
            }
        )
        carousel.reload()
        idleThreads()
        val cbViewHolder = carousel.viewController.contentBlockCarouselAdapter.createViewHolder(carousel, 0)
        cbViewHolder.updateContent(carousel.getShownContentBlock())
        assertNotNull(createdCbView)
        createdCbView?.controller?.loadContent(false)
        createdCbView?.controller?.onUrlClick("https://sendsay.com?xnpe_force_track=true")
        assertEquals("placeholder_1", notifiedPlaceholderId)
        assertNotNull(notifiedContentBlock)
        assertEquals("id1", notifiedContentBlock?.id)
        assertEquals("https://sendsay.com?xnpe_force_track=true", notifiedAction?.url)
    }

    @Test
    @LooperMode(LooperMode.Mode.LEGACY)
    fun `should notify close for message`() = runInSingleThread { idleThreads ->
        prepareContentBlockMessages(
            arrayListOf(
                buildMessage(
                    "id1",
                    type = "html",
                    data = mapOf("html" to buildHtmlMessageContent())
                )
            ))
        initSdk()
        idleThreads()
        Sendsay.componentForTesting.inAppContentBlockManager.loadInAppContentBlockPlaceholders()
        idleThreads()
        val carousel = Sendsay.getInAppContentBlocksCarousel(
            ApplicationProvider.getApplicationContext(),
            "placeholder_1"
        )
        assertNotNull(carousel)
        var notifiedPlaceholderId: String? = null
        var notifiedContentBlock: InAppContentBlock? = null
        carousel.behaviourCallback = object : EmptyCarouselBehaviourCallback() {
            override fun onCloseClicked(placeholderId: String, contentBlock: InAppContentBlock) {
                notifiedPlaceholderId = placeholderId
                notifiedContentBlock = contentBlock
            }
        }
        var createdCbView: InAppContentBlockPlaceholderView? = null
        carousel.viewController.contentBlockCarouselAdapter = ContentBlockCarouselAdapter(
            placeholderId = "placeholder_1",
            onPlaceholderCreated = {
                carousel.viewController.modifyPlaceholderBehaviour(it)
                createdCbView = it
            }
        )
        carousel.reload()
        idleThreads()
        val cbViewHolder = carousel.viewController.contentBlockCarouselAdapter.createViewHolder(carousel, 0)
        cbViewHolder.updateContent(carousel.getShownContentBlock())
        assertNotNull(createdCbView)
        createdCbView?.controller?.loadContent(false)
        val manualCloseUrl = createdCbView?.controller?.assignedHtmlContent?.actions?.find {
            it.actionType == HtmlActionType.CLOSE
        }
        createdCbView?.controller?.onUrlClick(manualCloseUrl?.actionUrl!!)
        assertEquals("placeholder_1", notifiedPlaceholderId)
        assertNotNull(notifiedContentBlock)
        assertEquals("id1", notifiedContentBlock?.id)
    }

    private fun prepareContentBlockMessages(messages: ArrayList<InAppContentBlock>) {
        every {
            anyConstructed<FetchManagerImpl>().fetchStaticInAppContentBlocks(any(), any(), any())
        } answers {
            arg<(Result<ArrayList<InAppContentBlock>?>) -> Unit>(1).invoke(Result(true, messages))
        }
        every {
            anyConstructed<InAppContentBlockManagerImpl>().loadInAppContentBlockPlaceholders()
        } answers {
            callOriginal()
        }
    }

    private fun initSdk() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val initialProject = SendsayProject("https://base-url.com", "project-token", "Token auth")
        skipInstallEvent()
        Sendsay.flushMode = FlushMode.MANUAL
        Sendsay.init(context, SendsayConfiguration(
            baseURL = initialProject.baseUrl,
            projectToken = initialProject.projectToken,
            authorization = initialProject.authorization)
        )
    }
}

open class EmptyCarouselBehaviourCallback(
    override val overrideDefaultBehavior: Boolean = false,
    override val trackActions: Boolean = true
) : ContentBlockCarouselCallback {
    override fun onMessageShown(placeholderId: String, contentBlock: InAppContentBlock, index: Int, count: Int) {
        // nothing to do
    }

    override fun onMessagesChanged(count: Int, messages: List<InAppContentBlock>) {
        // nothing to do
    }

    override fun onNoMessageFound(placeholderId: String) {
        // nothing to do
    }

    override fun onError(placeholderId: String, contentBlock: InAppContentBlock?, errorMessage: String) {
        // nothing to do
    }

    override fun onCloseClicked(placeholderId: String, contentBlock: InAppContentBlock) {
        // nothing to do
    }

    override fun onActionClicked(
        placeholderId: String,
        contentBlock: InAppContentBlock,
        action: InAppContentBlockAction
    ) {
        // nothing to do
    }

    override fun onHeightUpdate(placeholderId: String, height: Int) {
        // nothing to do
    }
}
