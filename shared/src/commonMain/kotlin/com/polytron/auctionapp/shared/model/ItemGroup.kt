package com.polytron.auctionapp.shared.model

/**
 * Model untuk grouping items by name
 */
data class ItemGroup(
    val name: String,
    val items: List<SharedItem>,
    val itemCount: Int = items.size,
    val totalBasePrice: Long = 0L,
    val totalMaxPrice: Long = 0L
)

/**
 * Extension untuk convert Map<String, List<SharedItem>> ke List<ItemGroup>
 */
fun Map<String, List<SharedItem>>.toItemGroups(): List<ItemGroup> {
    return this.map { (name, items) ->
        ItemGroup(
            name = name,
            items = items
        )
    }.sortedBy { it.name }
}
