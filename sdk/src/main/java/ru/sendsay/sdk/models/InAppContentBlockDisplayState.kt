package ru.sendsay.sdk.models

import java.util.Date

data class InAppContentBlockDisplayState(
    val displayedLast: Date?,
    val displayedCount: Int,
    val interactedLast: Date?,
    val interactedCount: Int
)
