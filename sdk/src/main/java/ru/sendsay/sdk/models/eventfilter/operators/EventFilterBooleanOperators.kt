// ktlint-disable filename
package ru.sendsay.sdk.models.eventfilter.operators

import ru.sendsay.sdk.models.eventfilter.EventFilterAttribute
import ru.sendsay.sdk.models.eventfilter.EventFilterEvent
import ru.sendsay.sdk.models.eventfilter.EventFilterOperand
import ru.sendsay.sdk.models.eventfilter.EventFilterOperator

internal class IsOperator : EventFilterOperator() {
    override val name: String = "is"
    override val operandCount: Int = 1
    override fun passes(
        event: EventFilterEvent,
        attribute: EventFilterAttribute,
        operands: List<EventFilterOperand>
    ): Boolean {
        return attribute.getValue(event)?.equals(operands[0].value) == true
    }
}
