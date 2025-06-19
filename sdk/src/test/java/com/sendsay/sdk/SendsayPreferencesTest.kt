package com.sendsay.sdk

import androidx.test.core.app.ApplicationProvider
import com.sendsay.sdk.preferences.SendsayPreferences
import com.sendsay.sdk.preferences.SendsayPreferencesImpl
import com.sendsay.sdk.testutil.SendsaySDKTest
import kotlin.test.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class SendsayPreferencesTest : SendsaySDKTest() {

    companion object {
        const val VAL_BOOL = "booleanValue"
        const val VAL_STRING = "stringValue"
    }

    private lateinit var prefs: SendsayPreferences

    @Before
    fun init() {
        prefs = SendsayPreferencesImpl(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun setBoolean_ShouldPass() {
        val toSet = false
        prefs.setBoolean(VAL_BOOL, toSet)
        assertEquals(toSet, prefs.getBoolean(VAL_BOOL, true))
    }

    @Test
    fun setString_ShouldPass() {
        val toSet = "sampleString"
        prefs.setString(VAL_STRING, toSet)
        assertEquals(toSet, prefs.getString(VAL_STRING, "wrong one"))
    }

    @Test
    fun remove_ShouldPass() {
        val value = "someOtherString"
        prefs.setString(VAL_STRING, value)
        assertEquals(value, prefs.getString(VAL_STRING, "deleted"))
        assertEquals(true, prefs.remove(VAL_STRING))
        assertEquals("deleted", prefs.getString(VAL_STRING, "deleted"))
    }
}
