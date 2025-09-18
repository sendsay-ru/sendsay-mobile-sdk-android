package ru.sendsay.sdk.repository

import ru.sendsay.sdk.models.InAppContentBlock
import ru.sendsay.sdk.models.InAppContentBlockDisplayState
import java.util.Date

internal interface InAppContentBlockDisplayStateRepository {
    fun get(message: InAppContentBlock): InAppContentBlockDisplayState
    fun setDisplayed(message: InAppContentBlock, date: Date)
    fun setInteracted(message: InAppContentBlock, date: Date)
    fun clear()
}
