package com.sendsay.sdk.services.inappcontentblock

import com.sendsay.sdk.models.InAppContentBlock

interface InAppContentBlockDataLoader {
    fun loadContent(placeholderId: String): InAppContentBlock?
}
