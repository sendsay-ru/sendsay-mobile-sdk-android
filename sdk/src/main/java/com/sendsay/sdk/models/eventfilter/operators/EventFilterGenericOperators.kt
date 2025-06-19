package com.sendsay.sdk.models.eventfilter.operators

import com.sendsay.sdk.models.eventfilter.EventFilterAttribute
import com.sendsay.sdk.models.eventfilter.EventFilterEvent
import com.sendsay.sdk.models.eventfilter.EventFilterOperand
import com.sendsay.sdk.models.eventfilter.EventFilterOperator

internal class IsSetOperator : EventFilterOperator() {
    override val name: String = "is set"
    override val operandCount: Int = 0
    override fun passes(
        event: EventFilterEvent,
        attribute: EventFilterAttribute,
        operands: List<EventFilterOperand>
    ): Boolean {
        return attribute.isSet(event)
    }
}

internal class IsNotSetOperator : EventFilterOperator() {
    override val name: String = "is not set"
    override val operandCount: Int = 0
    override fun passes(
        event: EventFilterEvent,
        attribute: EventFilterAttribute,
        operands: List<EventFilterOperand>
    ): Boolean {
        return !attribute.isSet(event)
    }
}

internal class HasValueOperator : EventFilterOperator() {
    override val name: String = "has value"
    override val operandCount: Int = 0
    override fun passes(
        event: EventFilterEvent,
        attribute: EventFilterAttribute,
        operands: List<EventFilterOperand>
    ): Boolean {
        return attribute.isSet(event) && attribute.getValue(event) != null
    }
}

internal class HasNoValueOperator : EventFilterOperator() {
    override val name: String = "has no value"
    override val operandCount: Int = 0
    override fun passes(
        event: EventFilterEvent,
        attribute: EventFilterAttribute,
        operands: List<EventFilterOperand>
    ): Boolean {
        return attribute.isSet(event) && attribute.getValue(event) == null
    }
}
