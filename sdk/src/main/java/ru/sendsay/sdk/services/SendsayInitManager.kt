package ru.sendsay.sdk.services

import ru.sendsay.sdk.Sendsay
import ru.sendsay.sdk.util.Logger
import ru.sendsay.sdk.util.logOnException
import java.util.concurrent.ConcurrentLinkedQueue

internal class SendsayInitManager {

    val afterInitCallbacks = ConcurrentLinkedQueue<() -> Unit>()

    /**
     * Runs all pending callbacks that are waiting for SendsaySDK init finish.
     */
    fun notifyInitializedState() {
        do {
            val pendingCallback = afterInitCallbacks.poll()
            pendingCallback?.let { executeCallbackSafely(it) }
        } while (pendingCallback != null)
        Logger.v(this, "All pending callbacks invoked")
    }

    /**
     * Drops all pending callbacks that are waiting for SendsaySDK init finish.
     * This method should be invoked to release resources but used only in reasonable cases (such as anonymize).
     */
    fun clear() {
        if (afterInitCallbacks.isNotEmpty()) {
            Logger.e(this, "Removing ${afterInitCallbacks.size} pending callbacks of afterInit")
        }
        afterInitCallbacks.clear()
    }

    /**
     * Runs `afterInitBlock` code after SendsaySDK is fully initialized.
     * If SDK is already initialized so is code invoked immediatelly.
     * !!! Pending callbacks are cleared on `anonymize` call.
     */
    fun waitForInitialize(
        afterInitBlock: () -> Unit
    ) {
        Sendsay.requireInitialized(notInitializedBlock = {
            afterInitCallbacks.add(afterInitBlock)
        }, initializedBlock = {
            executeCallbackSafely(afterInitBlock)
        })
    }

    private fun executeCallbackSafely(block: () -> Unit) {
        runCatching(block).logOnException()
    }
}
