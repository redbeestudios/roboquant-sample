package org.roboquant.samples.orders

import org.roboquant.orders.OrderState
import org.roboquant.orders.OrderStatus
import java.time.Instant

data class ComponentAwareBracketOrderState(
    override val order: ComponentAwareBracketOrder,
    val entryState: OrderState,
    val takeProfitState: OrderState,
    val stopLossState: OrderState,
    override val status: OrderStatus = OrderStatus.INITIAL,
    override val openedAt: Instant = Instant.MIN,
    override val closedAt: Instant = Instant.MAX,
) : OrderState() {

    /**
     *  Update the order state and return the new order state (if applicable)
     *
     * @param time
     * @param newStatus
     * @return
     */
    fun update(
        time: Instant,
        entryState: OrderState,
        takeProfitState: OrderState,
        stopLossState: OrderState,
        newStatus: OrderStatus = OrderStatus.ACCEPTED
    ): ComponentAwareBracketOrderState {
        return if (newStatus === OrderStatus.ACCEPTED && status == OrderStatus.INITIAL) {
            ComponentAwareBracketOrderState(order, entryState, takeProfitState, stopLossState, newStatus, time)
        } else if (status.open && (entryState.status.closed || takeProfitState.status.closed || stopLossState.status.closed)
        ) {
            val openTime = if (openedAt === Instant.MIN) time else openedAt

            return if (takeProfitState.status.closed) {
                ComponentAwareBracketOrderState(
                    order,
                    entryState,
                    takeProfitState,
                    stopLossState,
                    takeProfitState.status,
                    openTime,
                    time
                )
            } else if (stopLossState.status.closed) {
                ComponentAwareBracketOrderState(
                    order,
                    entryState,
                    takeProfitState,
                    stopLossState,
                    stopLossState.status,
                    openTime,
                    time
                )
            } else if (entryState.closed && !entryState.status.completed) {
                ComponentAwareBracketOrderState(
                    order,
                    entryState,
                    takeProfitState,
                    stopLossState,
                    entryState.status,
                    openTime,
                    time
                )
            } else {
                ComponentAwareBracketOrderState(
                    order,
                    entryState,
                    takeProfitState,
                    stopLossState,
                    newStatus,
                    openTime,
                    time
                )
            }
        } else {
            this
        }
    }
}