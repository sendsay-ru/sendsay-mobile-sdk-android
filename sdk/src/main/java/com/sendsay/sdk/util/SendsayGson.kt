package com.sendsay.sdk.util

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializer
import com.google.gson.ToNumberPolicy
import com.google.gson.reflect.TypeToken
import com.sendsay.sdk.models.CustomerRecommendation
import com.sendsay.sdk.models.CustomerRecommendationDeserializer
import com.sendsay.sdk.models.OrderItem
import com.sendsay.sdk.models.OrderItemAdapter
import com.sendsay.sdk.models.StrictNumberDeserializer
import com.sendsay.sdk.models.TrackSSECData
import com.sendsay.sdk.models.eventfilter.EventFilterAttribute
import com.sendsay.sdk.models.eventfilter.EventFilterConstraint
import com.sendsay.sdk.models.eventfilter.EventFilterOperator
import com.sendsay.sdk.models.eventfilter.EventFilterOperatorDeserializer
import com.sendsay.sdk.models.eventfilter.EventFilterOperatorSerializer

class SendsayGson {
    companion object {
        /**
         * Gson builder for Gson instance used by SDK.
         * Changes are not applied to 'instance' variable.
         */
        val builder: GsonBuilder = GsonBuilder()
//            .registerTypeAdapter(object : TypeToken<Long>() {}.type, JsonSerializer<Long> { src, _, _ ->
//                JsonPrimitive(src)
//            })
            // NaN and Infinity are serialized as strings.
            // Gson cannot serialize them, it can be setup to do it,
            // but then Sendsay servers fail to process the JSON afterwards.
            // This way devs know there is something going on and find the issue
            .registerTypeAdapter(
                object : TypeToken<Double>() {}.type,
                JsonSerializer<Double> { src, _, _ ->
                    if (src.isInfinite() || src.isNaN()) {
                        JsonPrimitive(src.toString())
                    } else {
                        JsonPrimitive(src)
                    }
                })
            .registerTypeAdapter(
                object : TypeToken<Float>() {}.type,
                JsonSerializer<Float> { src, _, _ ->
                    if (src.isInfinite() || src.isNaN()) {
                        JsonPrimitive(src.toString())
                    } else {
                        JsonPrimitive(src)
                    }
                })
            // Seems to be found some variant of solution.
            .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
//            .setObjectToNumberStrategy(ToNumberPolicy.LAZILY_PARSED_NUMBER)
            // ssec event
            .registerTypeAdapter(TrackSSECData::class.java, StrictNumberDeserializer())
            .registerTypeAdapter(OrderItem::class.java, OrderItemAdapter())
            // customer recommendation
            .registerTypeAdapter(
                CustomerRecommendation::class.java,
                CustomerRecommendationDeserializer()
            )
            // event filter
            .registerTypeHierarchyAdapter(
                EventFilterOperator::class.java,
                EventFilterOperatorSerializer()
            )
            .registerTypeHierarchyAdapter(
                EventFilterOperator::class.java,
                EventFilterOperatorDeserializer()
            )
            .registerTypeAdapterFactory(EventFilterAttribute.typeAdapterFactory)
            .registerTypeAdapterFactory(EventFilterConstraint.typeAdapterFactory)
            // keep HTML characters in JSON as are
            .disableHtmlEscaping()

        /**
         * Gson instance used by SDK.
         */
        val instance: Gson = builder.create()
    }
}
