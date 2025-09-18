package ru.sendsay.sdk.style.appinbox

import android.view.View
import android.widget.TextView
import ru.sendsay.sdk.style.PlatformSize
import ru.sendsay.sdk.util.ConversionUtils

data class TextViewStyle(
    var visible: Boolean? = null,
    var textColor: String? = null,
    var textSize: String? = null,
    var textWeight: String? = null,
    var textOverride: String? = null
) {
    fun applyTo(target: TextView) {
        visible?.let {
            target.visibility = if (it) View.VISIBLE else View.GONE
        }
        ConversionUtils.parseColor(textColor)?.let {
            target.setTextColor(it)
        }
        PlatformSize.parse(textSize)?.let {
            target.setTextSize(it.unit, it.size)
        }
        textWeight?.let {
            target.setTypeface(target.typeface, ConversionUtils.parseTypeface(it))
        }
        textOverride?.let { target.text = it }
    }
}
