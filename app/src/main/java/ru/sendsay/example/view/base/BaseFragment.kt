package ru.sendsay.example.view.base

import ru.sendsay.sdk.Sendsay
import ru.sendsay.sdk.models.PropertiesList

open class BaseFragment : androidx.fragment.app.Fragment() {

    /**
     * Tracks certain screen that customer have visited
     * @param pageName - Name of the screen
     */
    fun trackPage(pageName: String) {
        val properties = PropertiesList(hashMapOf("name" to pageName))

        Sendsay.trackEvent(
            eventType = "page_view",
            properties = properties.toHashMap()
        )
    }
}
