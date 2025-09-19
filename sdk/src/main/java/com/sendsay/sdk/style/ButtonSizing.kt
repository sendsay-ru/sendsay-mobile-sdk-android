package com.sendsay.sdk.style

import com.sendsay.sdk.util.Logger

internal enum class ButtonSizing(val value: String) {
    HUG_TEXT("hug"),
    FILL("fill");

    companion object {
        fun parse(from: String?): ButtonSizing? {
            return ButtonSizing.values().firstOrNull {
                it.value == from
            } ?: kotlin.run {
                Logger.e(this, "Unable to parse button sizing: $from")
                null
            }
        }
    }
}
