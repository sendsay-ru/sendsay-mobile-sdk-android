package com.sendsay.sdk.repository

import com.sendsay.sdk.models.CustomerIds
import com.sendsay.sdk.models.SegmentationData

internal interface SegmentsCache {
    fun get(): SegmentationData?
    fun set(segments: SegmentationData)
    fun clear(): Boolean
    fun isFresh(): Boolean
    fun isAssignedTo(customerIds: CustomerIds): Boolean
}
