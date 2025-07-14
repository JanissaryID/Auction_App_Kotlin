package com.polytron.auctionapp.repositories

import com.polytron.auctionapp.data.api.ItemApiService
import com.polytron.auctionapp.model.ItemRequest
import com.polytron.auctionapp.model.ItemResponse
import com.polytron.auctionapp.model.Items


class ItemsRepositoryImpl(
    private val service: ItemApiService,
    private val headers: Map<String, String>
) : ItemsRepository {

    override suspend fun fetchItems(): Items {
        return service.fetchAll(headers = headers)
    }

    override suspend fun fetchItemById(id: String): ItemResponse {
        return service.getById(headers = headers, id = id)
    }

    override suspend fun createItem(bodyObj: ItemRequest): ItemResponse {
        return service.create(headers = headers, bodyObj = bodyObj)
    }

    override suspend fun updateItem(id: String, bodyObj: ItemRequest): ItemResponse {
        return service.update(headers = headers, id = id, bodyObj = bodyObj)
    }

    override suspend fun deleteItem(id: String): ItemResponse {
        return service.delete(headers = headers, id = id)
    }
}