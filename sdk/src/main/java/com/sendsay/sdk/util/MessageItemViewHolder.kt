package com.sendsay.sdk.util

import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.sendsay.sdk.databinding.MessageInboxListItemBinding

/**
 * AppInbox message viewholder
 */
class MessageItemViewHolder(target: MessageInboxListItemBinding) : ViewHolder(target.root) {
    val itemContainer = target.messageItemContainer
    val readFlag = target.messageItemReadFlag
    val receivedTime = target.messageItemReceivedTime
    val title = target.messageItemTitle
    val content = target.messageItemContent
    val image = target.messageItemImage
}
