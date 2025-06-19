package com.sendsay.sdk.repository

import com.sendsay.sdk.models.InAppMessage

internal interface InAppMessagesCache {
    fun get(): List<InAppMessage>
    fun set(messages: List<InAppMessage>)
    fun clear(): Boolean
    fun getTimestamp(): Long
}
