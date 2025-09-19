package com.sendsay.sdk.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout.LayoutParams
import androidx.fragment.app.Fragment
import com.sendsay.sdk.Sendsay
import com.sendsay.sdk.databinding.MessageInboxListFragmentBinding
import com.sendsay.sdk.models.MessageItem
import com.sendsay.sdk.util.Logger

class AppInboxListFragment : Fragment() {

    private var onItemClickListener: (MessageItem, Int) -> Unit = { item: MessageItem, position: Int ->
        Logger.i(this, "AppInbox message ${item.id} is opening")
        onMessageItemClicked(item, position)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val layout = MessageInboxListFragmentBinding.inflate(inflater, container, false)
        layout.container.addView(
            Sendsay.getAppInboxListView(requireContext(), onItemClickListener),
            LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        )
        return layout.root
    }

    private fun onMessageItemClicked(item: MessageItem, index: Int) {
        requireContext().startActivity(AppInboxDetailActivity.buildIntent(requireContext(), item))
    }
}
