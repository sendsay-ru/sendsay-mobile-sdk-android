package com.sendsay.sdk.models

enum class TrackingSSECType(val value: String, val id: Int) {
    VIEW_PRODUCT("ssec_product_view", 0),
    ORDER("ssec_order", 1),
    VIEW_CATEGORY("ssec_category_view", 2),
    BASKET_ADD("ssec_basket", 3),
    BASKET_CLEAR("ssec_basket_clear", 4),
    SEARCH_PRODUCT("ssec_product_search", 5),
    SUBSCRIBE_PRODUCT_PRICE("ssec_product_price", 6),
    SUBSCRIBE_PRODUCT_ISA("ssec_subscribe_product_isa", 7),
    FAVORITE("ssec_product_favorite", 8),
    PREORDER("ssec_product_preorder", 12),
    PRODUCT_ISA("ssec_product_isa", 13),
    PRODUCT_PRICE_CHANGED("ssec_product_price_changed", 15),
    REGISTRATION("ssec_registration", 28),
    AUTHORIZATION("ssec_authorization", 29);

    companion object {
        fun find(value: String?, id: Int?) = TrackingSSECType.entries.find {
            it.value.equals(value, ignoreCase = true) || it.id == id
        }
    }
}
