package com.sendsay.example

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LogCollector {

    companion object {
        val instance: LogCollector = LogCollector()
    }

    private val _logs = MutableStateFlow<List<SdkLog>>(emptyList())
    val logs: StateFlow<List<SdkLog>> = _logs

    fun info(sdk: String, msg: String) =
        append(Level.INFO, sdk, msg)

    fun error(sdk: String, msg: String) =
        append(Level.ERROR, sdk, msg)

    private fun append(level: Level, sdk: String, msg: String) {
        _logs.update {
            it + SdkLog(
                System.currentTimeMillis(),
                sdk,
                level,
                msg
            )
        }
    }

    data class SdkLog(
        val time: Long,
        val sdk: String,
        val level: Level,
        val message: String
    ) {
        fun timeHumanizer(long: Long): String {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            return dateFormat.format(Date(time))
        }

        override fun toString(): String {
            return "[${timeHumanizer(time)}] [$sdk] [$level] $message"
        }
    }

    enum class Level {
        INFO,
        WARNING,
        ERROR
    }
}