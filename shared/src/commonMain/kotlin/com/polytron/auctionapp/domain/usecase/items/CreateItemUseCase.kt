package com.polytron.auctionapp.domain.usecase.items

import com.polytron.auctionapp.domain.model.ItemResponse
import com.polytron.auctionapp.domain.repository.ItemsRepository

class CreateItemUseCase(
    private val repository: ItemsRepository
) {
    suspend operator fun invoke(item: ItemResponse): ItemResponse {
        return repository.createItem(item)
    }
}
