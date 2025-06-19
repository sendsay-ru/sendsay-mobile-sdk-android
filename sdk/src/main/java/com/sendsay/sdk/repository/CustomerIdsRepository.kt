package com.sendsay.sdk.repository

import com.sendsay.sdk.models.CustomerIds

internal interface CustomerIdsRepository {

    fun get(): CustomerIds

    fun set(customerIds: CustomerIds)

    fun clear()
}
