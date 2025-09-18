package ru.sendsay.sdk.models

internal data class SegmentationData(
    val customerIds: CustomerIds,
    val segmentations: SegmentationCategories,
    val updatedAtMillis: Long?
)
