package com.sendsay.sdk.manager

import android.content.Context
import com.sendsay.sdk.models.Event
import com.sendsay.sdk.models.EventType
import com.sendsay.sdk.models.InAppContentBlock
import com.sendsay.sdk.models.InAppContentBlockPlaceholderConfiguration
import com.sendsay.sdk.services.inappcontentblock.InAppContentBlockDataLoader
import com.sendsay.sdk.view.InAppContentBlockPlaceholderView
import com.sendsay.sdk.services.OnIntegrationStoppedCallback

interface InAppContentBlockManager : OnIntegrationStoppedCallback {
    fun getPlaceholderView(
        placeholderId: String,
        context: Context,
        config: InAppContentBlockPlaceholderConfiguration
    ): InAppContentBlockPlaceholderView
    fun getPlaceholderView(
        placeholderId: String,
        dataLoader: InAppContentBlockDataLoader,
        context: Context,
        config: InAppContentBlockPlaceholderConfiguration
    ): InAppContentBlockPlaceholderView
    fun loadInAppContentBlockPlaceholders()
    fun clearAll()
    fun onEventCreated(event: Event, type: EventType)
    fun getAllInAppContentBlocksForPlaceholder(placeholderId: String): List<InAppContentBlock>
    fun passesFilters(contentBlock: InAppContentBlock): Boolean
    fun loadContentIfNeededSync(contentBlocks: List<InAppContentBlock>)
    fun passesFrequencyFilter(contentBlock: InAppContentBlock): Boolean
    fun passesDateFilter(contentBlock: InAppContentBlock): Boolean
    override fun onIntegrationStopped()
}

internal interface InAppContentBlockTrackingDelegate {
    fun track(
        placeholderId: String,
        contentBlock: InAppContentBlock,
        action: String,
        interaction: Boolean,
        trackingAllowed: Boolean,
        text: String? = null,
        link: String? = null,
        error: String? = null
    )
}
