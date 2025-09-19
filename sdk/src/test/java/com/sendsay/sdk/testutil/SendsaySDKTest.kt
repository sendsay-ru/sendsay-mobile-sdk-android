package com.sendsay.sdk.testutil

import android.content.ComponentName
import android.content.Context
import android.content.pm.ProviderInfo
import android.os.Binder
import androidx.test.core.app.ApplicationProvider
import com.sendsay.sdk.Sendsay
import com.sendsay.sdk.manager.FetchManagerImpl
import com.sendsay.sdk.manager.FlushManagerImpl
import com.sendsay.sdk.manager.InAppContentBlockManagerImpl
import com.sendsay.sdk.manager.InAppMessageManagerImpl
import com.sendsay.sdk.manager.PushNotificationSelfCheckManagerImpl
import com.sendsay.sdk.manager.ReloadMode
import com.sendsay.sdk.mockkConstructorFix
import com.sendsay.sdk.network.SendsayServiceImpl
import com.sendsay.sdk.preferences.SendsayPreferencesImpl
import com.sendsay.sdk.repository.DeviceInitiatedRepositoryImpl
import com.sendsay.sdk.services.SendsayContextProvider
import com.sendsay.sdk.telemetry.upload.VSAppCenterTelemetryUpload
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Before
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.shadow.api.Shadow
import org.robolectric.shadows.ShadowApplication

internal open class SendsaySDKTest {
    companion object {
        fun skipInstallEvent() {
            DeviceInitiatedRepositoryImpl(SendsayPreferencesImpl(
                ApplicationProvider.getApplicationContext()
            )).set(true)
        }
    }

    @Before
    fun prepareServiceBinderForRoom() {
        Shadow.extract<ShadowApplication>(RuntimeEnvironment.getApplication()).let { appShadow ->
            appShadow.setComponentNameAndServiceForBindService(ComponentName("", ""), Binder())
            appShadow.setBindServiceCallsOnServiceConnectedDirectly(false)
        }
    }

    @Before
    fun disableTelemetry() {
        mockkConstructorFix(VSAppCenterTelemetryUpload::class) {
            every { anyConstructed<VSAppCenterTelemetryUpload>().upload(any(), any()) }
        }
        every { anyConstructed<VSAppCenterTelemetryUpload>().upload(any(), any()) } just Runs
    }

    @Before
    fun disableInAppMessagePrefetch() {
        mockkConstructorFix(InAppMessageManagerImpl::class)
        every {
            anyConstructed<InAppMessageManagerImpl>().detectReloadMode(any(), any(), any())
        } returns ReloadMode.STOP
        every { anyConstructed<InAppMessageManagerImpl>().pickAndShowMessage() } just Runs
    }

    @Before
    fun disableInAppContentBlocksPrefetch() {
        mockkConstructorFix(InAppContentBlockManagerImpl::class)
        every { anyConstructed<InAppContentBlockManagerImpl>().loadInAppContentBlockPlaceholders() } just Runs
    }

    @Before
    fun mockNetworkManagers() {
        mockkConstructorFix(FetchManagerImpl::class) {
            every { anyConstructed<FetchManagerImpl>().fetchSegments(any(), any(), any(), any()) }
        }
        mockkConstructorFix(SendsayServiceImpl::class) {
            every { anyConstructed<SendsayServiceImpl>().fetchSegments(any(), any()) }
        }
        mockkConstructorFix(FlushManagerImpl::class)
    }

    @Before
    fun disablePushNotificationSelfCheck() {
        mockkConstructorFix(PushNotificationSelfCheckManagerImpl::class)
        every {
            anyConstructed<PushNotificationSelfCheckManagerImpl>().start()
        } just Runs
    }

    @Before
    fun registersForegroundStateAppCheck() {
        SendsayContextProvider.applicationIsForeground = false
        Robolectric
            .buildContentProvider(SendsayContextProvider::class.java)
            .create(ProviderInfo().apply {
                authority = "${ApplicationProvider.getApplicationContext<Context>().packageName}.sdk.contextprovider"
                grantUriPermissions = true
            }).get()
    }

    @After
    fun afterSendsayTest() {
        // we need to enforce the order here, first unmock, then resetSendsay
        unmockAllSafely()
        resetSendsay()
    }

    fun unmockAllSafely() {
        // mockk has a problem when it sometimes throws an exception, in that case just try again
        try { unmockkAll() } catch (error: ConcurrentModificationException) { unmockAllSafely() }
    }

    fun resetSendsay() {
        if (Sendsay.isInitialized && Sendsay.componentForTesting.flushManager.isRunning) {
            // Database is shared between Sendsay instances, FlushingManager is not
            // If flushing is ongoing after test is done, it can flush events from another test
            // You can use waitUntilFlushed() to fix this
            throw RuntimeException("Flushing still in progress after test is done!")
        }
        Sendsay.reset()
    }
}
