package ru.sendsay.sdk.repository

import ru.sendsay.sdk.models.InAppMessage

internal interface InAppMessagesCache {
    fun get(): List<InAppMessage>
    fun set(messages: List<InAppMessage>)
    fun clear(): Boolean
    fun getTimestamp(): Long
}
