package com.sendsay.sdk.runcatching

import android.app.Activity
import android.os.Bundle
import com.sendsay.sdk.Sendsay
import com.sendsay.sdk.models.SendsayConfiguration
import com.sendsay.sdk.models.FlushMode
import com.sendsay.sdk.testutil.SendsaySDKTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ActivityController

@RunWith(RobolectricTestRunner::class)
internal class SendsaySafeModeLifecycleTest : SendsaySDKTest() {
    private lateinit var controller: ActivityController<TestActivity>

    @Before
    fun before() {
        SendsayExceptionThrowing.prepareSendsayToThrow()
        controller = Robolectric.buildActivity(TestActivity::class.java)
        controller.create()
    }

    @Test
    fun `should not throw internal error on onResume when in safe mode`() {
        Sendsay.safeModeEnabled = true
        SendsayExceptionThrowing.makeSendsayThrow()
        controller.resume()
    }

    @Test(expected = SendsayExceptionThrowing.TestPurposeException::class)
    fun `should throw internal error on onStart when not in safe mode`() {
        Sendsay.safeModeEnabled = false
        SendsayExceptionThrowing.makeSendsayThrow()
        try {
            controller.start()
            controller.postCreate(null)
        } catch (e: RuntimeException) { // robolectric wraps exception into runtime exception
            if (e.cause != null) throw e.cause!!
        }
    }

    @Test
    fun `should not throw internal error on onStop when in safe mode`() {
        Sendsay.safeModeEnabled = true
        controller.start()
        controller.resume()
        SendsayExceptionThrowing.makeSendsayThrow()
        controller.pause()
        controller.stop()
    }

    @Test(expected = SendsayExceptionThrowing.TestPurposeException::class)
    fun `should throw internal error on onStop when not in safe mode`() {
        Sendsay.safeModeEnabled = false
        controller.start()
        controller.postCreate(null)
        controller.resume()
        SendsayExceptionThrowing.makeSendsayThrow()
        controller.pause()
        controller.stop()
    }

    class TestActivity : Activity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            skipInstallEvent()
            Sendsay.flushMode = FlushMode.MANUAL
            Sendsay.init(applicationContext, SendsayConfiguration(projectToken = "mock-token"))
        }
    }
}
