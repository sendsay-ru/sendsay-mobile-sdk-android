package ru.sendsay.sdk.models

import com.google.gson.annotations.SerializedName

data class OrderItem(
    @SerializedName("id")
    val id: String,
    @SerializedName("qnt")
    val qnt: Long? = null,
    @SerializedName("price")
    val price: Double? = null,

    @SerializedName("name")
    val name: String? = null,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("uniq")
    val uniq: String? = null,
    @SerializedName("available")
    val available: Long? = null,
    @SerializedName("old_price")
    val oldPrice: Double? = null,
    @SerializedName("picture")
    val picture: List<String>? = null,
    @SerializedName("url")
    val url: String? = null,
    @SerializedName("model")
    val model: String? = null,
    @SerializedName("vendor")
    val vendor: String? = null,
    @SerializedName("category_id")
    val categoryId: Long? = null,
    @SerializedName("category")
    val category: String? = null
)
