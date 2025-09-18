package ru.sendsay.sdk.models.eventfilter

import ru.sendsay.sdk.models.eventfilter.operators.ContainsOperator
import ru.sendsay.sdk.models.eventfilter.operators.DoesNotContainOperator
import ru.sendsay.sdk.models.eventfilter.operators.DoesNotEqualOperator
import ru.sendsay.sdk.models.eventfilter.operators.EndsWithOperator
import ru.sendsay.sdk.models.eventfilter.operators.EqualToOperator
import ru.sendsay.sdk.models.eventfilter.operators.EqualsOperator
import ru.sendsay.sdk.models.eventfilter.operators.GreaterThanOperator
import ru.sendsay.sdk.models.eventfilter.operators.HasNoValueOperator
import ru.sendsay.sdk.models.eventfilter.operators.HasValueOperator
import ru.sendsay.sdk.models.eventfilter.operators.InBetweenOperator
import ru.sendsay.sdk.models.eventfilter.operators.InOperator
import ru.sendsay.sdk.models.eventfilter.operators.IsNotSetOperator
import ru.sendsay.sdk.models.eventfilter.operators.IsOperator
import ru.sendsay.sdk.models.eventfilter.operators.IsSetOperator
import ru.sendsay.sdk.models.eventfilter.operators.LessThanOperator
import ru.sendsay.sdk.models.eventfilter.operators.NotBetweenOperator
import ru.sendsay.sdk.models.eventfilter.operators.NotInOperator
import ru.sendsay.sdk.models.eventfilter.operators.RegexOperator
import ru.sendsay.sdk.models.eventfilter.operators.StartsWithOperator
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

abstract class EventFilterOperator {
    companion object {
        internal const val ANY_OPERAND_COUNT = -1

        internal val existingOperators = hashSetOf(
            // generic
            IsSetOperator(),
            IsNotSetOperator(),
            HasValueOperator(),
            HasNoValueOperator(),
            // string
            EqualsOperator(),
            DoesNotEqualOperator(),
            InOperator(),
            NotInOperator(),
            ContainsOperator(),
            DoesNotContainOperator(),
            StartsWithOperator(),
            EndsWithOperator(),
            RegexOperator(),
            // boolean
            IsOperator(),
            // number
            EqualToOperator(),
            InBetweenOperator(),
            NotBetweenOperator(),
            LessThanOperator(),
            GreaterThanOperator()
        )
    }
    abstract val name: String
    abstract val operandCount: Int
    abstract fun passes(
        event: EventFilterEvent,
        attribute: EventFilterAttribute,
        operands: List<EventFilterOperand>
    ): Boolean

    fun validate(operands: List<EventFilterOperand>): Boolean {
        if (operandCount != ANY_OPERAND_COUNT && operands.size != operandCount) {
            throw EventFilterOperatorException(
                "Incorrect number of operands for operator $name. Required $operandCount, got ${operands.size}"
            )
        }
        return true
    }

    override fun equals(other: Any?): Boolean {
        return other is EventFilterOperator && other.name == name
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}

class EventFilterOperatorException(message: String) : Exception(message)

class EventFilterOperatorSerializer : JsonSerializer<EventFilterOperator> {
    override fun serialize(
        src: EventFilterOperator,
        typeOfSrc: Type,
        context: JsonSerializationContext
    ): JsonElement {
        return JsonPrimitive(src.name)
    }
}

class EventFilterOperatorDeserializer : JsonDeserializer<EventFilterOperator> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): EventFilterOperator {
        return EventFilterOperator.existingOperators.firstOrNull {
            it.name == json.asString
        } ?: throw JsonParseException("Unknown operator type ${json.asString}")
    }
}
