package com.sendsay.sdk.telemetry.storage

import com.sendsay.sdk.telemetry.model.CrashLog

internal interface TelemetryStorage {
    fun saveCrashLog(log: CrashLog)
    fun deleteCrashLog(log: CrashLog)
    fun getAllCrashLogs(): List<CrashLog>
    fun clear()
}
