package ru.sendsay.sdk.repository

internal interface UniqueIdentifierRepository {
    fun get(): String
    fun clear(): Boolean
}
