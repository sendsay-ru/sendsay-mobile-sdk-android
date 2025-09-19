package com.sendsay.sdk.style

import com.sendsay.sdk.util.Logger

internal enum class ButtonAlignment(val value: String) {
    LEFT("left"),
    CENTER("center"),
    RIGHT("right");

    companion object {
        fun parse(from: String?): ButtonAlignment? {
            return ButtonAlignment.values().firstOrNull {
                it.value == from
            } ?: kotlin.run {
                Logger.e(this, "Unable to parse Button alignment: $from")
                null
            }
        }
    }
}
