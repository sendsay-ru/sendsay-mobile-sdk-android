package com.sendsay.example.view.fragments

import TokenTracker
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.sendsay.example.App
import com.sendsay.example.databinding.FragmentTrackBinding
import com.sendsay.example.managers.CustomerTokenStorage
import com.sendsay.example.models.Constants
import com.sendsay.example.view.base.BaseFragment
import com.sendsay.example.view.dialogs.TrackCustomAttributesDialog
import com.sendsay.example.view.dialogs.TrackCustomEventDialog
import com.sendsay.sdk.Sendsay
import com.sendsay.sdk.models.CustomerIds
import com.sendsay.sdk.models.NotificationData
import com.sendsay.sdk.models.OrderItem
import com.sendsay.sdk.models.PropertiesList
import com.sendsay.sdk.models.PurchasedItem
import com.sendsay.sdk.models.TrackSSECDataBuilder
import com.sendsay.sdk.models.TrackingSSECType
import com.sendsay.sdk.util.Logger
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.absoluteValue
import kotlin.random.Random

class TrackFragment : BaseFragment(), AdapterView.OnItemClickListener {

    private lateinit var viewBinding: FragmentTrackBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = FragmentTrackBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    companion object {
        fun mockItems(): ArrayList<String> {
            val list = arrayListOf<String>()
            for (i in 1..14) {
                list.add("Item #$i")
            }
            return list
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.subtitle = "tracking"

        // Track visited screen
        trackPage(Constants.ScreenNames.purchaseScreen)

        viewBinding.listView.adapter = Adapter()

        // Init buttons listeners
        initListeners()
    }

    private fun initListeners() {
        viewBinding.listView.onItemClickListener = this

        viewBinding.buttonTrackClicked.setOnClickListener { trackPushClicked() }
        viewBinding.buttonTrackDelivered.setOnClickListener { trackPushDelivered() }
        viewBinding.buttonTrackToken.setOnClickListener { trackToken() }
        viewBinding.buttonAuthorizePush.setOnClickListener { requestPushAuthorization() }

        viewBinding.buttonTrackProductView.setOnClickListener { trackProductView() }
        viewBinding.buttonTrackOrder.setOnClickListener { trackOrder() }
        viewBinding.buttonTrackBasket.setOnClickListener { trackBasket() }
        viewBinding.buttonTrackClearBasket.setOnClickListener { trackClearBasket() }

        viewBinding.buttonUpdateProperties.setOnClickListener {
            TrackCustomAttributesDialog.show(childFragmentManager) {
                trackUpdateCustomerProperties(it)
            }
        }

        viewBinding.buttonCustomEvent.setOnClickListener {
            TrackCustomEventDialog.show(childFragmentManager) { eventName, properties ->
                trackCustomEvent(eventName, properties)
            }
        }
    }

    private fun requestPushAuthorization() {
        Sendsay.requestPushAuthorization(requireContext()) { granted ->
            Logger.i(this, "Push notifications are allowed: $granted")
        }
    }

    /**
     * Method to handle custom event tracking obtained by TrackCustomEventDialog
     */
    private fun trackCustomEvent(eventName: String, properties: HashMap<String, Any>) {
        Sendsay.trackEvent(
            eventType = eventName,
            properties = PropertiesList(properties = properties).toHashMap()
        )
    }

    /**
     * Method to handle push clicked event tracking
     */
    private fun trackPushClicked() {
        Sendsay.trackClickedPush(
            NotificationData(hashMapOf("campaign_id" to "id"))
        )
    }

    /**
     * Method to handle updating customer properties
     */
    private fun trackUpdateCustomerProperties(propertiesList: HashMap<String, Any>) {
        val registeredIdUpdate = propertiesList.remove("registered") as? String
        if (registeredIdUpdate != null) {
            App.instance.registeredIdManager.registeredID = registeredIdUpdate
        }
        val customerIds = CustomerIds()
            .withId("registered", (App.instance.registeredIdManager.registeredID))
            .withId("phone", "+79602386404")
        CustomerTokenStorage.INSTANCE.configure(
            customerIds = hashMapOf(
                "registered" to (App.instance.registeredIdManager.registeredID ?: "")
            )
        )

        // Логируем customerIds в Logcat
        Logger.d(this, "customerIds: $customerIds")
        //Logger.d(this, "[CTS] Conf loaded $confJson")

        Sendsay.identifyCustomer(
            customerIds = customerIds,
            properties = propertiesList
        )
    }

    /**
     * Method to handle push delivered event tracking"
     */
    private fun trackPushDelivered() {
        Sendsay.trackDeliveredPush(
            data = NotificationData(hashMapOf("campaign_id" to "id"))
        )
    }

    /**
     * Method to handle token tracking
     */
    private fun trackToken() {
        TokenTracker().trackToken(context)
    }


    private fun trackClearBasket() {
//        val currentDateTime = LocalDateTime.now()
        // Получение текущего времени с использованием SimpleDateFormat
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        val currentDateTime2 = dateFormat.format(Date())

        val data = TrackSSECDataBuilder(TrackingSSECType.BASKET_CLEAR)
            .setProduct(dateTime = currentDateTime2)
            .setItems(listOf(OrderItem(id = "-1")))
            .build()
        try {
            Sendsay.trackSSECEvent(TrackingSSECType.BASKET_CLEAR, data)
        } catch (e: IllegalArgumentException) {
            Log.e(TrackingSSECType.BASKET_CLEAR.name, e.stackTrace.toString())
        }

//        val jsonString = """
//        {"dt":"$currentDateTime2",
//            "items": [
//              {
//                "id": -1
//              }
//            ]
//          }
//        """.trimIndent()
//        val jsonToSsecExample: TrackSSECData =
//            SendsayGson.instance.fromJson(jsonString, TrackSSECData::class.java)
//        try {
//            Sendsay.trackSSECEvent(TrackingSSECType.BASKET_CLEAR, jsonToSsecExample)
//        } catch (e: IllegalArgumentException) {
//            Log.e(TrackingSSECType.BASKET_CLEAR.name, e.stackTrace.toString())
//        }
    }


    private fun trackProductView() {
//        val currentDateTime = LocalDateTime.now()
        // Получение текущего времени с использованием SimpleDateFormat
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        val currentDateTime2 = dateFormat.format(Date())

        val productData = TrackSSECDataBuilder(TrackingSSECType.VIEW_PRODUCT)
            .setProduct(
                dateTime = currentDateTime2,
                id = "product1",
                price = 7.88,
                available = 1,
                name = "name",
                oldPrice = 5.99,
                picture = listOf(),
                url = "url",
                model = "model",
                vendor = "vendor",
                categoryId = 777,
                category = "category_name",
            )
            .build()
        try {
            Sendsay.trackSSECEvent(TrackingSSECType.VIEW_PRODUCT, productData)
        } catch (e: IllegalArgumentException) {
            Log.e(TrackingSSECType.BASKET_ADD.name, e.stackTrace.toString())
        }

//        val jsonString = """
//        {"dt":"$currentDateTime2",
//            "id": "product1",
//            "available": 1,
//            "name": "name",
//            "price": 7.88,
//            "old_price": 5.99,
//            "picture": [],
//            "url": "url",
//            "model": "model",
//            "vendor": "vendor",
//            "category_id": 777,
//            "category": "category name"
//        }""".trimIndent()
//
//        val jsonToSsecExample: TrackSSECData =
//            SendsayGson.instance.fromJson(jsonString, TrackSSECData::class.java)
//        Sendsay.trackSSECEvent(TrackingSSECType.VIEW_PRODUCT, jsonToSsecExample)
    }

    private fun trackOrder() {
        // Генерация случайного transaction_id без отрицательных чисел
        val randomTransactionId = Random.nextLong().absoluteValue.toString()

//        val currentDateTime = LocalDateTime.now()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        val currentDateTime2 = dateFormat.format(Date())

//        Log.d("fasdfasdfsd1", currentDateTime.toString())
//        Log.d("fasdfasdfsd2", currentDateTime2)
//        Log.d("fasdfasdfsd3", currentDateTime2.toString())

        val orderData = TrackSSECDataBuilder(TrackingSSECType.ORDER)
            .setUpdate(isUpdatePerItem = false)
            .setTransaction(id = randomTransactionId, dt = currentDateTime2, sum = 100.9, status = 1)
            .setItems(
                listOf(
                    OrderItem(
                        id = "product1",
                        qnt = 1,
                        price = 7.88,
                        available = 1,
                        name = "name",
                        oldPrice = 5.99,
                        picture = listOf(),
                        url = "url",
                        model = "model",
                        vendor = "vendor",
                        categoryId = 777,
                        category = "category_name",
                    )
                )
            )
            .build()
        try {
            Sendsay.trackSSECEvent(TrackingSSECType.ORDER, orderData)
        } catch (e: IllegalArgumentException) {
            Log.e(TrackingSSECType.ORDER.name, e.stackTrace.toString())
        }

//        val jsonString = """
//       {"dt":"$currentDateTime2",
//               "transaction_id": "$randomTransactionId",
//               "transaction_dt": "$currentDateTime2",
//               "transaction_sum": 100.9,
//               "transaction_status": 1,
//               "update_per_item": 0,
//               "items": [
//                 {
//                   "id": "product1",
//                   "available": 1,
//                   "name": "name",
//                   "qnt": 1,
//                   "price": 7.88,
//                   "old_price": 5.99,
//                   "picture": [],
//                   "url": "url",
//                   "model": "model",
//                   "vendor": "vendor",
//                   "category_id": 777,
//                   "category": "category name"
//                 }
//               ]
//            }
//            """.trimIndent()
//        val jsonToSsecExample: TrackSSECData =
//            SendsayGson.instance.fromJson(jsonString, TrackSSECData::class.java)
//        Sendsay.trackSSECEvent(TrackingSSECType.ORDER, jsonToSsecExample)
    }

    private fun trackBasket() {
        // Генерация случайного transaction_id без отрицательных чисел
        val randomTransactionId = Random.nextLong().absoluteValue.toString()

//        val currentDateTime = LocalDateTime.now()
        // Получение текущего времени с использованием SimpleDateFormat
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        val currentDateTime2 = dateFormat.format(Date())

        val orderData = TrackSSECDataBuilder(TrackingSSECType.BASKET_ADD)
            .setTransaction(id = randomTransactionId, dt = currentDateTime2, sum = 100.9)
            .setItems(
                listOf(
                    OrderItem(
                        id = "product1",
                        qnt = 1,
                        price = 7.88,
                        available = 1,
                        name = "name",
                        oldPrice = 5.99,
                        picture = listOf(),
                        url = "url",
                        model = "model",
                        vendor = "vendor",
                        categoryId = 777,
                        category = "category_name",
                    )
                )
            )
            .build()
        try {
            Sendsay.trackSSECEvent(TrackingSSECType.BASKET_ADD, orderData)
        } catch (e: IllegalArgumentException) {
            Log.e(TrackingSSECType.BASKET_ADD.name, e.stackTrace.toString())
        }

//        val jsonString = """
//           {
//                   "dt":"$currentDateTime",
//                   "transaction_sum": 100.9,
//                   "update_per_item": 0,
//                   "items": [
//                     {
//                       "id": "product1",
//                       "available": 1,
//                       "name": "name",
//                       "qnt": 1,
//                       "price": 7.88,
//                       "old_price": 5.99,
//                       "picture": [],
//                       "url": "url",
//                       "model": "model",
//                       "vendor": "vendor",
//                       "category_id": 777,
//                       "category": "category name"
//
//                     }
//                   ]
//            }""".trimIndent()
//        val jsonToSsecExample: TrackSSECData =
//            SendsayGson.instance.fromJson(jsonString, TrackSSECData::class.java)
//        Sendsay.trackSSECEvent(TrackingSSECType.BASKET_ADD, jsonToSsecExample)
    }


    /**
     * Method to manually track customer's purchases
     */
    private fun trackPayment(position: Int) {
        val purchasedItem = PurchasedItem(
            value = 2011.1,
            currency = "USD",
            paymentSystem = "System",
            productId = id.toString(),
            productTitle = mockItems()[position]
        )
        Sendsay.trackPaymentEvent(
            purchasedItem = purchasedItem
        )
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

        // Track purchase at position
        trackPayment(position)
        Toast.makeText(context, "Payment Tracked", Toast.LENGTH_SHORT).show()
    }

    inner class Adapter : BaseAdapter() {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val inflater = LayoutInflater.from(parent?.context)
            if (convertView == null) {
                val view = inflater.inflate(android.R.layout.simple_list_item_1, parent, false)
                view.findViewById<TextView>(android.R.id.text1).text = mockItems()[position]
                return view
            }
            convertView.findViewById<TextView>(android.R.id.text1).text = mockItems()[position]
            return convertView
        }

        override fun getItem(position: Int): Any {
            return mockItems()[position]
        }

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getCount() = mockItems().size
    }
}
