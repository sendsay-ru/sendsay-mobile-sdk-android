package com.sendsay.example.managers

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.chuckerteam.chucker.api.RetentionManager
import okhttp3.Call
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody

class NetworkManager(
    val context: Context
) {
    private val mediaTypeJson: MediaType = "application/json".toMediaTypeOrNull()!!
    private lateinit var networkClient: OkHttpClient

    init {
        setupNetworkClient()
    }

    private fun getNetworkInterceptor(): Interceptor {
        return Interceptor {
            val request = it.request()
            return@Interceptor try {
                it.proceed(request)
            } catch (e: Exception) {
                // Sometimes the request can fail due to SSL problems crashing the app. When that
                // happens, we return a dummy failed request
                val message = "Error: request canceled by $e"
                Response.Builder()
                    .code(400)
                    .protocol(Protocol.HTTP_2)
                    .message(message)
                    .request(it.request())
                    .body(message.toResponseBody("text/plain".toMediaTypeOrNull()))
                    .build()
            }
        }
    }

    private fun getChuckerInterceptor(context: Context): Interceptor {

// Create the Collector
        val chuckerCollector = ChuckerCollector(
            context = context,
            // Toggles visibility of the notification
            showNotification = true,
            // Allows to customize the retention period of collected data
            retentionPeriod = RetentionManager.Period.ONE_DAY
        )

// Create the Interceptor
        val chuckerInterceptor = ChuckerInterceptor.Builder(context)
            // The previously created Collector
            .collector(chuckerCollector)
            // The max body content length in bytes, after this responses will be truncated.
            .maxContentLength(250_000L)
            // List of headers to replace with ** in the Chucker UI
            .redactHeaders("Auth-Token", "Bearer")
            // Read the whole response body even when the client does not consume the response completely.
            // This is useful in case of parsing errors or when the response body
            // is closed before being read like in Retrofit with Void and Unit types.
            .alwaysReadResponseBody(true)
            // Use decoder when processing request and response bodies. When multiple decoders are installed they
            // are applied in an order they were added.
//            .addBodyDecoder(decoder)
            // Controls Android shortcut creation.
            .createShortcut(true)
            .build()

        return chuckerInterceptor
    }

    private fun setupNetworkClient() {
        val networkInterceptor = getNetworkInterceptor()
        val chuckerInterceptor = getChuckerInterceptor(context)

        networkClient = OkHttpClient.Builder()
            .addInterceptor(chuckerInterceptor)
            .addInterceptor(networkInterceptor)
            .build()
    }

    private fun request(method: String, url: String, authorization: String?, body: String?): Call {
        val requestBuilder = Request.Builder().url(url)

        requestBuilder.addHeader("Content-Type", "application/json")
        if (authorization != null) {
            requestBuilder.addHeader("Authorization", authorization)
        }

        if (body != null) {
            when (method) {
                "GET" -> requestBuilder.get()
                "POST" -> requestBuilder.post(body.toRequestBody(mediaTypeJson))
                else -> throw RuntimeException("Http method $method not supported.")
            }
            requestBuilder.post(body.toRequestBody(mediaTypeJson))
        }

        return networkClient.newCall(requestBuilder.build())
    }

    fun post(url: String, authorization: String?, body: String?): Call {
        return request("POST", url, authorization, body)
    }

    fun get(url: String, authorization: String?): Call {
        return request("GET", url, authorization, null)
    }
}
