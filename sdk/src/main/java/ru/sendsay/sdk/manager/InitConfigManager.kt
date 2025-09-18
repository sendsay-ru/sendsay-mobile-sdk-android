package ru.sendsay.sdk.manager

import ru.sendsay.sdk.models.ConfigItem
import ru.sendsay.sdk.models.Event
import ru.sendsay.sdk.models.EventType
import ru.sendsay.sdk.models.FetchError
import ru.sendsay.sdk.services.OnIntegrationStoppedCallback

interface InitConfigManager : OnIntegrationStoppedCallback {
    fun fetchInitConfig(
        onSuccess: (ArrayList<ConfigItem>?) -> Unit,
        onFailure: (FetchError) -> Unit
    )

    fun reload()
    fun onEventCreated(event: Event, type: EventType)
    override fun onIntegrationStopped()
}
