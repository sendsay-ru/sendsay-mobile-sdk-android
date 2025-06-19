package com.sendsay.sdk.repository

import com.sendsay.sdk.models.InAppContentBlock
import com.sendsay.sdk.models.InAppContentBlockDisplayState
import java.util.Date

internal interface InAppContentBlockDisplayStateRepository {
    fun get(message: InAppContentBlock): InAppContentBlockDisplayState
    fun setDisplayed(message: InAppContentBlock, date: Date)
    fun setInteracted(message: InAppContentBlock, date: Date)
    fun clear()
}
