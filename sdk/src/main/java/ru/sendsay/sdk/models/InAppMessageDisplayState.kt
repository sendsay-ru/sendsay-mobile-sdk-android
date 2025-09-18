package ru.sendsay.sdk.models

import java.util.Date

data class InAppMessageDisplayState(
    val displayed: Date?,
    val interacted: Date?
)
