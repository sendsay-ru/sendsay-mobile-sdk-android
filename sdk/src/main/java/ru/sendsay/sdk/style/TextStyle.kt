package ru.sendsay.sdk.style

import ru.sendsay.sdk.util.Logger

internal enum class TextStyle(val value: String) {
    BOLD("bold"),
    ITALIC("italic"),
    REGULAR("regular");

    companion object {
        fun parse(from: String?): TextStyle? {
            return TextStyle.values().firstOrNull {
                it.value == from
            } ?: kotlin.run {
                Logger.e(this, "Unable to parse text style: $from")
                null
            }
        }
    }
}
