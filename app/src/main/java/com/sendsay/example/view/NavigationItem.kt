package com.sendsay.example.view

import com.sendsay.example.R

enum class NavigationItem(
    val navigationId: Int
) {
//    Fetch(R.id.fetchFragment),
    Track(R.id.trackFragment),
    Manual(R.id.flushFragment),
    Anonymize(R.id.anonymizeFragment),
//    InAppContentBlock(R.id.inAppContentBlocksFragment),
//    InAppCarousel(R.id.action_cb_to_carousel)
}
