package com.sendsay.sdk.manager

import com.sendsay.sdk.models.SendsayConfiguration
import com.sendsay.sdk.models.ExportedEvent
import com.sendsay.sdk.network.SendsayService
import com.sendsay.sdk.repository.EventRepository
import com.sendsay.sdk.util.Logger
import com.sendsay.sdk.util.runWithTimeout

internal class TimeLimitedFlushManagerImpl(
    configuration: SendsayConfiguration,
    eventRepository: EventRepository,
    sendsayService: SendsayService,
    connectionManager: ConnectionManager,
    onEventUploaded: (ExportedEvent) -> Unit,
    val flushTimeLimit: Long
) : FlushManagerImpl(
    configuration,
    eventRepository,
    sendsayService,
    connectionManager,
    onEventUploaded
) {
    override fun flushData(onFlushFinished: FlushFinishedCallback?) {
        runWithTimeout(flushTimeLimit, {
            super.flushData(onFlushFinished)
        }, {
            Logger.w(this, "Flushing timeouted")
        })
    }
}
