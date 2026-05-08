package com.polytron.auctionapp.domain.usecase.items

import com.polytron.auctionapp.domain.model.ItemResponse
import com.polytron.auctionapp.domain.repository.ItemsRepository

class UpdateItemUseCase(
    private val repository: ItemsRepository
) {
    suspend operator fun invoke(id: String, item: ItemResponse): ItemResponse {
        return repository.updateItem(id = id, item = item)
    }
}
