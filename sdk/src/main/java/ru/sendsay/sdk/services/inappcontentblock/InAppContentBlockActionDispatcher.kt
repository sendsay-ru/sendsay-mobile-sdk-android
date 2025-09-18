package ru.sendsay.sdk.services.inappcontentblock

import ru.sendsay.sdk.models.InAppContentBlock
import ru.sendsay.sdk.models.InAppContentBlockAction

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
