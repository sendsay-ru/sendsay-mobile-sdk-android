package ru.sendsay.sdk.manager

import ru.sendsay.sdk.models.SendsayConfiguration
import ru.sendsay.sdk.models.ExportedEvent
import ru.sendsay.sdk.network.SendsayService
import ru.sendsay.sdk.repository.EventRepository
import ru.sendsay.sdk.util.Logger
import ru.sendsay.sdk.util.runWithTimeout

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
