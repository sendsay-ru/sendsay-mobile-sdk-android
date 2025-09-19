package com.sendsay.sdk.runcatching

import com.sendsay.sdk.Sendsay
import com.sendsay.sdk.models.FlushMode
import com.sendsay.sdk.testutil.SendsaySDKTest
import com.sendsay.sdk.util.backgroundThreadDispatcher
import com.sendsay.sdk.util.mainThreadDispatcher
import java.util.Random
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class WaitForInitApiCoveredTest : SendsaySDKTest() {

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
    fun `all Init awaiting methods should be testable`() {
        val publicMethods = PublicApiTestCases.methods.map { it.first }
        assertTrue {
            PublicApiTestCases.awaitInitMethods.all {
                publicMethods.contains(it)
            }
        }
    }

    @Test
    fun `all Init awaiting methods cannot be SDKless`() {
        assertTrue {
            PublicApiTestCases.awaitInitMethods.all {
                !PublicApiTestCases.sdkLessMethods.contains(it)
            }
        }
    }

    @Test
    fun `should wake pending callbacks after init`() {
        PublicApiTestCases.initMethods.forEach {
            Sendsay.flushMode = FlushMode.MANUAL
            Sendsay.isInitialized = false
            skipInstallEvent()
            var called = false
            var initRuns = false
            Sendsay.initGate.waitForInitialize {
                called = true
                assertTrue(initRuns)
            }
            // run init
            initRuns = true
            it.second.invoke()
            // check if trigger has been called
            assertTrue(called)
            assertTrue(Sendsay.initGate.afterInitCallbacks.isEmpty())
        }
    }

    @Test
    fun `listed API methods should wait for SDK init`() {
        PublicApiTestCases.methods
            .filter { PublicApiTestCases.awaitInitMethods.contains(it.first) }
            .forEach {
                Sendsay.flushMode = FlushMode.MANUAL
                Sendsay.isInitialized = false
                skipInstallEvent()
                // invoke method
                it.second.invoke()
                assertEquals(1, Sendsay.initGate.afterInitCallbacks.size,
                    "Method 'Sendsay.${it.first.name}' didn't wait for full init"
                )
                // !!! do not run init, it'll start method in real and we have no power to detect when it is finished.
                // We are fine, that method has been added to pending callbacks
                Sendsay.initGate.clear()
            }
    }

    @Test
    fun `should drop pending callbacks after anonymize`() {
        PublicApiTestCases.initMethods.forEach {
            Sendsay.flushMode = FlushMode.MANUAL
            Sendsay.isInitialized = false
            skipInstallEvent()
            // run init
            it.second.invoke()
            // Simulate non-init, callback will be kept
            Sendsay.isInitialized = false
            var called = false
            Sendsay.initGate.waitForInitialize {
                called = true
            }
            // Return init state for anonymize method; plus callbacks are not triggered this way
            Sendsay.isInitialized = true
            assertEquals(1, Sendsay.initGate.afterInitCallbacks.size,
                "SendsayInitManager didn't wait for trigger"
            )
            Sendsay.anonymize()
            // check if trigger has not been called AND callbacks are dropped
            assertFalse(called)
            assertTrue(Sendsay.initGate.afterInitCallbacks.isEmpty())
        }
    }

    @Test
    fun `should invoke pending callbacks by FIFO`() {
        Sendsay.flushMode = FlushMode.MANUAL
        skipInstallEvent()
        val randomized = Random()
        var brief = ""
        val expectedResult = "Hello world"
        expectedResult.forEach {
            Sendsay.initGate.waitForInitialize {
                Thread.sleep(500 + (if (randomized.nextBoolean()) 500L else 0))
                brief += it
            }
        }
        // run init
        PublicApiTestCases.initMethods.get(1).second.invoke()
        // check if brief has been written as expected
        assertEquals(expectedResult, brief)
    }

    @Test
    fun `should invoke all pending callbacks in case or error`() {
        Sendsay.flushMode = FlushMode.MANUAL
        skipInstallEvent()
        val safeModeOrig = Sendsay.safeModeEnabled
        Sendsay.safeModeEnabled = true
        var runsCount = 0
        Sendsay.initGate.waitForInitialize {
            runsCount++
        }
        Sendsay.initGate.waitForInitialize {
            throw java.lang.RuntimeException("should be only logged")
        }
        Sendsay.initGate.waitForInitialize {
            runsCount++
        }
        // run init
        PublicApiTestCases.initMethods.get(1).second.invoke()
        Sendsay.safeModeEnabled = safeModeOrig
        assertEquals(2, runsCount)
    }
}
