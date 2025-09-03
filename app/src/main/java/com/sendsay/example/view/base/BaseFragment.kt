package com.sendsay.example.view.base

import com.sendsay.sdk.Sendsay
import com.sendsay.sdk.models.PropertiesAdapter

open class BaseFragment : androidx.fragment.app.Fragment() {

    /**
     * Tracks certain screen that customer have visited
     * @param pageName - Name of the screen
     */
    fun trackPage(pageName: String) {
        val properties = PropertiesAdapter(hashMapOf("name" to pageName))

        Sendsay.trackEvent(
            eventType = "page_view",
            properties = properties.toHashMap()
        )
    }
}
