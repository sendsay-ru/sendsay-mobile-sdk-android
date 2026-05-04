package com.sendsay.sdk.network

import android.content.Context
import com.sendsay.sdk.models.SendsayConfiguration
import com.sendsay.sdk.util.Logger
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
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.brotli.BrotliInterceptor
import okhttp3.logging.HttpLoggingInterceptor

internal class NetworkHandlerImpl(
    private var sendsayConfiguration: SendsayConfiguration,
    private val context: Context
) : NetworkHandler {

    private val mediaTypeJson: MediaType = "application/json".toMediaTypeOrNull()!!
    private lateinit var networkClient: OkHttpClient

    init {
        setupNetworkClient()
    }

    private fun getNetworkInterceptor(): Interceptor {
        return Interceptor {
            var request = it.request()

            Logger.d(this, "Server address: ${request.url.host}")

            return@Interceptor try {
                it.proceed(request)
            } catch (e: Exception) {
                // Sometimes the request can fail due to SSL problems crashing the app. When that
                // happens, we return a dummy failed request
                Logger.w(this, e.toString())
                val message = "Error: request canceled by $e"
                Response.Builder()
                    .code(400)
                    .protocol(Protocol.HTTP_2)
                    .message(message)
                    .request(it.request())
                    .body(ResponseBody.create("text/plain".toMediaTypeOrNull(), message))
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

    private fun getNetworkLogger(): HttpLoggingInterceptor {
        val interceptor = HttpLoggingInterceptor()

        interceptor.level = when (sendsayConfiguration.httpLoggingLevel) {
            SendsayConfiguration.HttpLoggingLevel.NONE -> HttpLoggingInterceptor.Level.NONE
            SendsayConfiguration.HttpLoggingLevel.BASIC -> HttpLoggingInterceptor.Level.BASIC
            SendsayConfiguration.HttpLoggingLevel.HEADERS -> HttpLoggingInterceptor.Level.HEADERS
            SendsayConfiguration.HttpLoggingLevel.BODY -> HttpLoggingInterceptor.Level.BODY
        }

        return interceptor
    }

    private fun setupNetworkClient() {
        val networkInterceptor = getNetworkInterceptor()
        val chuckerInterceptor = getChuckerInterceptor(context)

        networkClient = OkHttpClient.Builder()
            .addInterceptor(getNetworkLogger())
            .addInterceptor(networkInterceptor)
            .addInterceptor(chuckerInterceptor)
            // keep after logging due to body logging
            .addInterceptor(BrotliInterceptor)
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
                "POST" -> requestBuilder.post(RequestBody.create(mediaTypeJson, body))
                else -> throw RuntimeException("Http method $method not supported.")
            }
            requestBuilder.post(RequestBody.create(mediaTypeJson, body))
        }

        return networkClient.newCall(requestBuilder.build())
    }

    override fun post(url: String, authorization: String?, body: String?): Call {
        return request("POST", url, authorization, body)
    }

    override fun get(url: String, authorization: String?): Call {
        return request("GET", url, authorization, null)
    }
}
