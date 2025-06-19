package com.sendsay.sdk.view

import android.app.Activity
import android.app.AlertDialog
import com.sendsay.sdk.Sendsay
import com.sendsay.sdk.models.InAppMessageButtonType
import com.sendsay.sdk.models.InAppMessagePayload
import com.sendsay.sdk.models.InAppMessagePayloadButton
import com.sendsay.sdk.util.Logger
import com.sendsay.sdk.util.ensureOnMainThread

internal class InAppMessageAlert(
    private val activity: Activity,
    payload: InAppMessagePayload,
    onButtonClick: (InAppMessagePayloadButton) -> Unit,
    onDismiss: (Boolean, InAppMessagePayloadButton?) -> Unit,
    private val onError: (String) -> Unit
) : InAppMessageView {
    val dialog: AlertDialog
    var userInteraction: Boolean = false
    override val isPresented: Boolean
        get() = dialog.isShowing

    init {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(payload.title)
        builder.setMessage(payload.bodyText)
        if (payload.buttons != null) {
            for (button in payload.buttons) {
                if (button.buttonType == InAppMessageButtonType.CANCEL) {
                    builder.setNegativeButton(button.text) { _, _ ->
                        userInteraction = true
                        onDismiss(true, button)
                        dismiss()
                    }
                } else {
                    builder.setPositiveButton(button.text) { _, _ ->
                        userInteraction = true
                        onButtonClick(button)
                    }
                }
            }
        }
        builder.setOnDismissListener {
            Sendsay.deintegration.unregisterForIntegrationStopped(this)
            if (!Sendsay.isStopped && !userInteraction) {
                onDismiss(false, null)
            }
            activity.finish()
        }
        Sendsay.deintegration.registerForIntegrationStopped(this)
        dialog = builder.create()
    }

    override fun show() {
        try {
            dialog.show()
        } catch (e: Exception) {
            Logger.e(this, "[InApp] Unable to show Alert in-app message", e)
            onError.invoke("Invalid app foreground state")
        }
    }

    override fun dismiss() {
        try {
            dialog.dismiss()
        } catch (e: Exception) {
            Logger.e(this, "[InApp] Dismissing Alert in-app message failed", e)
        }
    }

    override fun onIntegrationStopped() {
        ensureOnMainThread {
            dismiss()
        }
    }
}
