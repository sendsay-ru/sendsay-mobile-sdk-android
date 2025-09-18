package ru.sendsay.sdk.telemetry.storage

import android.app.Application
import ru.sendsay.sdk.telemetry.model.CrashLog
import com.google.gson.Gson
import ru.sendsay.sdk.Sendsay
import ru.sendsay.sdk.util.Logger
import java.io.File

internal class FileTelemetryStorage(private val application: Application) : TelemetryStorage {
    companion object {
        const val CRASHLOG_FILE_PREFIX = "sendsaysdk_crashlog_"
        const val DIRECTORY = "sendsaysdk_telemetry_storage"
    }

    private fun getLogsDirectory(): File? {
        val directory = File(application.cacheDir, DIRECTORY)
        if (directory.exists()) {
            return directory
        }
        if (directory.mkdir()) {
            return directory
        }
        return null
    }

    internal fun getFileName(log: CrashLog): String = CRASHLOG_FILE_PREFIX + log.id + ".json"

    override fun saveCrashLog(log: CrashLog) {
        if (Sendsay.isStopped) {
            Logger.e(this, "Crash log was not saved, SDK is stopping")
            return
        }
        try {
            File(getLogsDirectory() ?: return, getFileName(log)).writeText(Gson().toJson(log))
        } catch (e: Exception) {
            // do nothing
        }
    }

    override fun deleteCrashLog(log: CrashLog) {
        try {
            File(getLogsDirectory() ?: return, getFileName(log)).delete()
        } catch (e: Exception) {
            // do nothing
        }
    }

    override fun getAllCrashLogs(): List<CrashLog> {
        if (Sendsay.isStopped) {
            Logger.e(this, "Crash logs not accessible, SDK is stopping")
            return arrayListOf()
        }
        try {
            val directory = getLogsDirectory() ?: return arrayListOf()
            val files = directory.listFiles { _: File, name: String ->
                name.startsWith(CRASHLOG_FILE_PREFIX)
            }
            val crashLogs = arrayListOf<CrashLog>()
            files?.forEach { file ->
                try {
                    crashLogs.add(Gson().fromJson(file.readText(), CrashLog::class.java))
                } catch (e: Exception) {
                    file.delete()
                }
            }
            return crashLogs.sortedBy { it.timestampMS }
        } catch (e: Exception) {
            return arrayListOf()
        }
    }

    override fun clear() {
        getLogsDirectory()?.deleteRecursively()
    }
}
