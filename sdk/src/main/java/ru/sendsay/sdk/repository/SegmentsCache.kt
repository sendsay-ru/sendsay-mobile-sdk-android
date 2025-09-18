package ru.sendsay.sdk.repository

import ru.sendsay.sdk.models.CustomerIds
import ru.sendsay.sdk.models.SegmentationData

internal interface SegmentsCache {
    fun get(): SegmentationData?
    fun set(segments: SegmentationData)
    fun clear(): Boolean
    fun isFresh(): Boolean
    fun isAssignedTo(customerIds: CustomerIds): Boolean
}
