package com.polytron.auctionapp.shared.model

/**
 * Model untuk grouping items by order ID
 */
data class OrderGroup(
    val orderId: String,
    val items: List<SharedItem>,
    val totalPrice: Long = items.sumOf { it.priceAsLong() },
    val itemCount: Int = items.size,
    val buyer: String? = items.firstOrNull()?.buyer,
    val status: ItemStatus? = items.firstOrNull()?.getStatus()
)

/**
 * Extension untuk convert Map<String, List<SharedItem>> ke List<OrderGroup>
 */
fun Map<String, List<SharedItem>>.toOrderGroups(): List<OrderGroup> {
    return this.map { (orderId, items) ->
        OrderGroup(
            orderId = orderId,
            items = items
        )
    }.sortedBy { it.orderId }
}
