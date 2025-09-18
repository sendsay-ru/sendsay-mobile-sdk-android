package ru.sendsay.sdk.testutil

import android.preference.PreferenceManager
import android.webkit.CookieManager
import androidx.test.core.app.ApplicationProvider
import ru.sendsay.sdk.Sendsay
import ru.sendsay.sdk.SendsayComponent
import ru.sendsay.sdk.manager.FlushManagerImpl
import ru.sendsay.sdk.models.Constants
import ru.sendsay.sdk.repository.EventRepositoryImpl
import ru.sendsay.sdk.services.SendsayContextProvider
import io.mockk.clearMocks
import kotlin.test.fail

internal fun Sendsay.shutdown() {
    val wasInitialized = isInitialized
    isInitialized = false
    flushMode = Constants.Flush.defaultFlushMode
    flushPeriod = Constants.Flush.defaultFlushPeriod
    initGate.clear()
    SendsayContextProvider.reset()
    if (!wasInitialized) return
    (componentForTesting.eventRepository as EventRepositoryImpl).close()
    componentForTesting.sessionManager.stopSessionListener()
    componentForTesting.serviceManager.stopPeriodicFlush(ApplicationProvider.getApplicationContext())
    componentForTesting.backgroundTimerManager.stopTimer()
    loggerLevel = Constants.Logger.defaultLoggerLevel
    (componentForTesting.flushManager as FlushManagerImpl).endsFlushProcess()
    telemetry = null
}

internal fun Sendsay.reset() {
    val wasInitialized = isInitialized
    isInitialized = false
    resetField(Sendsay, "application")
    resetField(Sendsay, "configuration")
    safeModeOverride = null
    runDebugModeOverride = null
    segmentationDataCallbacks.clear()
    flushMode = Constants.Flush.defaultFlushMode
    flushPeriod = Constants.Flush.defaultFlushPeriod
    initGate.clear()
    SendsayContextProvider.reset()
    if (!wasInitialized) return
    componentForTesting.campaignRepository.clear()
    componentForTesting.customerIdsRepository.clear()
    componentForTesting.deviceInitiatedRepository.set(false)
    componentForTesting.eventRepository.clear()
    (componentForTesting.eventRepository as EventRepositoryImpl).close()
    componentForTesting.pushTokenRepository.clear()
    componentForTesting.inAppContentBlockManager.clearAll()
    componentForTesting.segmentsManager.clearAll()

    componentForTesting.sessionManager.stopSessionListener()
    componentForTesting.serviceManager.stopPeriodicFlush(ApplicationProvider.getApplicationContext())
    componentForTesting.backgroundTimerManager.stopTimer()
    PreferenceManager.getDefaultSharedPreferences(ApplicationProvider.getApplicationContext())
        .edit().clear().commit()
    loggerLevel = Constants.Logger.defaultLoggerLevel
    telemetry = null
    CookieManager.getInstance().setAcceptCookie(true)
}

fun resetField(target: Any, fieldName: String) {
    val field = target.javaClass.getDeclaredField(fieldName)
    with(field) {
        isAccessible = true
        set(target, null)
    }
}

internal fun EventRepositoryImpl.close() {
    database.openHelper.close()
}

internal val Sendsay.componentForTesting: SendsayComponent
    get() {
        val componentField = Sendsay::class.java.getDeclaredField("component")
        componentField.isAccessible = true
        return componentField.get(this) as SendsayComponent
    }

/**
 * Asserts if iterables are equals ignoring of order of items.
 */
internal fun assertEqualsIgnoreOrder(
    expected: Collection<Any>?,
    actual: Collection<Any>?,
    message: String? = null
) {
    if (expected == null && actual == null) {
        return
    }
    if (expected == null || actual == null) {
        fail(messagePrefix(message) + "Expected <$expected>, actual <$actual>.")
    }
    if (expected.count() != actual.count()) {
        fail(messagePrefix(message) + "Expected <$expected>, actual <$actual>.")
    }
    val expectedCopy = expected.toTypedArray().sortBy { it.hashCode() }
    val actualCopy = actual.toTypedArray().sortBy { it.hashCode() }
    if (expectedCopy != actualCopy) {
        fail(messagePrefix(message) + "Expected <$expected>, actual <$actual>.")
    }
}

internal fun messagePrefix(message: String?) = if (message == null) "" else "$message. "

internal fun Any.resetVerifyMockkCount() {
    clearMocks(
        this,
        answers = false,
        recordedCalls = true,
        childMocks = false,
        verificationMarks = true,
        exclusionRules = false
    )
}
