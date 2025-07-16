package com.polytron.auctionapp.repositories

import com.polytron.auctionapp.data.api.ItemApiService
import com.polytron.auctionapp.model.ItemResponse
import com.polytron.auctionapp.model.Items
import com.polytron.auctionapp.model.UserRequest
import com.polytron.auctionapp.model.UserResponse


class ItemsRepositoryImpl(
    private val service: ItemApiService
) : ItemsRepository {

    override suspend fun fetchItems(token: String): Items {
        return service.fetchAll(token)
    }

    override suspend fun fetchItemById(id: String, token: String): ItemResponse {
        return service.getById(token, id)
    }

    override suspend fun createItem(bodyObj: ItemResponse, token: String): ItemResponse {
        return service.create(token, bodyObj)
    }

    override suspend fun updateItem(id: String, bodyObj: ItemResponse, token: String): ItemResponse {
        return service.update(token, id, bodyObj)
    }

    override suspend fun deleteItem(id: String, token: String): Boolean {
        return service.delete(token, id)
    }

    override suspend fun login(bodyObj: UserRequest): UserResponse {
        return service.login(bodyObj)
    }
}
