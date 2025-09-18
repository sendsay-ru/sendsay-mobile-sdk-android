package ru.sendsay.sdk.services.inappcontentblock

import ru.sendsay.sdk.models.InAppContentBlock
import ru.sendsay.sdk.util.Logger

internal class SingleContentBlockLoader : InAppContentBlockDataLoader {
    internal var assignedContentBlock: InAppContentBlock? = null
    override fun loadContent(placeholderId: String): InAppContentBlock? {
        if (assignedContentBlock == null) {
            Logger.w(
                this,
                "InAppCb: Content block loader has been requested for non-assigned placeholder: $placeholderId"
            )
        }
        return assignedContentBlock
    }
}
