package ru.sendsay.sdk.repository

import ru.sendsay.sdk.models.CustomerIds

internal interface CustomerIdsRepository {

    fun get(): CustomerIds

    fun set(customerIds: CustomerIds)

    fun clear()
}
