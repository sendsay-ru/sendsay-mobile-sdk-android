package com.sendsay.sdk.services.inappcontentblock

import com.sendsay.sdk.models.InAppContentBlock
import com.sendsay.sdk.models.InAppContentBlockAction

interface InAppContentBlockActionDispatcher {
    fun onError(placeholderId: String, contentBlock: InAppContentBlock?, errorMessage: String)
    fun onClose(placeholderId: String, contentBlock: InAppContentBlock)
    fun onAction(
        placeholderId: String,
        contentBlock: InAppContentBlock,
        action: InAppContentBlockAction
    )
    fun onNoContent(placeholderId: String, contentBlock: InAppContentBlock?)
    fun onShown(placeholderId: String, contentBlock: InAppContentBlock)
}
