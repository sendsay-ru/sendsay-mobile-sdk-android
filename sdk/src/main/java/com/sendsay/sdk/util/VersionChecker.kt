package com.sendsay.sdk.util

import android.content.Context
import com.sendsay.sdk.BuildConfig
import com.sendsay.sdk.network.NetworkHandler
import java.io.IOException
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response

internal class VersionChecker(
    private val networkManager: NetworkHandler,
    private val context: Context
) {
    private val baseUrl = "https://api.github.com/repos/sendsay/%s/releases/latest"

    private data class GitHubReleaseResponse(val tag_name: String)

    fun warnIfNotLatestSDKVersion() {
        val actualVersion: String?
        val gitProject: String
        when {
            context.isReactNativeSDK() -> {
                actualVersion = context.getReactNativeSDKVersion()
                gitProject = "sendsay-react-native-sdk"
            }
            context.isFlutterSDK() -> {
                actualVersion = context.getFlutterSDKVersion()
                gitProject = "sendsay-flutter-sdk"
            }
            context.isXamarinSDK() -> {
                actualVersion = context.getXamarinSDKVersion()
                gitProject = "sendsay-xamarin-sdk"
            }
            context.isMauiSDK() -> {
                actualVersion = context.getMauiSDKVersion()
                gitProject = "bloomreach-maui-sdk"
            }
            else -> {
                actualVersion = BuildConfig.SENDSAY_VERSION_NAME
                gitProject = "sendsay-android-sdk"
            }
        }
        val url = String.format(baseUrl, gitProject)

        if (actualVersion != null) {
            networkManager.get(url).enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    try {
                        if (response.isSuccessful) {
                            val parsedResponse = SendsayGson.instance.fromJson(
                                response.body?.string(),
                                GitHubReleaseResponse::class.java
                            )
                            val lastVersion = parsedResponse.tag_name
                            if (compareVersions(lastVersion, actualVersion) > 0) {
                                Logger.e(
                                "SENDSAY",
                                "####\n" +
                                "#### A newer version of the Sendsay SDK is available!\n" +
                                "#### Your version: $actualVersion  Last version: $lastVersion\n" +
                                "#### Upgrade to the latest version to benefit from the new features " +
                                        "and better stability:\n" +
                                "#### https://gitlab.sndsy.ru/$gitProject/releases\n" +
                                "####"
                                )
                            }
                        }
                    } catch (e: Exception) {
                        val error = e.localizedMessage ?: "Unknown error"
                        Logger.e(this, "Failed to retrieve last Sendsay SDK version: $error")
                    } finally {
                        response.close()
                    }
                }

                override fun onFailure(call: Call, e: IOException) {
                    val error = e.localizedMessage ?: "Unknown error"
                    Logger.e(this, "Failed to retrieve last Sendsay SDK version: $error")
                }
            })
        }
    }

    private fun compareVersions(version1: String, version2: String): Int {
        var comparisonResult = 0
        val version1Splits = version1.split(".").toTypedArray()
        val version2Splits = version2.split(".").toTypedArray()
        val maxLengthOfVersionSplits = version1Splits.size.coerceAtLeast(version2Splits.size)
        for (i in 0 until maxLengthOfVersionSplits) {
            val v1 = if (i < version1Splits.size) version1Splits[i].toInt() else 0
            val v2 = if (i < version2Splits.size) version2Splits[i].toInt() else 0
            val compare = v1.compareTo(v2)
            if (compare != 0) {
                comparisonResult = compare
                break
            }
        }
        return comparisonResult
    }
}
