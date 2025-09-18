package ru.sendsay.example.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import ru.sendsay.example.databinding.FragmentCarouselsBinding
import ru.sendsay.example.models.Constants
import ru.sendsay.example.view.base.BaseFragment
import ru.sendsay.sdk.Sendsay
import ru.sendsay.sdk.models.ContentBlockCarouselCallback
import ru.sendsay.sdk.models.ContentBlockSelector
import ru.sendsay.sdk.models.InAppContentBlock
import ru.sendsay.sdk.models.InAppContentBlockAction
import ru.sendsay.sdk.util.Logger

class CarouselsFragment : BaseFragment() {

    private lateinit var viewBinding: FragmentCarouselsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = FragmentCarouselsBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.let {
            it.subtitle = "InApp Carousels"
        }

        // Track visited screen
        trackPage(Constants.ScreenNames.inAppContentBlocksScreen)

        prepareExampleDefaultCarouselCbPlaceholder()
        prepareExampleCustomCarouselCbPlaceholder()
        prepareExampleAndroidCarouselCbPlaceholder()
    }

    private fun prepareExampleAndroidCarouselCbPlaceholder() {
        val androidCarousel = Sendsay.getInAppContentBlocksCarousel(requireContext(), "example_carousel_and")
        viewBinding.contentBlocksLayout.addView(androidCarousel)
    }

    private fun prepareExampleDefaultCarouselCbPlaceholder() {
        viewBinding.contentBlocksCarouselDefault.let {
            val initName = it.getShownContentBlock()?.name
            val initIndex = it.getShownIndex()
            val initCount = it.getShownCount()
            viewBinding.contentBlocksCarouselStatus.text = """
                Carousel is emptyShowing ${initName ?: ""} as ${initIndex + 1} of $initCount
                """.trimIndent()
        }
        viewBinding.contentBlocksCarouselDefault.behaviourCallback = object : ContentBlockCarouselCallback {

            override val overrideDefaultBehavior = false
            override val trackActions = true

            private var count: Int = 0
            private var index: Int = -1
            private var blockName: String? = null

            override fun onMessageShown(
                placeholderId: String,
                contentBlock: InAppContentBlock,
                index: Int,
                count: Int
            ) {
                this.blockName = contentBlock.name
                this.index = index
                this.count = count
                updateCarouselStatus()
            }

            private fun updateCarouselStatus() {
                viewBinding.contentBlocksCarouselStatus.text = "Showing ${blockName ?: ""} as ${index + 1} of $count"
            }

            override fun onMessagesChanged(count: Int, messages: List<InAppContentBlock>) {
                if (count == 0) {
                    this.blockName = null
                    this.index = -1
                }
                this.count = count
                updateCarouselStatus()
            }

            override fun onNoMessageFound(placeholderId: String) {
                Logger.i(this, "Carousel $placeholderId is empty")
            }

            override fun onError(placeholderId: String, contentBlock: InAppContentBlock?, errorMessage: String) {
                Logger.e(this, "Carousel $placeholderId error: $errorMessage")
            }

            override fun onCloseClicked(placeholderId: String, contentBlock: InAppContentBlock) {
                Logger.i(this, "Message ${contentBlock.name} has been closed in carousel $placeholderId")
            }

            override fun onActionClicked(
                placeholderId: String,
                contentBlock: InAppContentBlock,
                action: InAppContentBlockAction
            ) {
                Logger.i(this, "Action ${action.name} has been clicked in carousel $placeholderId")
            }

            override fun onHeightUpdate(placeholderId: String, height: Int) {
                Logger.i(this, "Carousel $placeholderId has new height $height")
            }
        }
    }

    private fun prepareExampleCustomCarouselCbPlaceholder() {
        viewBinding.contentBlocksCarouselCustom.contentBlockSelector = object : ContentBlockSelector() {
            override fun filterContentBlocks(source: List<InAppContentBlock>): List<InAppContentBlock> {
                return source.filter { !it.name.lowercase().contains("discarded") }
            }

            override fun sortContentBlocks(source: List<InAppContentBlock>): List<InAppContentBlock> {
                return super.sortContentBlocks(source).asReversed()
            }
        }
    }
}
