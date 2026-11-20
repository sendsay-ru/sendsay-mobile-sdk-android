package com.sendsay.sdk.models

import kotlin.test.assertEquals
import org.junit.Test

internal class OaidResultTest {

    @Test
    fun `reports a Huawei restriction even when an ID is returned`() {
        assertEquals(
            OaidResult.Unavailable.LIMITED_BY_USER,
            OaidResult.fromProvider(
                "test-oaid",
                isLimitAdTrackingEnabled = true,
            ),
        )
    }

    @Test
    fun `reports empty response when provider has no ID or tracking flag`() {
        assertEquals(
            OaidResult.Unavailable.EMPTY_RESPONSE,
            OaidResult.fromProvider(null),
        )
        assertEquals(
            OaidResult.Unavailable.EMPTY_RESPONSE,
            OaidResult.fromProvider("00000000-0000-0000-0000-000000000000"),
        )
    }

    @Test
    fun `accepts a usable ID from a provider without a tracking flag`() {
        assertEquals(
            OaidResult.Available("test-oaid"),
            OaidResult.fromProvider(" test-oaid "),
        )
    }
}
