package com.polytron.auctionapp.repositories

import com.polytron.auctionapp.model.ItemResponse
import com.polytron.auctionapp.model.Items
import com.polytron.auctionapp.model.UserRequest
import com.polytron.auctionapp.model.UserResponse

interface ItemsRepository {
    suspend fun fetchItems(token: String): Items
    suspend fun fetchItemById(id: String, token: String): ItemResponse
    suspend fun createItem(bodyObj: ItemResponse, token: String): ItemResponse
    suspend fun updateItem(id: String, bodyObj: ItemResponse, token: String): ItemResponse
    suspend fun deleteItem(id: String, token: String): ItemResponse
    suspend fun login(bodyObj: UserRequest): UserResponse
}
