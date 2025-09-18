package ru.sendsay.sdk.view

import androidx.appcompat.app.AppCompatActivity
import ru.sendsay.sdk.Sendsay

internal class InAppMessageActivity : AppCompatActivity() {

    internal var presentedMessageView: InAppMessageView? = null

    override fun onResume() {
        super.onResume()

        presentedMessageView = Sendsay.getPresentedInAppMessageView(this)

        if (presentedMessageView == null) {
            finish()
        } else {
            presentedMessageView!!.show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        val messageViewToDestroy = presentedMessageView ?: return
        messageViewToDestroy.dismiss()
        presentedMessageView = null
    }
}
