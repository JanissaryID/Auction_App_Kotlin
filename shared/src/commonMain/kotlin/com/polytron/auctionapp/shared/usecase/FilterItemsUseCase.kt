package com.polytron.auctionapp.shared.usecase

import com.polytron.auctionapp.shared.model.SharedItem
import com.polytron.auctionapp.shared.util.StringUtils

class FilterItemsUseCase {
    operator fun invoke(items: List<SharedItem>, query: String): List<SharedItem> {
        if (query.isBlank()) return items

        return items.filter { item ->
            StringUtils.containsIgnoreCase(item.nameItem, query) ||
            StringUtils.containsIgnoreCase(item.codeItem, query) ||
            StringUtils.containsIgnoreCase(item.buyer, query) ||
            StringUtils.containsIgnoreCase(item.orderId, query)
        }
    }
}
