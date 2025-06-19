package com.sendsay.sdk.manager

import android.os.Build
import com.sendsay.sdk.models.CustomerRecommendation
import com.sendsay.sdk.models.CustomerRecommendationOptions
import com.sendsay.sdk.models.CustomerRecommendationRequest
import com.sendsay.sdk.models.SendsayProject
import com.sendsay.sdk.models.FetchError
import com.sendsay.sdk.models.Result
import com.sendsay.sdk.testutil.mocks.SendsayMockService
import com.sendsay.sdk.testutil.waitForIt
import com.sendsay.sdk.util.SendsayGson
import com.google.gson.JsonPrimitive
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

@RunWith(RobolectricTestRunner::class)
internal class FetchManagerRecommendationTest {

    @Before
    fun setUp() {
    }

    fun getResponse(response: String): ResponseBody {
        return response.toResponseBody("application/json".toMediaTypeOrNull())
    }

    private fun runTest(
        mockResponse: String,
        expectedResult: Result<ArrayList<CustomerRecommendation>>? = null,
        expectedErrorResult: Result<FetchError>? = null
    ) {
        waitForIt {
            FetchManagerImpl(
                SendsayMockService(true, getResponse(mockResponse)),
                SendsayGson.instance
            ).fetchRecommendation(
                SendsayProject("mock-base-url.com", "mock-project-token", "mock-auth"),
                CustomerRecommendationRequest(
                    customerIds = hashMapOf("cookie" to "mock-cookie"),
                    options = CustomerRecommendationOptions(id = "mock-id", fillWithRandom = true)
                ),
                { result ->
                    if (expectedResult == null) {
                        it.fail("Unexpected result")
                    }
                    it.assertEquals(expectedResult, result)
                    it()
                },
                { result: Result<FetchError> ->
                    if (expectedErrorResult == null) {
                        it.fail("Unexpected result")
                    }

                    it.assertEquals(expectedErrorResult, result)
                    it()
                }
            )
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    @LooperMode(LooperMode.Mode.LEGACY)
    fun `should return error for non-existing user`() {
        val payload = """
        {
          "errors": {
            "_global": [
              "Customer does not exist"
            ]
          },
          "success": false
        }
        """
        runTest(
            mockResponse = payload,
            expectedErrorResult = Result(
                false,
                FetchError(null, "Failure state from server returned")
            )
        )
    }

    @Test
    fun `should return error for non-existing recommendation`() {
        val payload = """
        {
          "results": [
            {
              "error": "Not Found",
              "success": false
            }
          ],
          "success": true
        }
        """
        runTest(
            mockResponse = payload,
            expectedErrorResult = Result(false, FetchError(null, "Not Found"))
        )
    }

    @Test
    fun `should return result for recommendation`() {
        val payload = """
        {
          "results": [
            {
              "success": true,
              "value": [
                {
                  "description": "an awesome book",
                  "engine_name": "random",
                  "image": "no image available",
                  "item_id": "1",
                  "name": "book",
                  "price": 19.99,
                  "product_id": "1",
                  "recommendation_id": "5dd6af3d147f518cb457c63c",
                  "recommendation_variant_id": null
                },
                {
                  "description": "super awesome off-brand phone",
                  "engine_name": "random",
                  "image": "just google one",
                  "item_id": "3",
                  "name": "mobile phone",
                  "price": 499.99,
                  "product_id": "3",
                  "recommendation_id": "5dd6af3d147f518cb457c63c",
                  "recommendation_variant_id": "mock id"
                }
              ]
            }
          ],
          "success": true
        }
        """
        runTest(
            mockResponse = payload,
            expectedResult = Result(true, arrayListOf(
                CustomerRecommendation(
                    itemId = "1",
                    engineName = "random",
                    recommendationId = "5dd6af3d147f518cb457c63c",
                    recommendationVariantId = null,
                    data = hashMapOf(
                        "name" to JsonPrimitive("book"),
                        "description" to JsonPrimitive("an awesome book"),
                        "image" to JsonPrimitive("no image available"),
                        "price" to JsonPrimitive(19.99),
                        "product_id" to JsonPrimitive("1")
                    )
                ),
                CustomerRecommendation(
                    itemId = "3",
                    engineName = "random",
                    recommendationId = "5dd6af3d147f518cb457c63c",
                    recommendationVariantId = "mock id",
                    data = hashMapOf(
                        "name" to JsonPrimitive("mobile phone"),
                        "description" to JsonPrimitive("super awesome off-brand phone"),
                        "image" to JsonPrimitive("just google one"),
                        "price" to JsonPrimitive(499.99),
                        "product_id" to JsonPrimitive("3")
                    )
                )
            ))
        )
    }
}
