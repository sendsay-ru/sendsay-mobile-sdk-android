package com.sendsay.sdk.manager

import com.sendsay.sdk.models.ExportedEvent
import com.sendsay.sdk.models.Segment
import com.sendsay.sdk.models.SegmentationDataCallback
import com.sendsay.sdk.services.OnIntegrationStoppedCallback

internal interface SegmentsManager : OnIntegrationStoppedCallback {
    fun onEventUploaded(event: ExportedEvent)
    fun onCallbackAdded(callback: SegmentationDataCallback)
    fun reload()
    fun clearAll()
    fun onSdkInit()
    fun fetchSegmentsManually(category: String, forceFetch: Boolean, callback: (List<Segment>) -> Unit)
    override fun onIntegrationStopped()
}
