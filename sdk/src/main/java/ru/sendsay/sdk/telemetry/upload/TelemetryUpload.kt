package ru.sendsay.sdk.telemetry.upload

import ru.sendsay.sdk.telemetry.model.CrashLog
import ru.sendsay.sdk.telemetry.model.EventLog

internal interface TelemetryUpload {
    fun uploadSessionStart(runId: String, callback: (Result<Unit>) -> Unit)
    fun uploadCrashLog(log: CrashLog, callback: (Result<Unit>) -> Unit)
    fun uploadEventLog(log: EventLog, callback: (Result<Unit>) -> Unit)
}
