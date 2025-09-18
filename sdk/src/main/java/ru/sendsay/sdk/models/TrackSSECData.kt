package ru.sendsay.sdk.models

import com.google.gson.Gson
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type
import com.google.gson.reflect.TypeToken


data class TrackSSECData(
    @SerializedName("product_id")
    val productId: String? = null,

    @SerializedName("product_name")
    val productName: String? = null,

    @SerializedName("dt")
    val dateTime: String? = null,

    @SerializedName("product_picture")
    val picture: List<String>? = null,

    @SerializedName("product_url")
    val url: String? = null,

    @SerializedName("product_available")
    val available: Long? = null,

    @SerializedName("product_category_paths")
    val categoryPaths: List<String>? = null,

    @SerializedName("product_category_id")
    val categoryId: Long? = null,

    @SerializedName("product_category")
    val category: String? = null,

    @SerializedName("product_description")
    val description: String? = null,

    @SerializedName("product_vendor")
    val vendor: String? = null,

    @SerializedName("product_model")
    val model: String? = null,

    @SerializedName("product_type")
    val type: String? = null,

    @SerializedName("product_price")
    val price: Double? = null,

    @SerializedName("product_old_price")
    val oldPrice: Double? = null,

    // Остальные необязательные поля
    @SerializedName("email")
    val email: String? = null,

    @SerializedName("update_per_item")
    val updatePerItem: Long? = null,

    @SerializedName("update")
    val update: Long? = null,


    // Платёжные и транзакционные данные
    @SerializedName("transaction_id")
    val transactionId: String? = null,

    @SerializedName("transaction_dt")
    val transactionDt: String? = null,

    @SerializedName("transaction_status")
    val transactionStatus: Long? = null,

    @SerializedName("transaction_discount")
    val transactionDiscount: Double? = null,

    @SerializedName("transaction_sum")
    val transactionSum: Double? = null,

    // Доставка и оплата
    @SerializedName("delivery_dt")
    val deliveryDt: String? = null,

    @SerializedName("delivery_price")
    val deliveryPrice: Double? = null,

    @SerializedName("payment_dt")
    val paymentDt: String? = null,

    // items (для заказов/корзины)
    @SerializedName("items")
    val items: List<OrderItem>? = null,

    // cp1..cp20
    val cp: Map<String, Any>? = null
) {
    fun toHashmap(): HashMap<String, Any> {
        return hashMapOf("ssec" to this)
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

// Кастомный сериализатор для cp1..cp20
class SsecPayloadDeserializer : JsonDeserializer<TrackSSECData> {

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): TrackSSECData {
        val obj = json.asJsonObject

        // Собираем cp1..cp20 в Map
        val cpMap = obj.entrySet()
            .filter { it.key.matches(Regex("cp\\d+")) }
            .associate { it.key to it.value }
//            .associate { it.key to it.value.asString }

        // Десериализуем остальное стандартно
        val delegate = Gson().fromJson(obj, TrackSSECData::class.java)

        return delegate.copy(cp = cpMap)
    }
}
