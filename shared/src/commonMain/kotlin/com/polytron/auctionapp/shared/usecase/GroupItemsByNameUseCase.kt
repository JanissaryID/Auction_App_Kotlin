package com.polytron.auctionapp.shared.usecase

import com.polytron.auctionapp.shared.model.SharedItem
import com.polytron.auctionapp.shared.util.extractBaseName

class GroupItemsByNameUseCase {
    operator fun invoke(items: List<SharedItem>): Map<String, List<SharedItem>> {
        return items.groupBy { it.nameItem.extractBaseName() }
    }
}
