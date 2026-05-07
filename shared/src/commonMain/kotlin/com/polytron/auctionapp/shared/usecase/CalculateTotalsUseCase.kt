package com.polytron.auctionapp.shared.usecase

import com.polytron.auctionapp.shared.model.SharedItem
import com.polytron.auctionapp.shared.model.priceAsLong

data class TotalsResult(
    val totalPrice: Long
)

class CalculateTotalsUseCase {
    operator fun invoke(items: List<SharedItem>): TotalsResult {
        return TotalsResult(
            totalPrice = items.sumOf { it.priceAsLong() }
        )
    }
}
