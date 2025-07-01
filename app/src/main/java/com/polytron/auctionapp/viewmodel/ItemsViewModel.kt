package com.polytron.auctionapp.viewmodel

import android.util.Log
import com.polytron.auctionapp.data.api.KtorClient
import com.polytron.auctionapp.data.api.ItemApiService
import com.polytron.auctionapp.model.Item
import com.polytron.auctionapp.repositories.ItemsRepositoryImpl
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ItemsViewModel(): ViewModel() {
    private val _items = MutableStateFlow<List<Item>>(emptyList())
    val items = _items.asStateFlow()

    private val token = "51772c72ff72e7a142e7aa26a7178c6c"
    private val baseUrl = "https://api.kontenbase.com/query/api/v1/d11e834d-5663-4415-9bee-cfb371e77a2e"

    private val headers = mapOf("Authorization" to "Bearer $token")

    private val service = ItemApiService(
        client = KtorClient.httpClient,
        baseUrl = baseUrl
    )

    private val repository = ItemsRepositoryImpl(
        service = service,
        headers = headers
    )

    fun fetchItems() {
        viewModelScope.launch {
            try {
                val fetched = repository.fetchItems().reversed()
                _items.value = fetched
                Log.i("ViewModel", "fetchItems: ${_items.value} items loaded")
            } catch (e: Exception) {
                Log.e("ViewModel", "fetchItems error", e)
            }
        }
    }

    fun createItem(
        nameItem: String,
        codeItem: String,
        basePrice: String,
        maxPrice: String,
        time: String,
        admin: String,
    ) {
        viewModelScope.launch {
            try {
                val item = Item(
                    nameItem = nameItem,
                    codeItem = codeItem,
                    basePrice = basePrice,
                    maxPrice = maxPrice,
                    time = time,
                    admin = admin,
                    status = false,
                    buyer = ""
                )
                val created = repository.createItem(item)
                Log.i("ViewModel", "Success create item: $created")
            } catch (e: Exception) {
                Log.e("ViewModel", "create item error", e)
            }
        }
    }

    fun patchItem(
        id: String,
        item: Item
    ){
        viewModelScope.launch {
            try {
                val patched = repository.updateItem(id, item)
                Log.i("ViewModel", "Success patch item: $patched")
            } catch (e: Exception) {
                Log.e("ViewModel", "create item error", e)
            }
        }
    }

    fun deleteItem(id: String) {
        viewModelScope.launch {
            try {
                val deleted = repository.deleteItem(id)
                Log.i("ViewModel", "Success delete item: $deleted")
                fetchItems()
            } catch (e: Exception) {
                Log.e("ViewModel", "delete item error", e)
            }
        }
    }

    fun deleteAllItems() {
        viewModelScope.launch {
            try {
                val fetched = repository.fetchItems()
                fetched.forEach {
                    repository.deleteItem(it.id!!)
                    Log.i("ViewModel", "Success delete item: $it")
                }
                fetchItems()
            } catch (e: Exception) {
                Log.e("ViewModel", "delete item error", e)
            }
        }
    }
}