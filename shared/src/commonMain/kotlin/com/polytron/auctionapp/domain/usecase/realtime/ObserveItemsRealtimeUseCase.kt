package com.polytron.auctionapp.domain.usecase.realtime

import com.polytron.auctionapp.domain.model.RealtimeSse
import com.polytron.auctionapp.domain.repository.ItemsRepository

class ObserveItemsRealtimeUseCase(
    private val repository: ItemsRepository
) {
    suspend operator fun invoke(onEvent: suspend (RealtimeSse) -> Unit) {
        repository.withRealtimeEvents(onEvent)
    }
}
