package com.polytron.auctionapp.domain.usecase.items

import com.polytron.auctionapp.domain.repository.ItemsRepository

class DeleteItemUseCase(
    private val repository: ItemsRepository
) {
    suspend operator fun invoke(id: String) {
        repository.deleteItem(id)
    }
}
