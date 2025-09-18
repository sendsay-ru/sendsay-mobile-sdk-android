package ru.sendsay.sdk

import androidx.test.core.app.ApplicationProvider
import ru.sendsay.sdk.preferences.SendsayPreferencesImpl
import ru.sendsay.sdk.repository.DeviceInitiatedRepository
import ru.sendsay.sdk.repository.DeviceInitiatedRepositoryImpl
import ru.sendsay.sdk.testutil.SendsaySDKTest
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
