package ru.sendsay.sdk.style.appinbox

import android.view.View
import android.widget.ImageView
import ru.sendsay.sdk.util.ConversionUtils

data class ImageViewStyle(
    var visible: Boolean? = null,
    var backgroundColor: String? = null
) {
    fun applyTo(target: ImageView) {
        visible?.let {
            target.visibility = if (it) View.VISIBLE else View.GONE
        }
        ConversionUtils.parseColor(backgroundColor)?.let {
            target.setBackgroundColor(it)
        }
    }
}
