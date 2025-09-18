package ru.sendsay.sdk.view

import ru.sendsay.sdk.services.OnIntegrationStoppedCallback

internal interface InAppMessageView : OnIntegrationStoppedCallback {
    val isPresented: Boolean
    fun show()
    fun dismiss()
    override fun onIntegrationStopped()
}
