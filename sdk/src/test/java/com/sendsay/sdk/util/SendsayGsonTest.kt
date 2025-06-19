package com.sendsay.sdk.util

import com.sendsay.sdk.testutil.SendsaySDKTest
import java.util.Date
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class SendsayGsonTest : SendsaySDKTest() {
    @Test
    fun `should serialize obscure number values`() {
        assertEquals(
            """{"zero":0.0,"inf":"Infinity","pi":3.14159,"-inf":"-Infinity","nan":"NaN"}""",
            SendsayGson.instance.toJson(hashMapOf(
                "pi" to 3.14159f,
                "zero" to 0.0f,
                "inf" to Float.POSITIVE_INFINITY,
                "-inf" to Float.NEGATIVE_INFINITY,
                "nan" to Float.NaN
            ))
        )
        assertEquals(
            """{"zero":0.0,"inf":"Infinity","pi":3.14159,"-inf":"-Infinity","nan":"NaN"}""",
            SendsayGson.instance.toJson(hashMapOf(
                "pi" to 3.14159,
                "zero" to 0.0,
                "inf" to Double.POSITIVE_INFINITY,
                "-inf" to Double.NEGATIVE_INFINITY,
                "nan" to Double.NaN
            ))
        )
    }

    @Test
    fun `any soon change to builder should not affect instance singleton`() {
        val dataToSerialize = mapOf(
            "html" to "<html><body>Minimal</body></html>",
            "date" to Date()
        )
        val changedInstance = SendsayGson.builder
            .generateNonExecutableJson()
            .setDateFormat(java.text.DateFormat.MEDIUM)
            .create()
        val serializedBySDK = SendsayGson.instance.toJson(dataToSerialize)
        val serializedByCustomInstance = changedInstance.toJson(dataToSerialize)
        assertNotEquals(serializedBySDK, serializedByCustomInstance)
    }

    @Test
    fun `any change to builder should not affect instance singleton`() {
        val dataToSerialize = mapOf(
            "html" to "<html><body>Minimal</body></html>",
            "date" to Date()
        )
        val serializedBySDK = SendsayGson.instance.toJson(dataToSerialize)
        val changedInstance = SendsayGson.builder
            .generateNonExecutableJson()
            .setDateFormat(java.text.DateFormat.MEDIUM)
            .create()
        val serializedByCustomInstance = changedInstance.toJson(dataToSerialize)
        val serializedBySdkAfterChange = SendsayGson.instance.toJson(dataToSerialize)
        assertEquals(serializedBySDK, serializedBySdkAfterChange)
        assertNotEquals(serializedBySDK, serializedByCustomInstance)
    }
}
