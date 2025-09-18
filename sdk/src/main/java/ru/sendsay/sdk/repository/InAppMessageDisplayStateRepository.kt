package ru.sendsay.sdk.repository

import ru.sendsay.sdk.models.InAppMessage
import ru.sendsay.sdk.models.InAppMessageDisplayState
import java.util.Date

internal interface InAppMessageDisplayStateRepository {
    fun get(message: InAppMessage): InAppMessageDisplayState
    fun setDisplayed(message: InAppMessage, date: Date)
    fun setInteracted(message: InAppMessage, date: Date)
    fun clear()
}
