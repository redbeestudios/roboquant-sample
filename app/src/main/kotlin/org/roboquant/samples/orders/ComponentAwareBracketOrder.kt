package org.roboquant.samples.orders

import org.roboquant.orders.SingleOrder

class ComponentAwareBracketOrder(
    val entry: SingleOrder,
    val takeProfit: SingleOrder,
    val stopLoss: SingleOrder,
    id: Int = nextId(),
    tag: String = ""
) : org.roboquant.orders.Order(entry.asset, id, tag) {

    init {
        require(entry.asset == takeProfit.asset && entry.asset == stopLoss.asset) { "Bracket orders can only contain orders for the same asset" }
        require(entry.quantity == -takeProfit.quantity && entry.quantity == -stopLoss.quantity) { "Bracket orders takeProfit and stopLoss orders need to close position" }
    }

    override fun info() = sortedMapOf("entry" to entry, "takeProfit" to takeProfit, "stopLoss" to "stopLoss")
}