package com.polytron.auctionapp.data.shared

import com.polytron.auctionapp.data.remote.repository.ItemsRepository as RemoteItemsRepository
import com.polytron.auctionapp.model.toItemResponse
import com.polytron.auctionapp.model.toSharedItem
import com.polytron.auctionapp.shared.model.SharedItem
import com.polytron.auctionapp.shared.repository.ItemsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AndroidSharedItemsRepository(
    private val remoteRepository: RemoteItemsRepository
) : ItemsRepository {

    private val itemsState = MutableStateFlow<List<SharedItem>>(emptyList())

    override fun observeItems(): Flow<List<SharedItem>> = itemsState.asStateFlow()

    override suspend fun refreshItems(page: Int, perPage: Int): List<SharedItem> {
        val mapped = remoteRepository.getItems(page, perPage).map { it.toSharedItem() }
        itemsState.value = mapped
        return mapped
    }

    override suspend fun createItem(item: SharedItem): SharedItem {
        val created = remoteRepository.createItem(item.toItemResponse()).toSharedItem()
        itemsState.value = itemsState.value + created
        return created
    }

    override suspend fun updateItem(id: String, item: SharedItem): SharedItem {
        val updated = remoteRepository.updateItem(id, item.toItemResponse()).toSharedItem()
        itemsState.value = itemsState.value.map { if (it.id == id) updated else it }
        return updated
    }

    override suspend fun deleteItem(id: String) {
        remoteRepository.deleteItem(id)
        itemsState.value = itemsState.value.filterNot { it.id == id }
    }

    override suspend fun updateItemStatus(id: String, status: Int) {
        val existing = itemsState.value.firstOrNull { it.id == id } ?: refreshItems().firstOrNull { it.id == id }
        if (existing != null) {
            updateItem(id, existing.copy(status = status))
        }
    }
}
