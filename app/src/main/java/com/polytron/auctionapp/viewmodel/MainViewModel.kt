package com.polytron.auctionapp.viewmodel

import android.util.Log
import com.polytron.auctionapp.data.api.ItemApiService
import com.polytron.auctionapp.data.api.KtorClient
import com.polytron.auctionapp.data.datastore.UserPreferences
import com.polytron.auctionapp.model.ItemResponse
import com.polytron.auctionapp.model.UserRequest
import com.polytron.auctionapp.repositories.ItemsRepository
import com.polytron.auctionapp.repositories.ItemsRepositoryImpl
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainViewModel(
    private val userPreferences: UserPreferences,
    private val repository: ItemsRepository
) : ViewModel() {

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    private val _token = MutableStateFlow<String?>(null)
    val token: StateFlow<String?> = _token

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    private val _isBluetoothConnected = MutableStateFlow(false)
    val isBluetoothConnected: StateFlow<Boolean> = _isBluetoothConnected

    private val _showSuccessLogin = MutableStateFlow(false)
    val showSuccessLogin: StateFlow<Boolean> = _showSuccessLogin

    private val _items = MutableStateFlow<List<ItemResponse>>(emptyList())
    val items: StateFlow<List<ItemResponse>> = _items

    private val _selectedItems = MutableStateFlow<List<ItemResponse>>(emptyList())
    val selectedItems: StateFlow<List<ItemResponse>> = _selectedItems

    private val _editingBuyers = MutableStateFlow<Map<String, String>>(emptyMap())
    val editingBuyers: StateFlow<Map<String, String>> = _editingBuyers

    private val _editingPrices = MutableStateFlow<Map<String, String>>(emptyMap())
    val editingPrices: StateFlow<Map<String, String>> = _editingPrices

    init {
        viewModelScope.launch {
            userPreferences.userEmail.collectLatest { _email.value = it.orEmpty() }
        }
        viewModelScope.launch {
            userPreferences.userPassword.collectLatest { _password.value = it.orEmpty() }
        }
        viewModelScope.launch {
            userPreferences.userToken.collectLatest {
                _token.value = it
                val loggedIn = !it.isNullOrEmpty()
                _isLoggedIn.value = loggedIn
                _showSuccessLogin.value = loggedIn

                // ✅ Jika token valid, fetch items
                if (loggedIn) {
                    fetchItems()
                }
            }
        }
    }

    fun onEmailChange(value: String) {
        _email.value = value
    }

    fun onPasswordChange(value: String) {
        _password.value = value
    }

    fun toggleBluetooth() {
        _isBluetoothConnected.value = !_isBluetoothConnected.value
    }

    fun login() {
        viewModelScope.launch {
            try {
                val user = UserRequest(_email.value, _password.value)
                val login = repository.login(user)
                val tokenValue = login.token.orEmpty()
                _token.value = tokenValue
                _isLoggedIn.value = true
                _showSuccessLogin.value = true
                userPreferences.saveLogin(_email.value, _password.value, tokenValue)
            } catch (e: Exception) {
                Log.e("MainViewModel", "Login failed", e)
            }
        }
    }

    fun fetchItems() {
        viewModelScope.launch {
            val token = _token.value
            if (token.isNullOrBlank()) {
                Log.w("MainViewModel", "fetchItems skipped: token is null or blank")
                return@launch
            }

            try {
//                Log.d("MainViewModel", "Fetching items with token: $token")
                val fetched = repository.fetchItems(token).items?.reversed()
                _items.value = fetched ?: emptyList()
            } catch (e: Exception) {
                Log.e("MainViewModel", "Fetch items failed", e)
            }
        }
    }

    fun createItem(item: ItemResponse) {
        viewModelScope.launch {
            try {
                repository.createItem(item, _token.value.orEmpty())
                fetchItems()
            } catch (e: Exception) {
                Log.e("MainViewModel", "Create item failed", e)
            }
        }
    }

    fun patchItem(id: String, item: ItemResponse) {
        viewModelScope.launch {
            try {
                repository.updateItem(id, item, _token.value.orEmpty())
                fetchItems()
            } catch (e: Exception) {
                Log.e("MainViewModel", "Patch item failed", e)
            }
        }
    }

    fun deleteItem(id: String) {
        viewModelScope.launch {
            try {
                repository.deleteItem(id, _token.value.orEmpty())
                fetchItems()
            } catch (e: Exception) {
                Log.e("MainViewModel", "Delete item failed", e)
            }
        }
    }

    fun deleteAllItems() {
        viewModelScope.launch {
            try {
                val allItems = repository.fetchItems(_token.value.orEmpty()).items
                allItems?.forEach {
                    repository.deleteItem(it.id!!, _token.value.orEmpty())
                }
                fetchItems()
            } catch (e: Exception) {
                Log.e("MainViewModel", "Delete all items failed", e)
            }
        }
    }

    fun setSelectedItems(items: List<ItemResponse>) {
        _selectedItems.value = items
    }

    fun removeSelectedItem(item: ItemResponse) {
        _selectedItems.value = _selectedItems.value.filterNot { it.id == item.id }
    }

    fun clearSelectedItems() {
        _selectedItems.value = emptyList()
        _editingBuyers.value = emptyMap()
        _editingPrices.value = emptyMap()
    }

    fun updateEditingBuyer(itemId: String, name: String) {
        _editingBuyers.value = _editingBuyers.value.toMutableMap().apply { put(itemId, name) }
    }

    fun updateEditingPrice(itemId: String, price: String) {
        val clean = price.filter { it.isDigit() }
        _editingPrices.value = _editingPrices.value.toMutableMap().apply { put(itemId, clean) }
    }

    fun clearEditingForItem(itemId: String) {
        _editingBuyers.value = _editingBuyers.value.toMutableMap().apply { remove(itemId) }
        _editingPrices.value = _editingPrices.value.toMutableMap().apply { remove(itemId) }
    }

    fun updateSelectedItem(updatedItem: ItemResponse) {
        _selectedItems.value = _selectedItems.value.map {
            if (it.id == updatedItem.id) updatedItem else it
        }
    }
}
