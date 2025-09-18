package ru.sendsay.sdk

import androidx.test.core.app.ApplicationProvider
import ru.sendsay.sdk.models.SendsayConfiguration
import ru.sendsay.sdk.models.FlushMode
import ru.sendsay.sdk.testutil.SendsaySDKTest
import ru.sendsay.sdk.testutil.mocks.DebugMockApplication
import ru.sendsay.sdk.testutil.mocks.ReleaseMockApplication
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
internal class SendsayTest : SendsaySDKTest() {
    @Test
    fun `should get null as customer cookie before initialized`() {
        assertNull(Sendsay.customerCookie)
    }

    @Test
    fun `should get customer cookie after initialized`() {
        initSdk()
        assertNotNull(Sendsay.customerCookie)
    }

    @Test
    fun `should get current customer cookie after anonymize`() {
        initSdk()
        val cookie1 = Sendsay.customerCookie
        assertNotNull(cookie1)
        Sendsay.anonymize()
        val cookie2 = Sendsay.customerCookie
        assertNotNull(cookie2)
        assertNotEquals(cookie1, cookie2)
    }

    @Test
    @Config(application = ReleaseMockApplication::class)
    fun `should have debug-off and safeMode-on in release build - before SDK init`() {
        assertFalse(Sendsay.runDebugMode)
        assertTrue(Sendsay.safeModeEnabled)
    }

    @Test
    @Config(application = ReleaseMockApplication::class)
    fun `should have debug-off and safeMode-on in release build after SDK init`() {
        initSdk()
        assertFalse(Sendsay.runDebugMode)
        assertTrue(Sendsay.safeModeEnabled)
    }

    @Test
    @Config(application = DebugMockApplication::class)
    fun `should have debug-off and safeMode-off in debug build before SDK init`() {
        // without context => false
        assertFalse(Sendsay.runDebugMode)
        // without context => true
        assertTrue(Sendsay.safeModeEnabled)
    }

    @Test
    @Config(application = DebugMockApplication::class)
    fun `should have debug-on and safeMode-off in debug build after SDK init`() {
        initSdk()
        assertTrue(Sendsay.runDebugMode)
        assertFalse(Sendsay.safeModeEnabled)
    }

    @Test
    fun `should have debug-on and safeMode-off in UnitTest run before SDK init`() {
        // without context => false
        assertFalse(Sendsay.runDebugMode)
        // without context => true
        assertTrue(Sendsay.safeModeEnabled)
    }

    @Test
    fun `should have debug-on and safeMode-off in UnitTest run after SDK init`() {
        initSdk()
        assertTrue(Sendsay.runDebugMode)
        assertFalse(Sendsay.safeModeEnabled)
    }

    @Test
    @Config(application = ReleaseMockApplication::class)
    fun `MODE behaviour as expected for release build before SDK init`() {
        val debugModeOverrideIndex = 0
        val safeModeOverrideIndex = 1
        val expectedDebugModeIndex = 2
        val expectedSafeModeIndex = 3
        val behaviorRules = listOf(
            arrayOf(null, null, false, true),
            arrayOf(null, true, false, true),
            arrayOf(null, false, false, false),
            arrayOf(true, null, true, true),
            arrayOf(true, true, true, true),
            arrayOf(true, false, true, false),
            arrayOf(false, null, false, true),
            arrayOf(false, true, false, true),
            arrayOf(false, false, false, false)
        )
        behaviorRules.forEach { env ->
            val debugOverride = env[debugModeOverrideIndex]
            val safeOverride = env[safeModeOverrideIndex]
            Sendsay.runDebugModeOverride = debugOverride
            Sendsay.safeModeOverride = safeOverride
            val expectedDebug = env[expectedDebugModeIndex]
            assertEquals(expectedDebug, Sendsay.runDebugMode, """
            Debug should be '$expectedDebug' for state '$debugOverride':'$safeOverride'
            """.trimIndent())
            val expectedSafe = env[expectedSafeModeIndex]
            assertEquals(expectedSafe, Sendsay.safeModeEnabled, """
            Expected to safeMode be '$expectedSafe' for state '$debugOverride':'$safeOverride'
            """.trimIndent())
        }
    }

    @Test
    @Config(application = DebugMockApplication::class)
    fun `MODE behaviour as expected for debug build before SDK init`() {
        val debugModeOverrideIndex = 0
        val safeModeOverrideIndex = 1
        val expectedDebugModeIndex = 2
        val expectedSafeModeIndex = 3
        val behaviorRules = listOf(
            arrayOf(null, null, false, true),
            arrayOf(null, true, false, true),
            arrayOf(null, false, false, false),
            arrayOf(true, null, true, true),
            arrayOf(true, true, true, true),
            arrayOf(true, false, true, false),
            arrayOf(false, null, false, true),
            arrayOf(false, true, false, true),
            arrayOf(false, false, false, false)
        )
        behaviorRules.forEach { env ->
            val debugOverride = env[debugModeOverrideIndex]
            val safeOverride = env[safeModeOverrideIndex]
            Sendsay.runDebugModeOverride = debugOverride
            Sendsay.safeModeOverride = safeOverride
            val expectedDebug = env[expectedDebugModeIndex]
            assertEquals(expectedDebug, Sendsay.runDebugMode, """
            Debug should be '$expectedDebug' for state '$debugOverride':'$safeOverride'
            """.trimIndent())
            val expectedSafe = env[expectedSafeModeIndex]
            assertEquals(expectedSafe, Sendsay.safeModeEnabled, """
            Expected to safeMode be '$expectedSafe' for state '$debugOverride':'$safeOverride'
            """.trimIndent())
        }
    }

    @Test
    @Config(application = ReleaseMockApplication::class)
    fun `MODE behaviour as expected for release build after SDK init`() {
        initSdk()
        val debugModeOverrideIndex = 0
        val safeModeOverrideIndex = 1
        val expectedDebugModeIndex = 2
        val expectedSafeModeIndex = 3
        val behaviorRules = listOf(
            arrayOf(null, null, false, true),
            arrayOf(null, true, false, true),
            arrayOf(null, false, false, false),
            arrayOf(true, null, true, false),
            arrayOf(true, true, true, true),
            arrayOf(true, false, true, false),
            arrayOf(false, null, false, true),
            arrayOf(false, true, false, true),
            arrayOf(false, false, false, false)
        )
        behaviorRules.forEach { env ->
            val debugOverride = env[debugModeOverrideIndex]
            val safeOverride = env[safeModeOverrideIndex]
            Sendsay.runDebugModeOverride = debugOverride
            Sendsay.safeModeOverride = safeOverride
            val expectedDebug = env[expectedDebugModeIndex]
            assertEquals(expectedDebug, Sendsay.runDebugMode, """
            Debug should be '$expectedDebug' for state '$debugOverride':'$safeOverride'
            """.trimIndent())
            val expectedSafe = env[expectedSafeModeIndex]
            assertEquals(expectedSafe, Sendsay.safeModeEnabled, """
            Expected to safeMode be '$expectedSafe' for state '$debugOverride':'$safeOverride'
            """.trimIndent())
        }
    }

    @Test
    @Config(application = DebugMockApplication::class)
    fun `MODE behaviour as expected for debug build after SDK init`() {
        initSdk()
        val debugModeOverrideIndex = 0
        val safeModeOverrideIndex = 1
        val expectedDebugModeIndex = 2
        val expectedSafeModeIndex = 3
        val behaviorRules = listOf(
            arrayOf(null, null, true, false),
            arrayOf(null, true, true, true),
            arrayOf(null, false, true, false),
            arrayOf(true, null, true, false),
            arrayOf(true, true, true, true),
            arrayOf(true, false, true, false),
            arrayOf(false, null, false, true),
            arrayOf(false, true, false, true),
            arrayOf(false, false, false, false)
        )
        behaviorRules.forEach { env ->
            val debugOverride = env[debugModeOverrideIndex]
            val safeOverride = env[safeModeOverrideIndex]
            Sendsay.runDebugModeOverride = debugOverride
            Sendsay.safeModeOverride = safeOverride
            val expectedDebug = env[expectedDebugModeIndex]
            assertEquals(expectedDebug, Sendsay.runDebugMode, """
            Debug should be '$expectedDebug' for state '$debugOverride':'$safeOverride'
            """.trimIndent())
            val expectedSafe = env[expectedSafeModeIndex]
            assertEquals(expectedSafe, Sendsay.safeModeEnabled, """
            Expected to safeMode be '$expectedSafe' for state '$debugOverride':'$safeOverride'
            """.trimIndent())
        }
    }

    private fun initSdk() {
        Sendsay.flushMode = FlushMode.MANUAL
        Sendsay.init(ApplicationProvider.getApplicationContext(), SendsayConfiguration(projectToken = "mock-token"))
    }
}
