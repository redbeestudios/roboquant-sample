package org.roboquant.samples.orders

import org.roboquant.brokers.sim.*
import org.roboquant.common.iszero
import org.roboquant.orders.OrderState
import org.roboquant.orders.OrderStatus
import java.time.Instant

class ComponentAwareBracketOrderHandler(order: ComponentAwareBracketOrder) : TradeOrderHandler {

    private val main = ExecutionEngine.getHandler(order.entry) as SingleOrderHandler<*>
    private val profit = ExecutionEngine.getHandler(order.takeProfit) as SingleOrderHandler<*>
    private val loss = ExecutionEngine.getHandler(order.stopLoss) as SingleOrderHandler<*>

    override var state: OrderState = ComponentAwareBracketOrderState(order, main.state, profit.state, loss.state)

    override fun execute(pricing: Pricing, time: Instant): List<Execution> {

        val executions = mutableListOf<Execution>()
        if (main.state.status.open) executions.addAll(main.execute(pricing, time))


        if (loss.fill.iszero && main.state.status.completed) executions.addAll(profit.execute(pricing, time))
        if (profit.fill.iszero && main.state.status.completed) executions.addAll(loss.execute(pricing, time))

        val remaining = main.qty + loss.fill + profit.fill
        state = if (remaining.iszero) (state as ComponentAwareBracketOrderState).update(
            time,
            main.state,
            profit.state,
            loss.state,
            OrderStatus.COMPLETED
        )
        else (state as ComponentAwareBracketOrderState).update(time, main.state, profit.state, loss.state)

        return executions
    }
}