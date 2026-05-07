package com.polytron.auctionapp.desktop.data

import com.polytron.auctionapp.desktop.data.remote.ItemResponse
import com.polytron.auctionapp.desktop.data.remote.PocketBaseRepository
import com.polytron.auctionapp.desktop.data.session.DesktopSessionManager
import com.polytron.auctionapp.shared.model.SharedItem
import com.polytron.auctionapp.shared.repository.ItemsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Adapter that implements the shared ItemsRepository interface
 * but delegates to PocketBaseRepository for real API calls.
 * Converts between ItemResponse (PocketBase) ↔ SharedItem (shared module).
 */
class DesktopRemoteItemsRepository(
    private val pocketBaseRepository: PocketBaseRepository,
    private val sessionManager: DesktopSessionManager
) : ItemsRepository {

    private val itemsState = MutableStateFlow<List<SharedItem>>(emptyList())

    override fun observeItems(): Flow<List<SharedItem>> = itemsState.asStateFlow()

    override suspend fun refreshItems(page: Int, perPage: Int): List<SharedItem> {
        val remoteItems = pocketBaseRepository.getItems(page, perPage)
        val sharedItems = remoteItems.map { it.toSharedItem() }
        itemsState.value = sharedItems
        return sharedItems
    }

    override suspend fun createItem(item: SharedItem): SharedItem {
        val response = pocketBaseRepository.createItem(item.toItemResponse(requireCurrentUserId()))
        refreshItems() // Refresh list after create
        return response.toSharedItem()
    }

    override suspend fun updateItem(id: String, item: SharedItem): SharedItem {
        val response = pocketBaseRepository.updateItem(id, item.toItemResponse(requireCurrentUserId()))
        refreshItems() // Refresh list after update
        return response.toSharedItem()
    }

    override suspend fun deleteItem(id: String) {
        pocketBaseRepository.deleteItem(id)
        refreshItems() // Refresh list after delete
    }

    override suspend fun updateItemStatus(id: String, status: Int) {
        // Find existing item, update status only
        val existingItem = itemsState.value.find { it.id == id } ?: return
        val updateResponse = ItemResponse(
            orderID = existingItem.orderId,
            admin = "admin",
            nameItem = existingItem.nameItem,
            buyer = existingItem.buyer,
            price = existingItem.price,
            codeItem = existingItem.codeItem,
            user = requireCurrentUserId(),
            status = status
        )
        pocketBaseRepository.updateItem(id, updateResponse)
        refreshItems() // Refresh list after status update
    }

    // =========================================================================
    // Converters
    // =========================================================================
    private fun ItemResponse.toSharedItem(): SharedItem {
        return SharedItem(
            id = this.id ?: "",
            nameItem = this.nameItem,
            codeItem = this.codeItem,
            buyer = this.buyer,
            price = this.price,
            orderId = this.orderID,
            status = this.status
        )
    }

    private fun SharedItem.toItemResponse(userId: String): ItemResponse {
        return ItemResponse(
            orderID = this.orderId,
            admin = "admin",
            nameItem = this.nameItem,
            buyer = this.buyer,
            price = this.price,
            codeItem = this.codeItem,
            user = userId,
            status = this.status
        )
    }

    private fun requireCurrentUserId(): String {
        return sessionManager.getUserId()?.takeIf { it.isNotBlank() }
            ?: error("Tidak ada user id aktif. Silakan login ulang.")
    }
}
