package ru.sendsay.sdk

import android.content.Context
import android.content.pm.ProviderInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ApplicationProvider
import ru.sendsay.sdk.services.SendsayContextProvider
import ru.sendsay.sdk.testutil.mocks.MockApplication
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Robolectric.buildActivity
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = MockApplication::class)
internal class ForegroundStateTests {

    @Before
    fun init() {
        SendsayContextProvider.applicationIsForeground = false
        Robolectric
            .buildContentProvider(SendsayContextProvider::class.java)
            .create(ProviderInfo().apply {
                authority = "${ApplicationProvider.getApplicationContext<Context>().packageName}.sdk.contextprovider"
                grantUriPermissions = true
            }).get()
    }

    @Test
    fun `should detect foreground state for started activity until stopped`() {
        val controller = buildActivity(AppCompatActivity::class.java)
        assertFalse(SendsayContextProvider.applicationIsForeground)
        controller.create()
        assertFalse(SendsayContextProvider.applicationIsForeground)
        controller.start()
        assertTrue(SendsayContextProvider.applicationIsForeground)
        controller.postCreate(null)
        assertTrue(SendsayContextProvider.applicationIsForeground)
        controller.resume()
        assertTrue(SendsayContextProvider.applicationIsForeground)
        controller.postResume()
        assertTrue(SendsayContextProvider.applicationIsForeground)
        controller.visible()
        assertTrue(SendsayContextProvider.applicationIsForeground)
        controller.topActivityResumed(true)
        assertTrue(SendsayContextProvider.applicationIsForeground)
        controller.pause()
        assertTrue(SendsayContextProvider.applicationIsForeground)
        controller.stop()
        assertFalse(SendsayContextProvider.applicationIsForeground)
        controller.destroy()
        assertFalse(SendsayContextProvider.applicationIsForeground)
    }
}
