package com.sendsay.sdk.manager

import com.sendsay.sdk.models.ConfigItem
import com.sendsay.sdk.models.Event
import com.sendsay.sdk.models.EventType
import com.sendsay.sdk.models.FetchError
import com.sendsay.sdk.services.OnIntegrationStoppedCallback

interface InitConfigManager : OnIntegrationStoppedCallback {
    fun fetchInitConfig(
        onSuccess: (ConfigItem?) -> Unit,
        onFailure: (FetchError) -> Unit
    )

    fun reload()
    fun onEventCreated(event: Event, type: EventType)
    override fun onIntegrationStopped()
}
