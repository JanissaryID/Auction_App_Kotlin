package com.polytron.auctionapp.repositories

import com.polytron.auctionapp.data.api.ItemApiService
import com.polytron.auctionapp.model.Item


class ItemsRepositoryImpl(
    private val service: ItemApiService,
//    private val headers: Map<String, String>
) : ItemsRepository {

    override suspend fun fetchItems(): List<Item> {
        return service.fetchAll()
    }

    override suspend fun fetchItemById(id: String): Item {
        return service.getById(id)
    }

    override suspend fun createItem(bodyObj: Item): Item {
        return service.create(bodyObj)
    }

    override suspend fun updateItem(id: String, bodyObj: Item): Item {
        return service.update(id, bodyObj)
    }

    override suspend fun deleteItem(id: String): Item {
        return service.delete(id)
    }
}