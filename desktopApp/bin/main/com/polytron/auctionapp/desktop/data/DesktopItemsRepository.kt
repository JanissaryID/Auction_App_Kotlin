package com.polytron.auctionapp.desktop.data

import com.polytron.auctionapp.shared.model.SharedItem
import com.polytron.auctionapp.shared.repository.ItemsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Desktop implementation of ItemsRepository
 * Uses in-memory storage for demo purposes
 */
class DesktopItemsRepository : ItemsRepository {
    
    private val itemsState = MutableStateFlow<List<SharedItem>>(emptyList())

    init {
        // Initialize with sample data
        itemsState.value = listOf(
            SharedItem(
                id = "1",
                nameItem = "Sanco - 1",
                codeItem = "A01",
                buyer = "Buyer A",
                price = "150000",
                orderId = "Order-1001",
                status = 1
            ),
            SharedItem(
                id = "2",
                nameItem = "Sanco - 2",
                codeItem = "A02",
                buyer = "Buyer A",
                price = "175000",
                orderId = "Order-1001",
                status = 1
            ),
            SharedItem(
                id = "3",
                nameItem = "Kursi - 1",
                codeItem = "B01",
                buyer = "Buyer B",
                price = "250000",
                orderId = "Order-1002",
                status = 2
            ),
            SharedItem(
                id = "4",
                nameItem = "Kursi - 2",
                codeItem = "B02",
                buyer = "Buyer B",
                price = "280000",
                orderId = "Order-1002",
                status = 2
            ),
            SharedItem(
                id = "5",
                nameItem = "Meja - 1",
                codeItem = "C01",
                buyer = "Buyer C",
                price = "500000",
                orderId = "Order-1003",
                status = 1
            ),
            SharedItem(
                id = "6",
                nameItem = "Lemari - 1",
                codeItem = "D01",
                buyer = "Buyer D",
                price = "750000",
                orderId = "Order-1004",
                status = 3
            ),
            SharedItem(
                id = "7",
                nameItem = "Lemari - 2",
                codeItem = "D02",
                buyer = "Buyer D",
                price = "800000",
                orderId = "Order-1004",
                status = 3
            )
        )
    }

    override fun observeItems(): Flow<List<SharedItem>> = itemsState.asStateFlow()

    override suspend fun refreshItems(page: Int, perPage: Int): List<SharedItem> {
        // In real implementation, this would fetch from API
        // For now, just return current state
        return itemsState.value
    }

    override suspend fun createItem(item: SharedItem): SharedItem {
        val newItem = item.copy(id = generateId())
        itemsState.value = itemsState.value + newItem
        return newItem
    }

    override suspend fun updateItem(id: String, item: SharedItem): SharedItem {
        itemsState.value = itemsState.value.map { 
            if (it.id == id) item.copy(id = id) else it 
        }
        return item.copy(id = id)
    }

    override suspend fun deleteItem(id: String) {
        itemsState.value = itemsState.value.filterNot { it.id == id }
    }

    override suspend fun updateItemStatus(id: String, status: Int) {
        itemsState.value = itemsState.value.map { 
            if (it.id == id) it.copy(status = status) else it 
        }
    }

    private fun generateId(): String {
        val maxId = itemsState.value.mapNotNull { it.id.toIntOrNull() }.maxOrNull() ?: 0
        return (maxId + 1).toString()
    }
}
