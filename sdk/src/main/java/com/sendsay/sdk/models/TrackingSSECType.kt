package com.sendsay.sdk.models

enum class TrackingSSECType(val value: String) {
    VIEW_PRODUCT("ssec_product_view"),
    ORDER("ssec_order"),
    VIEW_CATEGORY("ssec_category_view"),
    BASKET_ADD("ssec_basket"),
    BASKET_CLEAR("ssec_basket_clear"),
    SEARCH_PRODUCT("ssec_product_search"),
    SUBSCRIBE_PRODUCT_PRICE("ssec_product_price"),
    SUBSCRIBE_PRODUCT_ISA("ssec_product_isa"),
    FAVORITE("ssec_product_favorite"),
    PREORDER("ssec_product_preorder"),
    PRODUCT_ISA("ssec_product_isa"),
    PRODUCT_PRICE_CHANGED("ssec_product_price_chang"),
    REGISTRATION("ssec_registration"),
    AUTHORIZATION("ssec_authorization");

    companion object {
        fun find(value: String?) = TrackingSSECType.entries.find { it.value.equals(value, ignoreCase = true) }
    }
}
