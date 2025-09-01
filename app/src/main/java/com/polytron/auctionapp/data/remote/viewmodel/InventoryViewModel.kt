package com.polytron.auctionapp.data.remote.viewmodel


import android.bluetooth.BluetoothDevice
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.polytron.auctionapp.bluetooth.BluetoothHelper
import com.polytron.auctionapp.data.local.repository.UserPreferencesRepository
import com.polytron.auctionapp.data.remote.model.RealtimeSse
import com.polytron.auctionapp.data.remote.repository.ItemsRepository
import com.polytron.auctionapp.model.ItemResponse
import com.polytron.auctionapp.model.RealtimeEvent
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.*

class InventoryViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val itemsRepository: ItemsRepository
) : ViewModel() {

    // =======================
    // Auth & user info
    // =======================
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    private val _token = MutableStateFlow<String?>(null)
    val token: StateFlow<String?> = _token

    private val _idUser = MutableStateFlow<String?>(null)
    val idUser: StateFlow<String?> = _idUser

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    private val _showSuccessLogin = MutableStateFlow(false)
    val showSuccessLogin: StateFlow<Boolean> = _showSuccessLogin

    // =======================
    // Bluetooth & printer
    // =======================
    private val _isBluetoothConnected = MutableStateFlow(false)
    val isBluetoothConnected: StateFlow<Boolean> = _isBluetoothConnected

    private val _showBluetoothDevice = MutableStateFlow(false)
    val showBluetoothDevice: StateFlow<Boolean> = _showBluetoothDevice

    private val _selectedPrinter = MutableStateFlow<BluetoothDevice?>(null)
    val selectedPrinter: StateFlow<BluetoothDevice?> = _selectedPrinter

    fun setSelectedPrinter(device: BluetoothDevice) {
        _selectedPrinter.value = device
        _isBluetoothConnected.value = true // anggap sudah tersambung
    }

    fun showBluetoothDevice(stat: Boolean) {
        _showBluetoothDevice.value = stat
    }

    fun updateBluetoothStatus(helper: BluetoothHelper) {
        _isBluetoothConnected.value = helper.isBluetoothEnabled()
    }

    fun toggleBluetooth() {
        _isBluetoothConnected.value = !_isBluetoothConnected.value
    }

    // =======================
    // Items & selection
    // =======================
    private val _items = MutableStateFlow<List<ItemResponse>>(emptyList())
    val items: StateFlow<List<ItemResponse>> = _items

    private val _selectedItems = MutableStateFlow<List<ItemResponse>>(emptyList())
    val selectedItems: StateFlow<List<ItemResponse>> = _selectedItems

    private val _editingBuyers = MutableStateFlow<Map<String, String>>(emptyMap())
    val editingBuyers: StateFlow<Map<String, String>> = _editingBuyers

    private val _editingPrices = MutableStateFlow<Map<String, String>>(emptyMap())
    val editingPrices: StateFlow<Map<String, String>> = _editingPrices

    // =======================
    // UI state
    // =======================
    val isLoading = MutableStateFlow(false)
    val errorMessage = MutableStateFlow<String?>(null)

    // =======================
    // Realtime
    // =======================
    private val _sseConnected = MutableStateFlow(false)
    val sseConnected: StateFlow<Boolean> = _sseConnected

    private val _sseId = MutableStateFlow<String?>(null)
    val sseId: StateFlow<String?> = _sseId

    private var sseJob: Job? = null

    private val COLLECTION_ITEMS = "Items"

    init {
        // Hanya ViewModel yang menjalankan coroutine
        viewModelScope.launch {
            userPreferencesRepository.userEmail.collectLatest { _email.value = it.orEmpty() }
        }
        viewModelScope.launch {
            userPreferencesRepository.userPassword.collectLatest { _password.value = it.orEmpty() }
        }
        viewModelScope.launch {
            userPreferencesRepository.userIdUser.collectLatest { _idUser.value = it.orEmpty() }
        }
        viewModelScope.launch {
            userPreferencesRepository.userToken.collectLatest { savedToken ->
                _token.value = savedToken
                val loggedIn = !savedToken.isNullOrEmpty()
                _isLoggedIn.value = loggedIn
                _showSuccessLogin.value = loggedIn

                if (loggedIn) {
                    // init repo client with token
                    itemsRepository.loginWithToken(savedToken!!)
                    fetchItems()
                    startRealtimeItems() // mulai SSE (connect + subscribe + process)
                }
            }
        }
    }

    // =======================
    // Input handlers
    // =======================
    fun onEmailChange(value: String) { _email.value = value }
    fun onPasswordChange(value: String) { _password.value = value }

    // =======================
    // Auth
    // =======================
    fun login(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null
            try {
                if (_email.value.isBlank() || _password.value.isBlank()) {
                    errorMessage.value = "Email atau password tidak boleh kosong"
                    onError(errorMessage.value!!)
                    return@launch
                }

                val auth = itemsRepository.loginWithEmailPassword(_email.value, _password.value)

                _token.value = auth.token
                _idUser.value = auth.userId
                _isLoggedIn.value = true
                _showSuccessLogin.value = true

                userPreferencesRepository.saveLogin(
                    _email.value,
                    _password.value,
                    auth.token,
                    auth.userId
                )
                itemsRepository.loginWithToken(auth.token)

                onSuccess()
            } catch (e: Exception) {
                errorMessage.value = "Login gagal: ${e.localizedMessage ?: "Unknown error"}"
                onError(errorMessage.value!!)
            } finally {
                isLoading.value = false
            }
        }
    }

    // =======================
    // CRUD Items
    // =======================
    fun fetchItems() {
        viewModelScope.launch {
            try {
                _items.value = itemsRepository.getItems(page = 1, perPage = 500)
            } catch (_: Exception) {
                // swallow/log if needed
            }
        }
    }

    fun createItem(item: ItemResponse) {
        viewModelScope.launch {
            try {
                itemsRepository.createItem(item)
                // Optional: fetchItems()
            } catch (_: Exception) { }
        }
    }

    fun patchItem(id: String, item: ItemResponse) {
        viewModelScope.launch {
            try {
                itemsRepository.updateItem(id, item)
                // Optional: fetchItems()
            } catch (_: Exception) { }
        }
    }

    fun deleteItem(id: String) {
        viewModelScope.launch {
            try {
                itemsRepository.deleteItem(id)
                // Optional: fetchItems()
            } catch (_: Exception) { }
        }
    }

    // =======================
    // Selection & editing
    // =======================
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

    // =======================
    // Realtime (SSE)
    // =======================
    fun startRealtimeItems() {
        // Restart aman
        sseJob?.cancel()

        sseJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                _sseConnected.value = false

                itemsRepository.withRealtimeEvents { evt: RealtimeSse ->
                    evt.id?.let { id ->
                        if (_sseId.value != id) {
                            _sseId.value = id
                            // lakukan subscribe ketika pertama kali dapat id
                            viewModelScope.launch {
                                itemsRepository.subscribeRealtime(
                                    clientId = id,
                                    collections = listOf(COLLECTION_ITEMS)
                                )
                            }
                        }
                        _sseConnected.value = true
                    }
                    handleRealtimeEventPayload(evt.data)
                }
            } catch (_: Exception) {
                _sseConnected.value = false
            }
        }
    }

    private fun handleRealtimeEventPayload(jsonString: String?) {
        if (jsonString.isNullOrBlank()) return
        val json = Json { ignoreUnknownKeys = true }
        try {
            val element = json.parseToJsonElement(jsonString)
            if (element.jsonObject.containsKey("record")) {
                val event = json.decodeFromJsonElement<RealtimeEvent>(element)
                when (event.action) {
                    "create",
                    "update",
                    "delete" -> fetchItems()

                    else -> Unit
                }
            } else {
                // non-record event, abaikan
            }
        } catch (_: Exception) {
            // parsing gagal, abaikan/log
        }
    }
}