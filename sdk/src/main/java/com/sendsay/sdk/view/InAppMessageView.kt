package com.sendsay.sdk.view

import com.sendsay.sdk.services.OnIntegrationStoppedCallback

internal interface InAppMessageView : OnIntegrationStoppedCallback {
    val isPresented: Boolean
    fun show()
    fun dismiss()
    override fun onIntegrationStopped()
}
