package ru.sendsay.sdk.runcatching

import ru.sendsay.sdk.Sendsay
import ru.sendsay.sdk.SendsayComponent
import ru.sendsay.sdk.manager.BackgroundTimerManagerImpl
import ru.sendsay.sdk.mockkConstructorFix
import ru.sendsay.sdk.receiver.NotificationsPermissionReceiver
import ru.sendsay.sdk.services.DefaultAppInboxProvider
import ru.sendsay.sdk.services.inappcontentblock.ContentBlockCarouselViewController
import io.mockk.every
import io.mockk.mockkObject

object SendsayExceptionThrowing {
    class TestPurposeException : Exception("Exception for test purposes")

    private var throwException = false

    fun prepareSendsayToThrow() {
        throwException = false
        mockkConstructorFix(SendsayComponent::class)
        mockkConstructorFix(DefaultAppInboxProvider::class) {
            every { anyConstructed<DefaultAppInboxProvider>().getAppInboxButton(any()) }
        }
        mockkObject(NotificationsPermissionReceiver)

        every { anyConstructed<SendsayComponent>().anonymize(any(), any()) } answers {
            if (throwException) throw TestPurposeException()
            callOriginal()
        }
        every { anyConstructed<SendsayComponent>().eventManager } answers {
            if (throwException) throw TestPurposeException()
            callOriginal()
        }
        every { anyConstructed<SendsayComponent>().sessionManager } answers {
            if (throwException) throw TestPurposeException()
            callOriginal()
        }
        every { anyConstructed<SendsayComponent>().connectionManager } answers {
            if (throwException) throw TestPurposeException()
            callOriginal()
        }
        every { anyConstructed<SendsayComponent>().flushManager } answers {
            if (throwException) throw TestPurposeException()
            callOriginal()
        }
        every { anyConstructed<SendsayComponent>().backgroundTimerManager } answers {
            if (throwException) throw TestPurposeException()
            callOriginal()
        }
        mockkConstructorFix(BackgroundTimerManagerImpl::class)
        every { anyConstructed<BackgroundTimerManagerImpl>().startTimer() } answers {
            if (throwException) throw TestPurposeException()
            callOriginal()
        }
        every { anyConstructed<BackgroundTimerManagerImpl>().stopTimer() } answers {
            if (throwException) throw TestPurposeException()
            callOriginal()
        }
        every { anyConstructed<SendsayComponent>().serviceManager } answers {
            if (throwException) throw TestPurposeException()
            callOriginal()
        }
        every { anyConstructed<SendsayComponent>().fcmManager } answers {
            if (throwException) throw TestPurposeException()
            callOriginal()
        }
        every { anyConstructed<SendsayComponent>().fetchManager } answers {
            if (throwException) throw TestPurposeException()
            callOriginal()
        }
        every { anyConstructed<SendsayComponent>().networkManager } answers {
            if (throwException) throw TestPurposeException()
            callOriginal()
        }
        every { anyConstructed<SendsayComponent>().trackingConsentManager } answers {
            if (throwException) throw TestPurposeException()
            callOriginal()
        }
        every { anyConstructed<SendsayComponent>().inAppMessageTrackingDelegate } answers {
            if (throwException) throw TestPurposeException()
            callOriginal()
        }
        every { anyConstructed<SendsayComponent>().appInboxManager } answers {
            if (throwException) throw TestPurposeException()
            callOriginal()
        }
        every { anyConstructed<SendsayComponent>().appInboxCache } answers {
            if (throwException) throw TestPurposeException()
            callOriginal()
        }
        every { anyConstructed<DefaultAppInboxProvider>().getAppInboxButton(any()) } answers {
            if (throwException) throw TestPurposeException()
            callOriginal()
        }
        every { anyConstructed<DefaultAppInboxProvider>().getAppInboxListFragment(any()) } answers {
            if (throwException) throw TestPurposeException()
            callOriginal()
        }
        every { anyConstructed<DefaultAppInboxProvider>().getAppInboxDetailFragment(any(), any()) } answers {
            if (throwException) throw TestPurposeException()
            callOriginal()
        }
        every { anyConstructed<SendsayComponent>().inAppContentBlockManager } answers {
            if (throwException) throw TestPurposeException()
            callOriginal()
        }
        every { NotificationsPermissionReceiver.requestPushAuthorization(any(), any()) } answers {
            if (throwException) throw TestPurposeException()
            callOriginal()
        }
        every { anyConstructed<SendsayComponent>().segmentsManager } answers {
            if (throwException) throw TestPurposeException()
            callOriginal()
        }
        Sendsay.appInboxProvider = DefaultAppInboxProvider()
        mockkConstructorFix(ContentBlockCarouselViewController::class)
        every { anyConstructed<ContentBlockCarouselViewController>().contentBlockCarouselAdapter } answers {
            if (throwException) throw TestPurposeException()
            callOriginal()
        }
    }

    fun makeSendsayThrow() {
        throwException = true
    }
}
