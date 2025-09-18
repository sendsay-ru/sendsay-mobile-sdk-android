package ru.sendsay.sdk.manager

import ru.sendsay.sdk.models.ExportedEvent
import ru.sendsay.sdk.models.Segment
import ru.sendsay.sdk.models.SegmentationDataCallback
import ru.sendsay.sdk.services.OnIntegrationStoppedCallback

internal interface SegmentsManager : OnIntegrationStoppedCallback {
    fun onEventUploaded(event: ExportedEvent)
    fun onCallbackAdded(callback: SegmentationDataCallback)
    fun reload()
    fun clearAll()
    fun onSdkInit()
    fun fetchSegmentsManually(category: String, forceFetch: Boolean, callback: (List<Segment>) -> Unit)
    override fun onIntegrationStopped()
}
