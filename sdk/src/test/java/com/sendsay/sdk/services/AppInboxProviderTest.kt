package com.sendsay.sdk.services

import android.content.Context
import android.view.View
import androidx.test.core.app.ApplicationProvider
import com.sendsay.sdk.Sendsay
import com.sendsay.sdk.manager.AppInboxManagerImplTest
import com.sendsay.sdk.models.SendsayConfiguration
import com.sendsay.sdk.models.SendsayProject
import com.sendsay.sdk.models.FlushMode
import com.sendsay.sdk.models.MessageItem
import com.sendsay.sdk.testutil.SendsaySDKTest
import com.sendsay.sdk.view.AppInboxDetailView
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
