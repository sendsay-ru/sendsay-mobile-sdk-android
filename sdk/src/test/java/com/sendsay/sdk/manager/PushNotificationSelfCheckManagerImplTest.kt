package com.sendsay.sdk.manager

import android.app.Activity
import android.app.AlertDialog
import android.app.Application
import android.content.ContextWrapper
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowAlertDialog

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
internal class PushNotificationSelfCheckManagerImplTest {
    private lateinit var presenter: SelfCheckDialogPresenter

    @Before
    fun setUp() {
        presenter = SelfCheckDialogPresenter(
            ApplicationProvider.getApplicationContext<Application>(),
        )
    }

    @Test
    fun `result waits for resumed Activity and is shown with its context`() {
        presenter.show(SelfCheckResult.Success)
        assertNull(ShadowAlertDialog.getLatestAlertDialog())

        val controller = Robolectric.buildActivity(Activity::class.java)
            .create().start()
        assertNull(ShadowAlertDialog.getLatestAlertDialog())

        controller.resume().visible()
        val dialog = ShadowAlertDialog.getLatestAlertDialog()
        assertTrue(dialog.isShowing)
        assertEquals(
            controller.get(),
            (dialog.context as ContextWrapper).baseContext,
        )
        assertTrue(shadowOf(dialog).message.toString().contains(
            "You are now ready to receive push notifications",
        ))
        dialog.dismiss()
        controller.pause().stop().destroy()
    }

    @Test
    fun `result is shown again after Activity is recreated`() {
        presenter.show(SelfCheckResult.TokenMissing)
        val first = Robolectric.buildActivity(Activity::class.java).setup()
        val firstDialog = ShadowAlertDialog.getLatestAlertDialog()
        assertTrue(firstDialog.isShowing)

        first.pause().stop().destroy()
        assertFalse(firstDialog.isShowing)

        val second = Robolectric.buildActivity(Activity::class.java).setup()
        val secondDialog = ShadowAlertDialog.getLatestAlertDialog()
        assertTrue(secondDialog.isShowing)
        assertEquals(
            second.get(),
            (secondDialog.context as ContextWrapper).baseContext,
        )
        secondDialog.dismiss()
        second.pause().stop().destroy()
    }

    @Test
    fun `acknowledged result is not shown on next resume`() {
        presenter.show(SelfCheckResult.Success)
        val controller = Robolectric.buildActivity(Activity::class.java).setup()
        val dialog = ShadowAlertDialog.getLatestAlertDialog()
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).performClick()
        shadowOf(android.os.Looper.getMainLooper()).idle()
        assertFalse(dialog.isShowing)

        controller.pause().stop().start().resume().visible()
        assertEquals(dialog, ShadowAlertDialog.getLatestAlertDialog())
        assertFalse(dialog.isShowing)
        controller.pause().stop().destroy()
    }

    @Test
    fun `result received in background waits for Activity to return`() {
        presenter.show(SelfCheckResult.Success)
        val controller = Robolectric.buildActivity(Activity::class.java).setup()
        ShadowAlertDialog.getLatestAlertDialog().dismiss()
        controller.pause().stop()

        presenter.show(SelfCheckResult.TokenMissing)
        assertFalse(ShadowAlertDialog.getLatestAlertDialog().isShowing)

        controller.start().resume().visible()
        val dialog = ShadowAlertDialog.getLatestAlertDialog()
        assertTrue(dialog.isShowing)
        assertTrue(shadowOf(dialog).message.toString().contains(
            "Unable to get push token",
        ))
        dialog.dismiss()
        controller.pause().stop().destroy()
    }
}
