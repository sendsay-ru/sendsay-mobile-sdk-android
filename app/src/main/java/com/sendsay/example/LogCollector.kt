package com.sendsay.example

import kotlinx.coroutines.flow.MutableStateFlow

object LogCollector {

    private val _logs = MutableStateFlow<MutableList<SdkLog>>(mutableListOf())
    val logs = _logs

    fun info(sdk: String, msg: String) =
        append(Level.INFO, sdk, msg)

    fun error(sdk: String, msg: String) =
        append(Level.ERROR, sdk, msg)

    private fun append(level: Level, sdk: String, msg: String) {
        _logs.value += SdkLog(
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
    override fun toString(): String {
        return "[$time] [$sdk] [$level] $message"
    }
}

enum class Level {
    INFO,
    WARNING,
    ERROR
}