package com.polytron.auctionapp.repositories

import com.polytron.auctionapp.model.ItemResponse
import com.polytron.auctionapp.model.Items

interface ItemsRepository {
    suspend fun fetchItems(): Items
    suspend fun fetchItemById(id: String): ItemResponse
    suspend fun createItem(bodyObj: ItemResponse): ItemResponse
    suspend fun updateItem(id: String, bodyObj: ItemResponse): ItemResponse
    suspend fun deleteItem(id: String): ItemResponse
}