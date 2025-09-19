package com.sendsay.sdk.models

import com.sendsay.sdk.Sendsay

abstract class SegmentationDataCallback {
    abstract val exposingCategory: String
    abstract val includeFirstLoad: Boolean
    abstract fun onNewData(segments: List<Segment>)
    final fun unregister() {
        Sendsay.unregisterSegmentationDataCallback(this)
    }
}
