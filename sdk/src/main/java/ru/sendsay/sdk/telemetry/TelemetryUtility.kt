package ru.sendsay.sdk.telemetry

import android.content.Context
import ru.sendsay.sdk.models.SendsayConfiguration
import ru.sendsay.sdk.telemetry.model.ErrorData
import ru.sendsay.sdk.telemetry.model.ErrorStackTraceElement
import kotlin.math.min
import kotlin.reflect.KProperty1

object TelemetryUtility {
    private const val SDK_PACKAGE = "ru.sendsay"
    private const val MAX_STACK_TRACE_LENGTH = 100

    internal fun getErrorData(e: Throwable): ErrorData {
        return getErrorDataInternal(e, hashSetOf())
    }

    /**
     * There can be loops in cause hierarchy
     * We need to remember which throwables we already visited and break in case of a loop
     */
    private fun getErrorDataInternal(e: Throwable, visitedThrowables: HashSet<Throwable>): ErrorData {
        visitedThrowables.add(e)
        var cause: ErrorData? = null
        if (e.cause != null) {
            val throwableCause = e.cause as Throwable
            if (!visitedThrowables.contains(throwableCause)) {
                cause = getErrorDataInternal(throwableCause, visitedThrowables)
            }
        }
        val stackTrace = e.stackTrace.slice(0 until min(MAX_STACK_TRACE_LENGTH, e.stackTrace.size)).map {
            ErrorStackTraceElement(it.className, it.methodName, it.fileName, it.lineNumber)
        }

        return ErrorData(
            type = e.javaClass.name,
            message = e.message ?: "",
            stackTrace = stackTrace,
            cause = cause
        )
    }

    internal fun isSDKRelated(e: Throwable): Boolean {
        var t: Throwable? = e
        var visited = hashSetOf<Throwable>()
        while (t != null && !visited.contains(t)) {
            t.stackTrace.forEach { if (it.className.startsWith(SDK_PACKAGE)) return true }
            visited.add(t)
            t = t.cause
        }
        return false
    }

    internal fun formatConfigurationForTracking(configuration: SendsayConfiguration): HashMap<String, String> {
        val defaultConfiguration = SendsayConfiguration()
        val isDefault = { property: KProperty1<SendsayConfiguration, Any?> ->
            property.get(configuration) == property.get(defaultConfiguration)
        }
        val formatConfigurationProperty = { property: KProperty1<SendsayConfiguration, Any?> ->
            "${property.get(configuration)}${if (isDefault(property)) " [default]" else ""}"
        }
        return hashMapOf(
            "projectRouteMap"
                to if (configuration.projectRouteMap.isNotEmpty()) "[REDACTED]" else "[]",
            "baseURL"
                to formatConfigurationProperty(SendsayConfiguration::baseURL),
            "httpLoggingLevel"
                to formatConfigurationProperty(SendsayConfiguration::httpLoggingLevel),
            "maxTries"
                to formatConfigurationProperty(SendsayConfiguration::maxTries),
            "sessionTimeout"
                to formatConfigurationProperty(SendsayConfiguration::sessionTimeout),
            "campaignTTL"
                to formatConfigurationProperty(SendsayConfiguration::campaignTTL),
            "automaticSessionTracking"
                to formatConfigurationProperty(SendsayConfiguration::automaticSessionTracking),
            "automaticPushNotification"
                to formatConfigurationProperty(SendsayConfiguration::automaticPushNotification),
            "pushIcon"
                to formatConfigurationProperty(SendsayConfiguration::pushIcon),
            "pushChannelName"
                to formatConfigurationProperty(SendsayConfiguration::pushChannelName),
            "pushChannelDescription"
                to formatConfigurationProperty(SendsayConfiguration::pushChannelDescription),
            "pushChannelId"
                to formatConfigurationProperty(SendsayConfiguration::pushChannelId),
            "pushNotificationImportance"
                to formatConfigurationProperty(SendsayConfiguration::pushNotificationImportance),
            "defaultProperties"
                to if (configuration.defaultProperties.isNotEmpty()) "[REDACTED]" else "[]",
            "tokenTrackFrequency"
                to formatConfigurationProperty(SendsayConfiguration::tokenTrackFrequency)
            // TODO: this should be uploaded as well, but AppCenter has limitation to 20 properties
            // "allowDefaultCustomerProperties"
            //      to formatConfigurationProperty(SendsayConfiguration::allowDefaultCustomerProperties)
        )
    }

    internal data class AppInfo(
        val packageName: String,
        val versionName: String,
        val versionCode: String
    )

    internal fun getAppInfo(context: Context): AppInfo {
        try {
            val packageManager = context.packageManager
            val packageInfo = packageManager.getPackageInfo(context.packageName, 0)
            @Suppress("DEPRECATION")
            return AppInfo(
                packageInfo.packageName,
                packageInfo.versionName ?: "unknown version",
                packageInfo.versionCode.toString()
            )
        } catch (e: Exception) {
            return AppInfo("unknown package", "unknown version", "unknown version code")
        }
    }
}
