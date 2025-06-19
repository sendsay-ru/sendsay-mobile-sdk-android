package com.sendsay.example.view.base

import androidx.fragment.app.Fragment
import com.sendsay.sdk.Sendsay
import com.sendsay.sdk.models.PropertiesList

open class BaseFragment : androidx.fragment.app.Fragment() {

    /**
     * Tracks certain screen that customer have visited
     * @param pageName - Name of the screen
     */
    fun trackPage(pageName: String) {
        val properties = PropertiesList(hashMapOf("name" to pageName))

        Sendsay.trackEvent(
            eventType = "page_view",
            properties = properties
        )
    }
}
