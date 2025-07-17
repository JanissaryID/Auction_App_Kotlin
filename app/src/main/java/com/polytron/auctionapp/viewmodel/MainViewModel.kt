package com.polytron.auctionapp.viewmodel

import android.util.Log
import com.polytron.auctionapp.data.datastore.UserPreferences
import com.polytron.auctionapp.model.ItemResponse
import com.polytron.auctionapp.model.UserRequest
import com.polytron.auctionapp.repositories.ItemsRepository
import com.yourapp.pocketbase.PocketBaseClient
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonPrimitive

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

    private val _id_user = MutableStateFlow<String?>(null)
    val idUser: StateFlow<String?> = _id_user

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

    val isLoading = MutableStateFlow(false)
    val errorMessage = MutableStateFlow<String?>(null)

    private val pb = PocketBaseClient("https://gzip-hanging-immigration-prospect.trycloudflare.com")
    private val itemCollection = pb.collection("Items")

    private var sseJob: Job? = null

    init {
        viewModelScope.launch {
            userPreferences.userEmail.collectLatest { _email.value = it.orEmpty() }
        }
        viewModelScope.launch {
            userPreferences.userPassword.collectLatest { _password.value = it.orEmpty() }
        }
        viewModelScope.launch {
            userPreferences.userIdUser.collectLatest { _id_user.value = it.orEmpty() }
        }
        viewModelScope.launch {
            userPreferences.userToken.collectLatest {
                _token.value = it
                val loggedIn = !it.isNullOrEmpty()
                _isLoggedIn.value = loggedIn
                _showSuccessLogin.value = loggedIn

                // ✅ Jika token valid, fetch items
//                Log.i("MainViewModel", "Token: $it")
                if (loggedIn) {
                    fetchItems()
                    subscribeRealtimeItems(_token.value.orEmpty())
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

    fun login(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // Set loading state to true
                isLoading.value = true
                errorMessage.value = null

                // Login request
                val user = UserRequest(_email.value, _password.value)
                val login = repository.login(user)

                val tokenValue = login.token.orEmpty()
                val idUser = login.record?.id.orEmpty()

                // Update state setelah berhasil login
                _token.value = tokenValue
                _isLoggedIn.value = true
                _showSuccessLogin.value = true
                _id_user.value = idUser

                // Save to preferences
                userPreferences.saveLogin(_email.value, _password.value, tokenValue, idUser)

                // Call onSuccess callback
                onSuccess()

            } catch (e: Exception) {
                // Handle error
//                Log.e("MainViewModel", "Login failed", e)

                // Set error state
                errorMessage.value = "Login failed: ${e.localizedMessage}"

                // Call onError callback with error message
                onError(errorMessage.value ?: "Unknown error")
            } finally {
                // Set loading state to false after login attempt
                isLoading.value = false
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
//                Log.e("MainViewModel", "Item = $item")
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

    fun subscribeRealtimeItems(token: String) {
        // Pastikan streaming berjalan di IO
        val streamScope = viewModelScope + Dispatchers.IO

        itemCollection.subscribe(
            token = token,
            scope = streamScope
        ) { event ->
            // event.record bertipe JsonObject (non-null) di versi lib kita
            val record = event.record
            val id = record?.get("id")?.jsonPrimitive?.content ?: return@subscribe

            println("Realtime event: ${event.action} (id=$id)")

            when (event.action) {
                "create" -> decodeItem(record)?.let { newItem ->
                    _items.update { old ->
                        if (old.any { it.id == id }) {
                            // Upsert jika sudah ada (mis. reconnect)
                            old.map { if (it.id == id) newItem else it }
                        } else {
                            // Append di akhir; ganti ke `listOf(newItem) + old` jika mau muncul di atas
                            old + newItem
                        }
                    }
                }

                "update" -> decodeItem(record)?.let { updatedItem ->
                    _items.update { old ->
                        old.map { if (it.id == id) updatedItem else it }
                    }
                }

                "delete" -> {
                    _items.update { old -> old.filterNot { it.id == id } }
                }

                else -> {
//                    println("Else")
                    // Event lain (PB_CONNECT, custom, dsb) diabaikan
                     println("Ignored event: ${event.action}")
                }
            }
        }
    }


    fun decodeItem(json: JsonObject?): ItemResponse? {
        return json?.let { Json.decodeFromJsonElement(it) }
    }

    override fun onCleared() {
        super.onCleared()
        itemCollection.unsubscribe()
    }
}