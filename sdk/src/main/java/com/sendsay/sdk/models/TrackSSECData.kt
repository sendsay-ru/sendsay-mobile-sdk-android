package com.sendsay.sdk.models

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

data class TrackSSECData(
    val productId: String? = null,
    val productName: String? = null,
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

    // Данные о выпуске CDP Sendsay
    val issue: Int? = null,
    val letter: Int? = null,
    val issueDt: String? = null,

    // Продуктовое обновление
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

    // Подписка на изменения продуктов / работа с избранным
    val subscriptionAdd: List<OrderItem>? = null,
    val subscriptionDelete: List<Int>? = null,
    val subscriptionClear: Int? = null,

) {
    /**
     * Формирует плоскую карту ключей без префиксов для отправки в properties["ssec"].
     * Набор полей зависит от типа события, чтобы исключить конфликт имён (id/dt/price и т.п.).
     */
    fun toSsecMap(): Map<String, Any> {
        val out = linkedMapOf<String, Any>()
        fun put(k: String, v: Any?) {
            if (v != null) out[k] = v
        }

        // Все свойства добавляем по их 
        put("id", productId)
        put("name", productName)
        put("picture", picture)
        put("url", url)
        put("available", available)
        put("category_paths", categoryPaths)
        put("category_id", categoryId)
        put("category", category)
        put("description", description)
        put("vendor", vendor)
        put("model", model)
        put("type", type)
        put("price", price)
        put("oldPrice", oldPrice)

        // Данные о выпуске CDP Sendsay
        put("issue", issue)
        put("letter", letter)
        put("issue_dt", issueDt)

        // Обновления продукта
        put("update_per_item", updatePerItem)
        put("update", update)

        // Платёжные и транзакционные данные
        put("transaction_id", transactionId)
        put("transaction_dt", transactionDt)
        put("transaction_status", transactionStatus)
        put("transaction_discount", transactionDiscount)
        put("transaction_sum", transactionSum)

        // Доставка и оплата
        put("delivery_dt", deliveryDt)
        put("delivery_price", deliveryPrice)
        put("payment_dt", paymentDt)

        // Позиции/заказы (добавляем только если не пусто)
        if (!items.isNullOrEmpty()) out["items"] = items

        // Подписка на изменения
        put("add", subscriptionAdd)
        put("delete", subscriptionDelete)
        put("clear", subscriptionClear)

        return out
    }

    /**
     * Удобный хелпер: готовые properties для отправки/кеширования
     * { "ssec": <map> }
     */
    fun toProperties(event: TrackingSSECType): HashMap<String, Any> =
        hashMapOf("ssec" to toSsecMap())
}

enum class SSECTransactionStatus(val code: Long, val desc: String? = null) {
    REGISTRED(1, "Заказ Оформлен (создан,принят)"),
    PAID(2, "Заказ Оплачен"),
    ACCEPTED(3, "Заказ Принят в работу (сборка, комплектация)"),
    DELIVERY(4, "Доставка"),
    DELIVERY_TRACKING(5, "Доставка: присвоен трек-номер"),
    DELIVERY_HANDED_OVER(6, "Доставка: передан в доставку"),
    DELIVERY_SHIPPED(7, "Доставка: отправлен"),
    DELIVERY_COURIER_OR_POINT(8, "Доставка: поступил в пункт-выдачи / передан курьеру"),
    DELIVERY_RECEIVED(9, "Доставка: получен"),
    CANCELED(10, "Заказ Отменен: отмена заказа"),
    CANCELED_RETURN(11, "Заказ Отменен: возврат заказа"),
    CHANGED_UPDATE_ORDER(12, "Заказ Изменен: обновление заказа");

    companion object {
        fun fromCode(code: Long?): SSECTransactionStatus? =
            entries.find { it.code == code }
    }
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
                    prim.isString -> prim.asString
                    prim.isNumber -> {
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
