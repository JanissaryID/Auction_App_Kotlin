package com.polytron.auctionapp.viewmodel

import android.util.Log
import com.polytron.auctionapp.data.api.ItemApiService
import com.polytron.auctionapp.data.api.KtorClient
import com.polytron.auctionapp.model.ItemResponse
import com.polytron.auctionapp.repositories.ItemsRepositoryImpl
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ItemsViewModel() : ViewModel() {

    private val token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJjb2xsZWN0aW9uSWQiOiJwYmNfMzE0MjYzNTgyMyIsImV4cCI6MTc1MjU2NzY4MSwiaWQiOiJjOGljanlnNGJja3E3aGgiLCJyZWZyZXNoYWJsZSI6ZmFsc2UsInR5cGUiOiJhdXRoIn0.QUdPV-WDASNYn7GsTnflApliXvhugEsvc_MVH8A4EbM"
    private val baseUrl = "https://mono-mac-terminology-ridge.trycloudflare.com/api/collections"

    private val headers = mapOf("Authorization" to "Bearer $token")
    private val _items = MutableStateFlow<List<ItemResponse>>(emptyList())
    val items = _items.asStateFlow()

    private val _selectedItems = MutableStateFlow<List<ItemResponse>>(emptyList())
    val selectedItems = _selectedItems.asStateFlow()
    fun setSelectedItems(items: List<ItemResponse>) { _selectedItems.value = items }

    fun removeSelectedItem(item: ItemResponse) {
        _selectedItems.value = _selectedItems.value.filterNot { it.id == item.id }
    }
    fun clearSelectedItems() {
        _selectedItems.value = emptyList()
        _editingBuyers.value = emptyMap()
        _editingPrices.value = emptyMap()
    }

    private val _editingBuyers = MutableStateFlow<Map<String, String>>(emptyMap())
    val editingBuyers = _editingBuyers.asStateFlow()

    private val _editingPrices = MutableStateFlow<Map<String, String>>(emptyMap())
    val editingPrices = _editingPrices.asStateFlow()

    fun updateEditingBuyer(itemId: String, name: String) {
        _editingBuyers.value = _editingBuyers.value.toMutableMap().apply {
            put(itemId, name)
        }
    }

    fun clearEditingForItem(itemId: String) {
        _editingBuyers.value = _editingBuyers.value.toMutableMap().apply { remove(itemId) }
        _editingPrices.value = _editingPrices.value.toMutableMap().apply { remove(itemId) }
    }

    fun updateEditingPrice(itemId: String, price: String) {
        val clean = price.filter { it.isDigit() }
        _editingPrices.value = _editingPrices.value.toMutableMap().apply {
            put(itemId, clean)
        }
    }

    fun updateSelectedItem(updatedItem: ItemResponse) {
        _selectedItems.value = _selectedItems.value.map {
            if (it.id == updatedItem.id) updatedItem else it
        }
    }

    private val service = ItemApiService(
        client = KtorClient.httpClient,
        baseUrl = baseUrl
    )

    private val repository = ItemsRepositoryImpl(service = service, headers = headers)

    fun fetchItems() {
        viewModelScope.launch {
            try {
                val fetched = repository.fetchItems().items?.reversed()
                if (fetched != null) {
                    _items.value = fetched
                }
                Log.i("ViewModel", "fetchItems: ${_items.value.size} items loaded")
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
        orderID: String,
        admin: String,
    ) {
        viewModelScope.launch {
            try {
                val item = ItemResponse(
                    nameItem = nameItem,
                    codeItem = codeItem,
                    basePrice = basePrice,
                    maxPrice = maxPrice,
                    orderID = orderID,
                    admin = admin,
                    status = 0,
                    buyer = "",
                    price = "",
                    typePayment = ""
                )
                val created = repository.createItem(item)
                Log.i("ViewModel", "Success create item: $created")
            } catch (e: Exception) {
                Log.e("ViewModel", "create item error", e)
            }
        }
    }

    fun patchItem(id: String, item: ItemResponse) {
        viewModelScope.launch {
            try {
                repository.updateItem(id, item)
                Log.i("ViewModel", "Success patch item: $item")
            } catch (e: Exception) {
                Log.e("ViewModel", "patchItem error", e)
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
                val fetched = repository.fetchItems().items
                fetched?.forEach {
                    repository.deleteItem(it.id!!)
                    Log.i("ViewModel", "Success delete item: $it")
                }
                fetchItems()
            } catch (e: Exception) {
                Log.e("ViewModel", "delete all items error", e)
            }
        }
    }
}
