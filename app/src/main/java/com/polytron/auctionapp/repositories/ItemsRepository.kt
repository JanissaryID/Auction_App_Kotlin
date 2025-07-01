package com.polytron.auctionapp.repositories

import com.polytron.auctionapp.model.Item

interface ItemsRepository {
    suspend fun fetchItems(): List<Item>
    suspend fun fetchItemById(id: String): Item
    suspend fun createItem(bodyObj: Item): Item
    suspend fun updateItem(id: String, bodyObj: Item): Item
    suspend fun deleteItem(id: String): Item
}