package com.polytron.auctionapp.domain.usecase.realtime

import com.polytron.auctionapp.domain.repository.ItemsRepository

class SubscribeItemsRealtimeUseCase(
    private val repository: ItemsRepository
) {
    suspend operator fun invoke(clientId: String, collections: List<String>): Boolean {
        return repository.subscribeRealtime(clientId = clientId, collections = collections)
    }
}
