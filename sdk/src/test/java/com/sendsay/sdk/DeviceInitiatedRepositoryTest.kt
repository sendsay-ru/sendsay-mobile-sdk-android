package com.sendsay.sdk

import androidx.test.core.app.ApplicationProvider
import com.sendsay.sdk.preferences.SendsayPreferencesImpl
import com.sendsay.sdk.repository.DeviceInitiatedRepository
import com.sendsay.sdk.repository.DeviceInitiatedRepositoryImpl
import com.sendsay.sdk.testutil.SendsaySDKTest
import kotlin.test.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class DeviceInitiatedRepositoryTest : SendsaySDKTest() {

    private lateinit var repo: DeviceInitiatedRepository

    @Before
    fun init() {
        val prefs = SendsayPreferencesImpl(ApplicationProvider.getApplicationContext())
        repo = DeviceInitiatedRepositoryImpl(prefs)
    }

    @Test
    fun testGet_ShouldPassed() {
        val value = true
        repo.set(value)
        assertEquals(true, repo.get())
    }
}
