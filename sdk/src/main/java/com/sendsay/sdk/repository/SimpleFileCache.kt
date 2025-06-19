package com.sendsay.sdk.repository

import android.content.Context
import com.sendsay.sdk.Sendsay
import com.sendsay.sdk.util.Logger
import com.sendsay.sdk.util.ThreadSafeAccess.Companion.waitForAccessWithDone
import com.sendsay.sdk.util.ThreadSafeAccess.Companion.waitForAccessWithResult
import com.sendsay.sdk.util.ensureOnBackgroundThread
import com.sendsay.sdk.util.logOnExceptionWithResult
import java.io.File
import java.io.IOException
import java.security.MessageDigest
import java.util.concurrent.TimeUnit.SECONDS
import java.util.concurrent.atomic.AtomicInteger
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Dispatcher
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

internal open class SimpleFileCache(context: Context, directoryPath: String) {
    companion object {
        internal const val DOWNLOAD_TIMEOUT_SECONDS = 10L
    }

    internal val httpClient = OkHttpClient.Builder()
        .dispatcher(Dispatcher().apply {
            maxRequestsPerHost = 100
            maxRequests = 100
        })
            .build()

    private val directory: File = File(context.filesDir, directoryPath)

    init {
        if (!directory.exists()) {
            directory.mkdir()
        }
    }

    fun getFileName(url: String): String {
        return MessageDigest
            .getInstance("SHA-512")
            .digest(url.toByteArray())
            .fold("") { str, it -> str + "%02x".format(it) }
    }

    fun clear() {
        val files = try {
            directory.listFiles()
        } catch (e: Exception) {
            Logger.e(this, "Unable to access ${directory.path}, please validate storage permissions", e)
            null
        }
        files?.forEach { file ->
            file.delete()
        }
    }

    fun preload(url: String, callback: ((Boolean) -> Unit)?) = waitForAccessWithDone(url) { releaseLock ->
        if (isFileDownloaded(url)) {
            releaseLock()
            callback?.invoke(true)
        } else {
            downloadFile(url) { downloaded ->
                releaseLock()
                callback?.invoke(downloaded)
            }
        }
    }

    private fun isFileDownloaded(url: String): Boolean {
        return retrieveFileDirectly(url).exists()
    }

    fun preload(urls: List<String>, callback: ((Boolean) -> Unit)?) {
        val distinctUrls = urls.distinct()
        if (urls.isEmpty()) {
            callback?.invoke(true)
            return
        }
        ensureOnBackgroundThread {
            val counter = AtomicInteger(distinctUrls.size)
            var allDownloaded = true
            for (each in distinctUrls) {
                preload(each) {
                    allDownloaded = allDownloaded && it
                    if (counter.decrementAndGet() <= 0) {
                        // all files are processed
                        runCatching {
                            callback?.invoke(allDownloaded)
                        }.logOnExceptionWithResult()
                    }
                }
            }
        }
//        for (fileUrl in urls) {
//            if (has(fileUrl)) {
//                perFileCallback.invoke(true)
//            } else {
//                downloadFile(fileUrl, perFileCallback)?.let {
//                    downloadQueue.add(it)
//                }
//            }
//        }
    }

    internal fun downloadFile(url: String, callback: ((Boolean) -> Unit)?) {
        val validUrl = url.toHttpUrlOrNull()
        if (validUrl == null) {
            callback?.invoke(false)
            return
        }
        if (Sendsay.isStopped) {
            Logger.e(this, "File $url now downloaded, SDK is stopping")
            callback?.invoke(false)
            return
        }
        val request = Request.Builder().url(validUrl).build()
        val downloadRequest = httpClient.newCall(request)
        downloadRequest.enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                if (Sendsay.isStopped) {
                    Logger.e(this, "File $url now downloaded, SDK is stopping")
                    callback?.invoke(false)
                } else if (response.isSuccessful) {
                    val file = createTempFile()
                    with(file.outputStream()) {
                        response.body?.byteStream()?.copyTo(this)
                        this.close()
                    }
                    file.renameTo(retrieveFileDirectly(url))
                    callback?.invoke(true)
                } else {
                    Logger.w(
                        this,
                        "Error while downloading file. Server responded ${response.code}"
                    )
                    callback?.invoke(false)
                }
                try {
                    response.close()
                } catch (e: Exception) {
                    Logger.e(this, "Error while closing http response", e)
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                Logger.w(this, "Error while downloading file from $url : $e")
                callback?.invoke(false)
            }
        })
    }

    fun has(url: String): Boolean = waitForAccessWithResult(url) {
        isFileDownloaded(url)
    }.getOrDefault(false)

    fun getFile(url: String): File? = waitForAccessWithResult(url) {
        retrieveFileDirectly(url).takeIf { it.exists() }
    }.getOrNull()

    internal fun retrieveFileDirectly(url: String) = File(directory, getFileName(url))
}
