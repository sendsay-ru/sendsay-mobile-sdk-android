package com.sendsay.sdk.models

import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import kotlinx.datetime.format.DateTimeComponents
//import java.time.LocalDateTime
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern

class TrackSSECDataBuilder(private val type: TrackingSSECType) {
    private var productId: String? = null
    private var productName: String? = null
    private var productDateTime: String? = null
    private var productPicture: List<String>? = null
    private var productUrl: String? = null
    private var productAvailable: Int? = null
    private var productCategoryPaths: List<String>? = null
    private var productCategoryId: Long? = null
    private var productCategory: String? = null
    private var productDescription: String? = null
    private var productVendor: String? = null
    private var productModel: String? = null
    private var productType: String? = null
    private var productPrice: Double? = null
    private var productOldPrice: Double? = null
    private var updatePerItem: Int? = null
    private var update: Int? = null
    private var transactionId: String? = null
    private var transactionDt: String? = null
    private var transactionSum: Double? = null
    private var transactionDiscount: Double? = null
    private var deliveryDt: String? = null
    private var deliveryPrice: Double? = null
    private var paymentDt: String? = null
    private var items: List<OrderItem>? = null
    private var cpMap: Map<String, Any>? = null

    // общий setters
    fun setProduct(
        id: String? = null,
        name: String? = null,
        dateTime: String? = null,
        picture: List<String>? = null,
        url: String? = null,
        available: Int? = null,
        categoryPaths: List<String>? = null,
        categoryId: Long? = null,
        category: String? = null,
        description: String? = null,
        vendor: String? = null,
        model: String? = null,
        type: String? = null,
        price: Double? = null,
        oldPrice: Double? = null,
    ) = apply {
        this.productId = id
        this.productName = name
        this.productDateTime = dateTime
        this.productPicture = picture
        this.productUrl = url
        this.productAvailable = available
        this.productCategoryPaths = categoryPaths
        this.productCategoryId = categoryId
        this.productCategory = category
        this.productDescription = description
        this.productVendor = vendor
        this.productModel = model
        this.productType = type
        this.productPrice = price
        this.productOldPrice = oldPrice
    }

    fun setTransaction(
        id: String? = null,
        dt: String? = null,
        sum: Double? = null,
        discount: Double? = null
    ) = apply {
        this.transactionId = id
        this.transactionDt = dt
        this.transactionSum = sum
        this.transactionDiscount = discount
    }

    fun setDelivery(
        dt: String? = null,
        deliveryPrice: Double? = null
    ) = apply {
        this.deliveryDt = dt
        this.deliveryPrice = deliveryPrice
    }

    fun setPayment(dt: String) = apply {
        this.paymentDt = dt
    }

    fun setItems(items: List<OrderItem>) = apply { this.items = items }

    fun build(): TrackSSECData {
        when (type) {
            TrackingSSECType.VIEW_PRODUCT -> {
                requireNotNull(productId) { "product.id is required for VIEW_PRODUCT" }
            }

            TrackingSSECType.ORDER -> {
                requireNotNull(transactionId) { "transaction.id is required for type ORDER" }
                requireNotNull(transactionDt) { "transaction.dt is required for type ORDER" }
                if (update == 1) require(!items.isNullOrEmpty()) { "items must be provided for type ORDER and 'update' == 1" }
            }

            TrackingSSECType.BASKET_ADD -> {
                requireNotNull(transactionId) { "transaction.id is required for type BASKET_ADD" }
                requireNotNull(transactionDt) { "transaction.dt is required for type BASKET_ADD" }
                require(!items.isNullOrEmpty()) { "items must be provided for type BASKET_ADD" }
                items?.any { it.id.isEmpty() || it.price == null || it.qnt == null }
                    ?.let { require(!(it)) { "items must content id, price & qnt for type BASKET_ADD" } }
                updatePerItem = 0
            }

            TrackingSSECType.BASKET_CLEAR -> {
                requireNotNull(productDateTime) { "product.id is required for type BASKET_CLEAR" }
                require(!items.isNullOrEmpty()) { "items must be provided for type BASKET_CLEAR" }
                items?.any { it.id.isEmpty() }
                    ?.let { require(!(it)) { "items must content id for type BASKET_CLEAR" } }
            }

            else -> {
                // другие события
            }
        }

//        val formattedProductDateTime = getFormatDT(productDateTime)
//        val formattedTransactionDt = getFormatDT(transactionDt)
//        val formattedDeliveryDt = getFormatDT(deliveryDt)
//        val formattedPaymentDt = getFormatDT(paymentDt)
        val formattedProductDateTime = productDateTime
        val formattedTransactionDt = transactionDt
        val formattedDeliveryDt = deliveryDt
        val formattedPaymentDt = paymentDt

        return TrackSSECData(
            productId = productId,
            productName = productName,
            dateTime = formattedProductDateTime,
            picture = productPicture,
            url = productUrl,
            available = productAvailable,
            categoryPaths = productCategoryPaths,
            categoryId = productCategoryId,
            category = productCategory,
            description = productDescription,
            vendor = productVendor,
            model = productModel,
            type = productType,
            price = null,
            oldPrice = null,
            transactionId = transactionId,
            transactionDt = formattedTransactionDt,
            transactionStatus = null,
            transactionDiscount = null,
            transactionSum = null,
            deliveryDt = formattedDeliveryDt,
            deliveryPrice = null,
            paymentDt = formattedPaymentDt,
            items = items,
            cp = cpMap
        )
    }

//    private fun getFormatDT(dt: LocalDateTime?): String? {
//        @OptIn(FormatStringsInDatetimeFormats::class)
//        val customFormat =
//            DateTimeComponents.Format {
//                byUnicodePattern("yyyy-MM-dd HH:mm:ss")
//            }
//        return if (dt != null) customFormat.parse(dt.toString())
//            .toString() else null
//    }

    companion object {
        val gsonAdapter = GsonBuilder()
            .registerTypeAdapter(TrackSSECData::class.java, SsecPayloadDeserializer())
            .create()
    }
}