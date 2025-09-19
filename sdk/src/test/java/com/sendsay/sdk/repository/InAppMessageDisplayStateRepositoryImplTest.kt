package com.sendsay.sdk.repository

import androidx.test.core.app.ApplicationProvider
import com.sendsay.sdk.models.InAppMessageDisplayState
import com.sendsay.sdk.models.InAppMessageTest
import com.sendsay.sdk.preferences.SendsayPreferences
import com.sendsay.sdk.preferences.SendsayPreferencesImpl
import com.sendsay.sdk.util.SendsayGson
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.time.temporal.ChronoUnit
import java.util.Date
import kotlin.test.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class InAppMessageDisplayStateRepositoryImplTest {
    private lateinit var repo: InAppMessageDisplayStateRepository
    private lateinit var prefs: SendsayPreferences
    private val message = InAppMessageTest.buildInAppMessage()

    @Before
    fun before() {
        prefs = SendsayPreferencesImpl(ApplicationProvider.getApplicationContext())
        repo = InAppMessageDisplayStateRepositoryImpl(prefs, SendsayGson.instance)
    }

    @Test
    fun `should return empty data`() {
        assertEquals(InAppMessageDisplayState(null, null), repo.get(InAppMessageTest.buildInAppMessage()))
    }

    @Test
    fun `should save data`() {
        repo.setDisplayed(message, Date(1000))
        assertEquals(Date(1000), repo.get(message).displayed)
        repo.setInteracted(message, Date(2000))
        assertEquals(InAppMessageDisplayState(Date(1000), Date(2000)), repo.get(message))
        repo.setDisplayed(message, Date(3000))
        assertEquals(InAppMessageDisplayState(Date(3000), Date(2000)), repo.get(message))
    }

    @Test
    fun `should keep data between instances`() {
        // need to round of milliseconds that would get lost in json serialization
        val now = Date.from(Date().toInstant().truncatedTo(ChronoUnit.SECONDS))
        repo.setDisplayed(message, now)
        repo = InAppMessageDisplayStateRepositoryImpl(
            SendsayPreferencesImpl(ApplicationProvider.getApplicationContext()),
            Gson()
        )
        assertEquals(InAppMessageDisplayState(now, null), repo.get(message))
    }

    @Test
    fun `should read data in old format`() {
        prefs.setString(
            InAppMessageDisplayStateRepositoryImpl.KEY,
            """{"mock-id":{"displayed":"Jul 22, 2020 12:21:56 PM"}}"""
        )
        assertEquals(
            SimpleDateFormat(InAppMessageDisplayStateRepositoryImpl.LEGACY_DATE_FORMAT)
                .parse("Jul 22, 2020 12:21:56 PM"),
            repo.get(InAppMessageTest.buildInAppMessage("mock-id")).displayed
        )
        assertEquals(null, repo.get(InAppMessageTest.buildInAppMessage("mock-id")).interacted)
    }

    @Test
    fun `should provide default for unreadable date format`() {
        prefs.setString(
            InAppMessageDisplayStateRepositoryImpl.KEY,
            """{
                "mock-id":{"displayed":"Jul 22, 2020 12:21:56 PM"},
                "mock-id2":{"displayed":"Jul 22, 2020 12:21:56"}
            }"""
        )
        assertEquals(
            SimpleDateFormat(InAppMessageDisplayStateRepositoryImpl.LEGACY_DATE_FORMAT)
                .parse("Jul 22, 2020 12:21:56 PM"),
            repo.get(InAppMessageTest.buildInAppMessage("mock-id")).displayed
        )
        assertEquals(Date(0), repo.get(InAppMessageTest.buildInAppMessage("mock-id2")).displayed)
    }

    @Test
    fun `should delete old data`() {
        repo.setDisplayed(message, Date(1000))
        repo = InAppMessageDisplayStateRepositoryImpl(
            SendsayPreferencesImpl(ApplicationProvider.getApplicationContext()),
            Gson()
        )
        assertEquals(InAppMessageDisplayState(null, null), repo.get(message))
    }
}
