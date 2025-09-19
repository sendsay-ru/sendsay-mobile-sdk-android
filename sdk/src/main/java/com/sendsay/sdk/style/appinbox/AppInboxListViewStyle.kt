package com.sendsay.sdk.style.appinbox

import androidx.recyclerview.widget.RecyclerView
import com.sendsay.sdk.util.ConversionUtils

data class AppInboxListViewStyle(
    var backgroundColor: String? = null,
    var item: AppInboxListItemStyle? = null
) {
    fun applyTo(target: RecyclerView) {
        ConversionUtils.parseColor(backgroundColor)?.let {
            target.setBackgroundColor(it)
        }
        // note: 'item' style is used elsewhere due to performance reasons
    }
}
