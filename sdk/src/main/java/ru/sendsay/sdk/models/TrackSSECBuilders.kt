package ru.sendsay.sdk.models

// Типо-безопасные билдеры-обёртки вокруг существующего TrackSSECDataBuilder
// Каждый билдер знает свой обязательный набор полей и делегирует в core-билдер.
interface TrackSSECBuilder {
    fun cp(map: Map<String, Any>): TrackSSECBuilder
    fun buildData(): TrackSSECData
    fun buildProperties(): HashMap<String, Any>
}

/** VIEW_PRODUCT */
class ViewProductBuilder internal constructor(
    private val core: TrackSSECDataCore = TrackSSECDataCore(TrackingSSECType.VIEW_PRODUCT)
) : TrackSSECBuilder {

    /** Обязательное поле — id; остальные не обязательны */
    fun product(
        id: String,
        name: String? = null,
        dateTime: String? = null,
        picture: List<String>? = null,
        url: String? = null,
        available: Long? = null,
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
        core.setProduct(
            id = id, name = name, dateTime = dateTime, picture = picture, url = url,
            available = available, categoryPaths = categoryPaths, categoryId = categoryId,
            category = category, description = description, vendor = vendor, model = model,
            type = type, price = price, oldPrice = oldPrice
        )
    }

    fun email(value: String) = apply { core.setEmail(value) }
    fun update(isUpdate: Boolean? = null, isUpdatePerItem: Boolean? = null) = apply {
        core.setUpdate(isUpdate, isUpdatePerItem)
    }

    override fun cp(map: Map<String, Any>) = apply { core.setCp(map) }
    override fun buildData(): TrackSSECData = core.build()
    override fun buildProperties(): HashMap<String, Any> =
        core.build().toProperties(TrackingSSECType.VIEW_PRODUCT)
}

/** ORDER */
class OrderBuilder internal constructor(
    private val core: TrackSSECDataCore = TrackSSECDataCore(TrackingSSECType.ORDER)
) : TrackSSECBuilder {

    /** discount - необязательное для заказа поле */
    fun transaction(
        id: String,
        dt: String,
        sum: Double,
        status: Long,
        discount: Double? = null
    ) = apply {
        core.setTransaction(id = id, dt = dt, sum = sum, discount = discount, status = status)
    }

    fun update(isUpdate: Boolean? = null, isUpdatePerItem: Boolean? = null) = apply {
        core.setUpdate(isUpdate, isUpdatePerItem)
    }

    fun delivery(dt: String, price: Double? = null) = apply { core.setDelivery(dt, price) }
    fun payment(dt: String) = apply { core.setPayment(dt) }

    /** Товары — без nullable внутри */
    fun items(items: List<OrderItem>) = apply { core.setItems(items) }

    /** Иногда магазины передают product-поля и для ORDER */
    fun productOptional(
        id: String? = null,
        name: String? = null,
        price: Double? = null
    ) = apply {
        core.setProduct(id = id, name = name, price = price)
    }

    override fun cp(map: Map<String, Any>) = apply { core.setCp(map) }
    override fun buildData(): TrackSSECData = core.build()
    override fun buildProperties(): HashMap<String, Any> =
        core.build().toProperties(TrackingSSECType.ORDER)
}

/** BASKET_ADD */
class BasketAddBuilder internal constructor(
    private val core: TrackSSECDataCore = TrackSSECDataCore(TrackingSSECType.BASKET_ADD)
) : TrackSSECBuilder {

    fun transaction(
        id: String,
        dt: String,
        sum: Double? = null,
        status: Long? = null,
        discount: Double? = null
    ) = apply {
        core.setTransaction(
            id = id,
            dt = dt,
            sum = sum,
            status = status,
            discount = discount
        )
    }

    fun items(items: List<OrderItem>) = apply { core.setItems(items) }

    override fun cp(map: Map<String, Any>) = apply { core.setCp(map) }
    override fun buildData(): TrackSSECData = core.build()
    override fun buildProperties(): HashMap<String, Any> =
        core.build().toProperties(TrackingSSECType.BASKET_ADD)
}

/** BASKET_CLEAR */
class BasketClearBuilder internal constructor(
    private val core: TrackSSECDataCore = TrackSSECDataCore(TrackingSSECType.BASKET_CLEAR)
) : TrackSSECBuilder {

    fun items(items: List<OrderItem>) = apply { core.setItems(items) }
    fun dateTime(dt: String) = apply { core.setProduct(dateTime = dt) }

    override fun cp(map: Map<String, Any>) = apply { core.setCp(map) }
    override fun buildData(): TrackSSECData = core.build()
    override fun buildProperties(): HashMap<String, Any> =
        core.build().toProperties(TrackingSSECType.BASKET_CLEAR)
}

/** Фабрика удобного доступа */
object TrackSSEC {
    fun viewProduct() = ViewProductBuilder()
    fun order() = OrderBuilder()
    fun basketAdd() = BasketAddBuilder()
    fun basketClear() = BasketClearBuilder()

    /** Универсальный вход по enum */
    fun builder(type: TrackingSSECType): TrackSSECBuilder = when (type) {
        TrackingSSECType.VIEW_PRODUCT -> viewProduct()
        TrackingSSECType.ORDER -> order()
        TrackingSSECType.BASKET_ADD -> basketAdd()
        TrackingSSECType.BASKET_CLEAR -> basketClear()
        else -> viewProduct() // Добавляем остальные кейсы по мере необходимости
    }
}


class TrackSSECDataCore(private val type: TrackingSSECType) {
    private var productId: String? = null
    private var productName: String? = null
    private var productDateTime: String? = null
    private var productPicture: List<String>? = null
    private var productUrl: String? = null
    private var productAvailable: Long? = null
    private var productCategoryPaths: List<String>? = null
    private var productCategoryId: Long? = null
    private var productCategory: String? = null
    private var productDescription: String? = null
    private var productVendor: String? = null
    private var productModel: String? = null
    private var productType: String? = null
    private var productPrice: Double? = null
    private var productOldPrice: Double? = null
    private var email: String? = null
    private var updatePerItem: Int? = null
    private var update: Int? = null
    private var transactionId: String? = null
    private var transactionDt: String? = null
    private var transactionSum: Double? = null
    private var transactionDiscount: Double? = null
    private var transactionStatus: Long? = null
    private var deliveryDt: String? = null
    private var deliveryPrice: Double? = null
    private var paymentDt: String? = null
    private var items: List<OrderItem>? = null
    private var cpMap: Map<String, Any>? = null

    fun setCp(cp: Map<String, Any>) = apply { this.cpMap = cp }

    // общий setters
    fun setProduct(
        id: String? = null,
        name: String? = null,
        dateTime: String? = null,
        picture: List<String>? = null,
        url: String? = null,
        available: Long? = null,
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
        discount: Double? = null,
        status: Long? = null
    ) = apply {
        this.transactionId = id
        this.transactionDt = dt
        this.transactionSum = sum
        this.transactionDiscount = discount
        this.transactionStatus = status
    }

    fun setDelivery(
        dt: String,
        deliveryPrice: Double? = null
    ) = apply {
        this.deliveryDt = dt
        this.deliveryPrice = deliveryPrice
    }

    fun setPayment(dt: String) = apply {
        this.paymentDt = dt
    }

    fun setEmail(email: String) = apply {
        this.email = email
    }

    fun setUpdate(isUpdate: Boolean? = null, isUpdatePerItem: Boolean? = null) = apply {
        isUpdate?.let { this.update = if (it) 1 else 0 }
        isUpdatePerItem?.let { this.updatePerItem = if (it) 1 else 0 }
    }

    fun setItems(items: List<OrderItem>) = apply { this.items = items }

    /**
     * Формирует плоскую карту ключей без префиксов для отправки в properties["ssec"].
     * Набор полей зависит от типа события, чтобы исключить конфликт имён (id/dt/price и т.п.).
     */
    fun buildSsecMap(): Map<String, Any> {
        val out = linkedMapOf<String, Any>()
        fun put(k: String, v: Any?) {
            if (v != null) out[k] = v
        }

        when (type) {
            TrackingSSECType.VIEW_PRODUCT -> {
                requireNotNull(productId) { "product.id is required for VIEW_PRODUCT" }
            }

            TrackingSSECType.ORDER -> {
                requireNotNull(transactionId) { "transaction.id is required for type ORDER" }
                requireNotNull(transactionDt) { "transaction.dt is required for type ORDER" }
                requireNotNull(transactionSum) { "transaction.sum is required for type ORDER" }
                requireNotNull(transactionStatus) { "transaction.status is required for type ORDER" }
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
//                requireNotNull(productDateTime) { "product.dt is required for type BASKET_CLEAR" }
                require(!items.isNullOrEmpty()) { "items must be provided for type BASKET_CLEAR" }
                items?.any { it.id.isEmpty() }
                    ?.let { require(!(it)) { "items must content id for type BASKET_CLEAR" } }
            }

            else -> {
                // другие события
            }
        }

        when (type) {
            TrackingSSECType.ORDER -> {
                // транзакционные поля
                put("id", transactionId)
                put("dt", transactionDt)
                put("status", transactionStatus)
                put("discount", transactionDiscount)
                put("sum", transactionSum)

                // уникализируем потенциально конфликтные поля
                put("dt_delivery", deliveryDt)
                put("price_delivery", deliveryPrice)
                put("dt_payment", paymentDt)

                if (!items.isNullOrEmpty()) out["items"] = items!!
            }

            else -> {
                // продуктовые поля
                put("id", productId)
                put("name", productName)
                put("dt", productDateTime)
                put("picture", productPicture)
                put("url", productUrl)
                put("available", productAvailable)
                put("category_paths", productCategoryPaths)
                put("category_id", productCategoryId)
                put("category", productCategory)
                put("description", productDescription)
                put("vendor", productVendor)
                put("model", productModel)
                put("type", productType)
                put("price", productPrice)
                put("old_price", productOldPrice)
                // дополнительные флаги
                put("email", email)
                put("update_per_item", updatePerItem)
                put("update", update)
            }
        }

        // cp1..cp20 и любые расширения
        cpMap?.forEach { (k, v) -> put(k, v) }

        return out
    }

    /** Удобный хелпер: готовые properties для отправки/кеширования */
    fun buildProperties(): HashMap<String, Any> = hashMapOf("ssec" to buildSsecMap())

    fun build(): TrackSSECData {
        when (type) {
            TrackingSSECType.VIEW_PRODUCT -> {
                requireNotNull(productId) { "product.id is required for VIEW_PRODUCT" }
            }

            TrackingSSECType.ORDER -> {
                requireNotNull(transactionId) { "transaction.id is required for type ORDER" }
                requireNotNull(transactionDt) { "transaction.dt is required for type ORDER" }
                requireNotNull(transactionSum) { "transaction.sum is required for type ORDER" }
                requireNotNull(transactionStatus) { "transaction.status is required for type ORDER" }
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
//                requireNotNull(productDateTime) { "product.dt is required for type BASKET_CLEAR" }
                require(!items.isNullOrEmpty()) { "items must be provided for type BASKET_CLEAR" }
                items?.any { it.id.isEmpty() }
                    ?.let { require(!(it)) { "items must content id for type BASKET_CLEAR" } }
            }

            else -> {
                // другие события
            }
        }

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
            price = productPrice,
            oldPrice = productOldPrice,

            email = email,
            updatePerItem = updatePerItem,
            update = update,

            transactionId = transactionId,
            transactionDt = formattedTransactionDt,
            transactionSum = transactionSum,
            transactionDiscount = transactionDiscount,
            transactionStatus = transactionStatus,
            deliveryDt = formattedDeliveryDt,
            deliveryPrice = deliveryPrice,
            paymentDt = formattedPaymentDt,
            items = items,
            cp = cpMap
        )
    }
}