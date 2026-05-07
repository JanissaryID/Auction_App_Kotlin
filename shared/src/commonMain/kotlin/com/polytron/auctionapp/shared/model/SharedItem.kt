package com.polytron.auctionapp.shared.model

data class SharedItem(
    val id: String,
    val nameItem: String? = null,
    val codeItem: String? = null,
    val buyer: String? = null,
    val price: String? = null,
    val orderId: String? = null,
    val status: Int? = null
)

fun SharedItem.priceAsLong(): Long {
    return price?.replace(Regex("\\D"), "")?.toLongOrNull() ?: 0L
}
