package ru.sendsay.sdk.manager

typealias FlushFinishedCallback = (Result<Unit>) -> Unit

internal interface FlushManager {

    val isRunning: Boolean
    /**
     * Starts flushing all events to Sendsay
     */
    fun flushData(onFlushFinished: FlushFinishedCallback? = null)
}
