package com.polytron.auctionapp.domain.usecase.items

import com.polytron.auctionapp.domain.model.ItemResponse
import com.polytron.auctionapp.domain.repository.ItemsRepository

class FetchItemsUseCase(
    private val repository: ItemsRepository
) {
    suspend operator fun invoke(page: Int = 1, perPage: Int = 500): List<ItemResponse> {
        return repository.getItems(page = page, perPage = perPage)
    }
}
