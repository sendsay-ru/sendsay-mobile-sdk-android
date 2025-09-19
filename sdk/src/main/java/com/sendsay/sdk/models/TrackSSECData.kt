package com.sendsay.sdk.models

import com.google.gson.Gson
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type
import com.google.gson.reflect.TypeToken

data class TrackSSECData(
    val productId: String? = null,
    val productName: String? = null,
    val dateTime: String? = null,
    val picture: List<String>? = null,
    val url: String? = null,
    val available: Long? = null,
    val categoryPaths: List<String>? = null,
    val categoryId: Long? = null,
    val category: String? = null,
    val description: String? = null,
    val vendor: String? = null,
    val model: String? = null,
    val type: String? = null,
    val price: Double? = null,
    val oldPrice: Double? = null,

    // Остальные необязательные поля
    val email: String? = null,
    val updatePerItem: Int? = null,
    val update: Int? = null,

    // Платёжные и транзакционные данные
    val transactionId: String? = null,
    val transactionDt: String? = null,
    val transactionStatus: Long? = null,
    val transactionDiscount: Double? = null,
    val transactionSum: Double? = null,

    // Доставка и оплата
    val deliveryDt: String? = null,
    val deliveryPrice: Double? = null,
    val paymentDt: String? = null,

    // items (для заказов/корзины)
    val items: List<OrderItem>? = null,
) {
    /**
     * Плоская карта без префиксов ключей для отправки в properties["ssec"].
     * Набор полей зависит от типа события, чтобы исключить конфликт имён.
     */
    fun toSsecMap(event: TrackingSSECType): Map<String, Any> {
        val out = linkedMapOf<String, Any>()
        fun put(k: String, v: Any?) { if (v != null) out[k] = v }

        when (event) {
            TrackingSSECType.ORDER -> {
                // транзакционные поля
                put("transaction_id", transactionId)
                put("transaction_dt", transactionDt)
                put("transaction_status", transactionStatus)
                put("transaction_discount", transactionDiscount)
                put("transaction_sum", transactionSum)

                // уникализируем потенциально конфликтные поля
                put("delivery_dt", deliveryDt)
                put("delivery_price", deliveryPrice)
                put("payment_dt", paymentDt)

                if (!items.isNullOrEmpty()) out["items"] = items
            }

            TrackingSSECType.BASKET_ADD -> {
                // событие корзины: дата, сумма, позиции, флаг update_per_item
                put("dt", dateTime ?: transactionDt)
                put("sum", transactionSum)
                if (!items.isNullOrEmpty()) out["items"] = items
                put("update_per_item", updatePerItem)
            }

            TrackingSSECType.BASKET_CLEAR -> {
                // очистка корзины: дата + позиции
                put("dt", dateTime ?: transactionDt)
                if (!items.isNullOrEmpty()) out["items"] = items
            }

            else -> {
                // продуктовые поля для прочих событий
                put("id", productId)
                put("name", productName)
                put("dt", dateTime)
                put("picture", picture)
                put("url", url)
                put("available", available)
                put("category_paths", categoryPaths)
                put("category_id", categoryId)
                put("category", category)
                put("description", description)
                put("vendor", vendor)
                put("model", model)
                put("type", this.type)
                put("price", price)
                put("old_price", oldPrice)

                put("email", email)
                put("update_per_item", updatePerItem)
                put("update", update)
            }
        }

        return out
    }

    /** хелпер: { "ssec": <map> } */
    fun toProperties(event: TrackingSSECType): HashMap<String, Any> =
        hashMapOf("ssec" to toSsecMap(event))
}

// Кастомный сериализатор для чисел (чтобы JSON не менял все Num примитивы в Double)
class StrictNumberDeserializer : JsonDeserializer<Any> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): Any {
        return when {
            json.isJsonNull -> null as Any
            json.isJsonPrimitive -> {
                val prim = json.asJsonPrimitive
                when {
                    prim.isBoolean -> prim.asBoolean
                    prim.isString  -> prim.asString
                    prim.isNumber  -> {
                        val str = prim.asString
                        if (str.contains('.')) prim.asDouble
                        else {
                            try {
                                val long = str.toLong()
                                if (long in Int.MIN_VALUE..Int.MAX_VALUE) long.toInt() else long
                            } catch (_: NumberFormatException) {
                                prim.asDouble
                            }
                        }
                    }
                    else -> prim.asString
                }
            }

            json.isJsonObject -> {
                val type = object : TypeToken<Map<String, Any?>>() {}.type
                context.deserialize<Map<String, Any?>>(json, type)
            }

            json.isJsonArray -> {
                val type = object : TypeToken<List<Any?>>() {}.type
                context.deserialize<List<Any?>>(json, type)
            }

            else -> json.toString()
        }
    }
}

class NumberPreserveAdapter : JsonDeserializer<Any> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Any {
        return when {
            json.isJsonPrimitive && json.asJsonPrimitive.isNumber -> {
                val num = json.asJsonPrimitive.asNumber
                if (num.toString().contains(".")) num.toDouble()
                else {
                    try { num.toInt() } catch (e: Exception) { num.toLong() }
                }
            }
            json.isJsonObject -> context.deserialize<Map<String, Any>>(json, object : TypeToken<Map<String, Any>>(){}.type)
            json.isJsonArray -> context.deserialize<List<Any>>(json, object : TypeToken<List<Any>>(){}.type)
            else -> json.toString()
        }
    }
}
