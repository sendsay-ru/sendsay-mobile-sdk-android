package ru.sendsay.sdk.services.inappcontentblock

import ru.sendsay.sdk.models.InAppContentBlock

interface InAppContentBlockDataLoader {
    fun loadContent(placeholderId: String): InAppContentBlock?
}
