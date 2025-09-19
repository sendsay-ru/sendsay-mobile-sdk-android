package com.sendsay.sdk.models

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonSerializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import kotlin.collections.component1
import kotlin.collections.component2

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
    val category: String? = null,

    // cp1..cp20
    val cp: Map<String, Any>? = null
)

// Кастомный сериализатор для cp1..cp20
class OrderItemAdapter : JsonSerializer<OrderItem>, JsonDeserializer<OrderItem> {
    override fun serialize(src: OrderItem, typeOfSrc: Type, ctx: JsonSerializationContext): JsonElement {
        val o = JsonObject()
        fun add(k: String, v: Any?) { if (v != null) o.add(k, ctx.serialize(v)) }

        // базовые поля
        add("id", src.id)
        add("qnt", src.qnt)
        add("price", src.price)
        add("name", src.name)
        add("description", src.description)
        add("uniq", src.uniq)
        add("available", src.available)
        add("old_price", src.oldPrice)
        add("picture", src.picture)
        add("url", src.url)
        add("model", src.model)
        add("vendor", src.vendor)
        add("category_id", src.categoryId)
        add("category", src.category)

        // cp1..cp20 раскладываем как отдельные поля
        src.cp?.forEach { (k, v) ->
            if (k.matches(Regex("cp\\d+"))) o.add(k, ctx.serialize(v))
        }
        return o
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, ctx: JsonDeserializationContext): OrderItem {
        val o = json.asJsonObject
        fun je(name: String) = o.get(name)?.takeIf { !it.isJsonNull }
        fun asString(name: String) = je(name)?.asString
        fun asLong(name: String) = je(name)?.asLong
        fun asDouble(name: String) = je(name)?.asDouble

        val cp = o.entrySet()
            .filter { it.key.matches(Regex("cp\\d+")) }
            .associate { (k, v) -> k to ctx.deserialize<Any>(v, Any::class.java) }

        val pictureType = object : TypeToken<List<String>>() {}.type

        return OrderItem(
            id = o["id"].asString,
            qnt = asLong("qnt"),
            price = asDouble("price"),
            name = asString("name"),
            description = asString("description"),
            uniq = asString("uniq"),
            available = asLong("available"),
            oldPrice = asDouble("old_price"),
            picture = ctx.deserialize(je("picture"), pictureType),
            url = asString("url"),
            model = asString("model"),
            vendor = asString("vendor"),
            categoryId = asLong("category_id"),
            category = asString("category"),
            cp = cp
        )
    }
}