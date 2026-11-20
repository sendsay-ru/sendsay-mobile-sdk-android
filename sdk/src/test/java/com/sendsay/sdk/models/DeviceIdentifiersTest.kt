package com.sendsay.sdk.models

import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class DeviceIdentifiersTest {

    @Test
    fun `GAID takes precedence when Google Play services provides it`() {
        runBlocking {
            val result = resolveDeviceIdentifiers(
                fetchGaid = { GaidResult.Available("test-gaid") },
                fetchOaid = { error("Unexpected OAID request") },
                fetchAndroidId = { error("Unexpected Android ID request") },
            )

            assertEquals("test-gaid", result.gaid)
            assertEquals(OaidResult.Unavailable.NOT_SELECTED, result.oaid)
            assertNull(result.androidId)
        }
    }

    @Test
    fun `OAID is requested when Google Play services is unavailable`() {
        runBlocking {
            val result = resolveDeviceIdentifiers(
                fetchGaid = { GaidResult.Unsupported },
                fetchOaid = { OaidResult.Available("test-oaid") },
                fetchAndroidId = { error("Unexpected Android ID request") },
            )

            assertNull(result.gaid)
            assertEquals(OaidResult.Available("test-oaid"), result.oaid)
            assertNull(result.androidId)
        }
    }

    @Test
    fun `Android ID is requested when both providers are unsupported`() {
        runBlocking {
            val result = resolveDeviceIdentifiers(
                fetchGaid = { GaidResult.Unsupported },
                fetchOaid = { OaidResult.Unavailable.UNSUPPORTED },
                fetchAndroidId = { "test-android-id" },
            )

            assertNull(result.gaid)
            assertEquals(OaidResult.Unavailable.UNSUPPORTED, result.oaid)
            assertEquals("test-android-id", result.androidId)
        }
    }

    @Test
    fun `a missing GAID from an existing provider stops fallback`() {
        runBlocking {
            val result = resolveDeviceIdentifiers(
                fetchGaid = { GaidResult.Unavailable },
                fetchOaid = { error("Unexpected OAID request") },
                fetchAndroidId = { error("Unexpected Android ID request") },
            )

            assertNull(result.gaid)
            assertEquals(OaidResult.Unavailable.NOT_SELECTED, result.oaid)
            assertNull(result.androidId)
        }
    }

    @Test
    fun `a missing OAID from an existing provider stops fallback`() {
        runBlocking {
            val reasons = listOf(
                OaidResult.Unavailable.LIMITED_BY_USER,
                OaidResult.Unavailable.EMPTY_RESPONSE,
                OaidResult.Unavailable.TIMEOUT,
                OaidResult.Unavailable.FETCH_FAILED,
            )

            for (reason in reasons) {
                val result = resolveDeviceIdentifiers(
                    fetchGaid = { GaidResult.Unsupported },
                    fetchOaid = { reason },
                    fetchAndroidId = { error("Unexpected Android ID request") },
                )

                assertNull(result.gaid)
                assertEquals(reason, result.oaid)
                assertNull(result.androidId)
            }
        }
    }

    @Test
    fun `a zeroed advertising ID is unusable`() {
        assertNull("00000000-0000-0000-0000-000000000000".usableAdvertisingId())
    }
}
