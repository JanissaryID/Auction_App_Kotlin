package com.polytron.auctionapp.shared.repository

import com.polytron.auctionapp.shared.model.SharedItem
import kotlinx.coroutines.flow.Flow

interface ItemsRepository {
    fun observeItems(): Flow<List<SharedItem>>
    suspend fun refreshItems(page: Int = 1, perPage: Int = 500): List<SharedItem>
    suspend fun createItem(item: SharedItem): SharedItem
    suspend fun updateItem(id: String, item: SharedItem): SharedItem
    suspend fun deleteItem(id: String)
    suspend fun updateItemStatus(id: String, status: Int)
}
