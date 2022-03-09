package org.roboquant.samples

import org.roboquant.Roboquant
import org.roboquant.brokers.sim.ExecutionEngine
import org.roboquant.feeds.csv.CSVFeed
import org.roboquant.metrics.AccountSummary
import org.roboquant.orders.MarketOrder
import org.roboquant.orders.Order
import org.roboquant.orders.StopOrder
import org.roboquant.orders.TrailOrder
import org.roboquant.policies.DefaultPolicy
import org.roboquant.samples.orders.ComponentAwareBracketOrder
import org.roboquant.samples.orders.ComponentAwareBracketOrderHandler
import org.roboquant.strategies.EMACrossover
import org.roboquant.strategies.Signal

class SamplePolicy(private val percentage:Double = 0.05) : DefaultPolicy() {

    override fun createOrder(signal: Signal, qty: Double, price: Double): Order? {
        // We don't short and  all other sell/exit orders are covered by the bracket order
        if (qty < 0) return null

        val asset = signal.asset
        return ComponentAwareBracketOrder(
                MarketOrder(asset, qty),
                TrailOrder(asset, -qty, percentage/2.0),
                StopOrder(asset, -qty, price* (1 - percentage))
        )
    }
}

fun main() {
    val strategy = EMACrossover() // (1)
    val metric = AccountSummary() // (2)
    val policy = SamplePolicy()
    ExecutionEngine.register<ComponentAwareBracketOrder> { ComponentAwareBracketOrderHandler(it) }
    val roboquant = Roboquant(strategy, metric, policy = policy) // (3)

    val feed = CSVFeed("data/US") // (4)
    roboquant.run(feed)

    roboquant.broker.account.fullSummary().print()
}
