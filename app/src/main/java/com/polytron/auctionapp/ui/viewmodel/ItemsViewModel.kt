package com.polytron.auctionapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.polytron.auctionapp.core.logging.AppLogger
import com.polytron.auctionapp.domain.model.ItemResponse
import com.polytron.auctionapp.domain.session.SessionManager
import com.polytron.auctionapp.domain.usecase.items.CreateItemUseCase
import com.polytron.auctionapp.domain.usecase.items.DeleteItemUseCase
import com.polytron.auctionapp.domain.usecase.items.FetchItemsUseCase
import com.polytron.auctionapp.domain.usecase.items.UpdateItemUseCase
import com.polytron.auctionapp.domain.usecase.realtime.ObserveItemsRealtimeUseCase
import com.polytron.auctionapp.domain.usecase.realtime.SubscribeItemsRealtimeUseCase
import com.polytron.auctionapp.presentation.items.ItemsViewModel as SharedItemsViewModel

class ItemsViewModel(
    fetchItemsUseCase: FetchItemsUseCase,
    createItemUseCase: CreateItemUseCase,
    updateItemUseCase: UpdateItemUseCase,
    deleteItemUseCase: DeleteItemUseCase,
    observeItemsRealtimeUseCase: ObserveItemsRealtimeUseCase,
    subscribeItemsRealtimeUseCase: SubscribeItemsRealtimeUseCase,
    sessionManager: SessionManager,
    logger: AppLogger
) : ViewModel() {
    private val delegate = SharedItemsViewModel(
        scope = viewModelScope,
        fetchItemsUseCase = fetchItemsUseCase,
        createItemUseCase = createItemUseCase,
        updateItemUseCase = updateItemUseCase,
        deleteItemUseCase = deleteItemUseCase,
        observeItemsRealtimeUseCase = observeItemsRealtimeUseCase,
        subscribeItemsRealtimeUseCase = subscribeItemsRealtimeUseCase,
        sessionManager = sessionManager,
        logger = logger
    )

    val items = delegate.items
    val isLoading = delegate.isLoading

    fun fetchItems() = delegate.fetchItems()
    suspend fun createItem(item: ItemResponse) = delegate.createItem(item)
    suspend fun patchItem(id: String, item: ItemResponse) = delegate.patchItem(id, item)
    suspend fun deleteItem(id: String) = delegate.deleteItem(id)
    fun startRealtimeItems() = delegate.startRealtimeItems()
}
