package com.sendsay.sdk.manager

import com.sendsay.sdk.Sendsay
import com.sendsay.sdk.models.ConfigItem
import com.sendsay.sdk.models.Event
import com.sendsay.sdk.models.EventType
import com.sendsay.sdk.models.FetchError
import com.sendsay.sdk.services.SendsayProjectFactory
import com.sendsay.sdk.util.Logger
import com.sendsay.sdk.util.currentTimeSeconds
import java.util.ArrayList
import java.util.Date

internal class InitConfigManagerImpl(
    private val fetchManager: FetchManager,
    private val projectFactory: SendsayProjectFactory
) : InitConfigManager {

    private var sessionStartDate = Date()

    override fun fetchInitConfig(
        onSuccess: (ArrayList<ConfigItem>?) -> Unit,
        onFailure: (FetchError) -> Unit
    ) {
        if (Sendsay.isStopped) {
            Logger.e(this, "InitConfig: InitConfig fetch failed, SDK is stopping")
            onFailure(FetchError(null, "SDK is stopping"))
            return
        }
        fetchManager.fetchInitConfig(
            sendsayProject = projectFactory.mutualSendsayProject,
            onSuccess = { result ->
                onSuccess(result.results)
            },
            onFailure = {
                onFailure(it.results)
            }
        )
    }

    override fun reload() {
        val oneHourInMillis = 60 * 60 * 1000
        val now = Date()
        if (now.time > sessionStartDate.time + oneHourInMillis) {
            TODO("implement calling fetchInitConfig() EVERY HOUR with $sessionStartDate")
        }
    }

    override fun onEventCreated(event: Event, type: EventType) {
        when (type) {
            EventType.SESSION_START -> {
                Logger.d(this, "InAppCB: Event session_start occurs, storing time value")
                val eventTimestampInMillis = (event.timestamp ?: currentTimeSeconds()) * 1000
                sessionStartDate = Date(eventTimestampInMillis.toLong())
            }

            else -> {
                // nothing to trigger
            }
        }
    }

    override fun onIntegrationStopped() {}
}