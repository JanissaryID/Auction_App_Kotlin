package com.polytron.auctionapp.shared.usecase

import com.polytron.auctionapp.shared.model.SharedItem

class GroupItemsByOrderUseCase {
    operator fun invoke(items: List<SharedItem>): Map<String, List<SharedItem>> {
        return items.groupBy { it.orderId.orEmpty().ifBlank { "Tanpa Order ID" } }
    }
}
