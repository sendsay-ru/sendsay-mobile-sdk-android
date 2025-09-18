package ru.sendsay.sdk.services

import android.content.Context
import android.view.View
import androidx.test.core.app.ApplicationProvider
import ru.sendsay.sdk.Sendsay
import ru.sendsay.sdk.manager.AppInboxManagerImplTest
import ru.sendsay.sdk.models.SendsayConfiguration
import ru.sendsay.sdk.models.SendsayProject
import ru.sendsay.sdk.models.FlushMode
import ru.sendsay.sdk.models.MessageItem
import ru.sendsay.sdk.testutil.SendsaySDKTest
import ru.sendsay.sdk.view.AppInboxDetailView
import io.mockk.every
import io.mockk.mockkObject
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class AppInboxProviderTest : SendsaySDKTest() {

    @Before
    fun before() {
        // Need to be initialized to use bitmapCache for HTML parser
        val context = ApplicationProvider.getApplicationContext<Context>()
        val initialProject = SendsayProject(
            "https://base-url.com",
            "project-token",
            "Token auth"
        )
        Sendsay.flushMode = FlushMode.MANUAL
        Sendsay.init(context, SendsayConfiguration(
            baseURL = initialProject.baseUrl,
            projectToken = initialProject.projectToken,
            authorization = initialProject.authorization)
        )
    }

    @Test
    fun `should show empty view for missing message`() {
        mockkObject(Sendsay)
        every { Sendsay.fetchAppInboxItem(any(), any()) } answers {
            secondArg<(MessageItem?) -> Unit>().invoke(null)
        }
        val provider = DefaultAppInboxProvider()
        val context = ApplicationProvider.getApplicationContext<Context>()
        val view = provider.getAppInboxDetailView(context, "id1")
        val htmlDetailView = view as AppInboxDetailView
        assertEquals(View.GONE, htmlDetailView.htmlContainer.visibility)
        assertEquals(View.GONE, htmlDetailView.pushContainer.visibility)
    }

    @Test
    fun `should show empty view for unknown type`() {
        mockkObject(Sendsay)
        every { Sendsay.fetchAppInboxItem(any(), any()) } answers {
            secondArg<(MessageItem?) -> Unit>().invoke(AppInboxManagerImplTest.buildMessage(
                id = "id1",
                type = "blablabla",
                data = mapOf(
                    "title" to "Title",
                    "pre_header" to "Message",
                    "message" to AppInboxManagerImplTest.buildHtmlMessageContent()
                )))
        }
        val provider = DefaultAppInboxProvider()
        val context = ApplicationProvider.getApplicationContext<Context>()
        val view = provider.getAppInboxDetailView(context, "id1")
        val htmlDetailView = view as AppInboxDetailView
        assertEquals(View.GONE, htmlDetailView.htmlContainer.visibility)
        assertEquals(View.GONE, htmlDetailView.pushContainer.visibility)
    }

    @Test
    fun `should show push notification view`() {
        mockkObject(Sendsay)
        every { Sendsay.fetchAppInboxItem(any(), any()) } answers {
            secondArg<(MessageItem?) -> Unit>().invoke(AppInboxManagerImplTest.buildMessage(
                id = "id1", type = "html", data = mapOf(
                "title" to "Title",
                "pre_header" to "Message",
                "message" to AppInboxManagerImplTest.buildHtmlMessageContent()
            )))
        }
        val provider = DefaultAppInboxProvider()
        val context = ApplicationProvider.getApplicationContext<Context>()
        val view = provider.getAppInboxDetailView(context, "id1")
        val htmlDetailView = view as AppInboxDetailView
        assertEquals(View.VISIBLE, htmlDetailView.htmlContainer.visibility)
        assertEquals(View.GONE, htmlDetailView.pushContainer.visibility)
    }

    @Test
    fun `should show html notification view`() {
        mockkObject(Sendsay)
        every { Sendsay.fetchAppInboxItem(any(), any()) } answers {
            secondArg<(MessageItem?) -> Unit>().invoke(AppInboxManagerImplTest.buildMessage(
                id = "id1", type = "html", data = mapOf(
                "title" to "Title",
                "pre_header" to "Message",
                "message" to AppInboxManagerImplTest.buildHtmlMessageContent()
            )))
        }
        val provider = DefaultAppInboxProvider()
        val context = ApplicationProvider.getApplicationContext<Context>()
        val view = provider.getAppInboxDetailView(context, "id1")
        val htmlDetailView = view as AppInboxDetailView
        assertEquals(View.VISIBLE, htmlDetailView.htmlContainer.visibility)
        assertEquals(View.GONE, htmlDetailView.pushContainer.visibility)
    }
}
