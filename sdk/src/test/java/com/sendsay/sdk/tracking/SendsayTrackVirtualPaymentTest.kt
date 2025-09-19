package com.sendsay.sdk.tracking

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.sendsay.sdk.Sendsay
import com.sendsay.sdk.manager.EventManagerImpl
import com.sendsay.sdk.mockkConstructorFix
import com.sendsay.sdk.models.Event
import com.sendsay.sdk.models.EventType
import com.sendsay.sdk.models.SendsayConfiguration
import com.sendsay.sdk.models.FlushMode
import com.sendsay.sdk.models.PurchasedItem
import com.sendsay.sdk.testutil.SendsaySDKTest
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.slot
import io.mockk.verify
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class SendsayTrackVirtualPaymentTest : SendsaySDKTest() {
    val purchase = PurchasedItem(
        currency = "USD",
        value = 200.3,
        productId = "Item",
        productTitle = "Speed Boost",
        paymentSystem = "payment-system"
    )

    @Before
    fun before() {
        mockkConstructorFix(EventManagerImpl::class) {
            every { anyConstructed<EventManagerImpl>().addEventToQueue(any(), any(), any()) }
        }
        skipInstallEvent()
        val context = ApplicationProvider.getApplicationContext<Context>()
        val configuration = SendsayConfiguration(projectToken = "mock-token", automaticSessionTracking = false)
        Sendsay.flushMode = FlushMode.MANUAL
        Sendsay.init(context, configuration)
    }

    @Test
    fun `should track virtual payment`() {
        val eventSlot = slot<Event>()
        val eventTypeSlot = slot<EventType>()
        every {
            anyConstructed<EventManagerImpl>().addEventToQueue(capture(eventSlot), capture(eventTypeSlot), any())
        } just Runs
        Sendsay.trackPaymentEvent(purchasedItem = purchase)
        verify(exactly = 1) {
            anyConstructed<EventManagerImpl>().addEventToQueue(any(), any(), any())
        }

        assertEquals("payment", eventSlot.captured.type)
        assertTrue(
            eventSlot.captured.properties?.entries?.containsAll<Map.Entry<String, Any>>(
                hashMapOf(
                    "currency" to "USD",
                    "brutto" to 200.3,
                    "item_id" to "Item",
                    "product_title" to "Speed Boost",
                    "payment_system" to "payment-system"
                ).entries
            ) ?: false
        )
        assertEquals(EventType.PAYMENT, eventTypeSlot.captured)
    }
}
