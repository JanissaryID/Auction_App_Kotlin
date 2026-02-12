package com.polytron.auctionapp.data.remote.viewmodel

import android.bluetooth.BluetoothDevice
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.polytron.auctionapp.data.local.repository.UserPreferencesRepository
import com.polytron.auctionapp.data.remote.model.RealtimeSse
import com.polytron.auctionapp.data.remote.repository.ItemsRepository
import com.polytron.auctionapp.model.ItemResponse
import com.polytron.auctionapp.model.RealtimeEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject

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

    // State untuk Nama/Username yang akan ditampilkan di UI
    private val _userName = MutableStateFlow<String?>(null)
    val userName: StateFlow<String?> = _userName

    private val _avatarFileName = MutableStateFlow<String?>(null)
    val avatarFileName: StateFlow<String?> = _avatarFileName

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    private val _showSuccessLogin = MutableStateFlow(false)

    // =======================
    // Bluetooth & printer
    // =======================
    private val _isBluetoothConnected = MutableStateFlow(false)
    val isBluetoothConnected: StateFlow<Boolean> = _isBluetoothConnected

    private val _showBluetoothDevice = MutableStateFlow(false)
    val showBluetoothDevice: StateFlow<Boolean> = _showBluetoothDevice

    private val _selectedPrinter = MutableStateFlow<BluetoothDevice?>(null)
    val selectedPrinter: StateFlow<BluetoothDevice?> = _selectedPrinter

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent = _toastEvent.asSharedFlow()

    fun setSelectedPrinter(device: BluetoothDevice) {
        _selectedPrinter.value = device
        _isBluetoothConnected.value = true
    }

    fun showBluetoothDevice(stat: Boolean) {
        _showBluetoothDevice.value = stat
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
    private val _sseId = MutableStateFlow<String?>(null)
    private var sseJob: Job? = null

    private val COLLECTION_ITEMS = "Items"

    init {
        viewModelScope.launch {
            userPreferencesRepository.userEmail.collectLatest { _email.value = it.orEmpty() }
        }
        viewModelScope.launch {
            userPreferencesRepository.userPassword.collectLatest { _password.value = it.orEmpty() }
        }
        viewModelScope.launch {
            userPreferencesRepository.userIdUser.collectLatest { _idUser.value = it.orEmpty() }
        }
        // Collect Nama User dari DataStore
        viewModelScope.launch {
            userPreferencesRepository.userName.collectLatest { _userName.value = it }
        }

        viewModelScope.launch {
            userPreferencesRepository.userAvatar.collectLatest { _avatarFileName.value = it }
        }

        viewModelScope.launch {
            userPreferencesRepository.userToken.collectLatest { savedToken ->
                _token.value = savedToken
                val loggedIn = !savedToken.isNullOrEmpty()
                _isLoggedIn.value = loggedIn
                _showSuccessLogin.value = loggedIn

                if (loggedIn) {
                    itemsRepository.loginWithToken(savedToken)
                    fetchItems()
                    startRealtimeItems()
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

                // Repository sekarang sudah melakukan fetch 'getOne' di dalamnya
                val auth = itemsRepository.loginWithEmailPassword(_email.value, _password.value)

                _token.value = auth.token
                _idUser.value = auth.userId
                _userName.value = auth.name
                _avatarFileName.value = auth.avatar
                _isLoggedIn.value = true
                _showSuccessLogin.value = true

                // Simpan ke DataStore
                userPreferencesRepository.saveLogin(
                    email = _email.value,
                    password = _password.value,
                    token = auth.token,
                    idUser = auth.userId,
                    name = auth.name ?: "Unknown",
                    avatar = auth.avatar
                )

                itemsRepository.loginWithToken(auth.token)

                // Trigger fetch data awal setelah login berhasil
                fetchItems()
                startRealtimeItems()

                _toastEvent.emit("Berhasil Login, ${auth.name}!")

                onSuccess()
            } catch (e: Exception) {
                Log.i("LOGIN", "loginWithEmailPassword: $e")
                errorMessage.value = "Login gagal: ${e.localizedMessage}"
                _toastEvent.emit("Login gagal")
                onError(errorMessage.value!!)
            } finally {
                isLoading.value = false
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            userPreferencesRepository.clearLogin() // Pastikan fungsi ini ada di repo
            _token.value = null
            _idUser.value = null
            _userName.value = null
            _isLoggedIn.value = false
            _items.value = emptyList()
            sseJob?.cancel()
            _toastEvent.emit("Berhasil Logout")
        }
    }

    // =======================
    // CRUD Items
    // =======================
    fun fetchItems() {
        viewModelScope.launch {
            try {
                _items.value = itemsRepository.getItems(page = 1, perPage = 500)
            } catch (_: Exception) {}
        }
    }

    fun createItem(item: ItemResponse) {
        viewModelScope.launch {
            try { itemsRepository.createItem(item) } catch (_: Exception) {}
        }
    }

    fun patchItem(id: String, item: ItemResponse) {
        viewModelScope.launch {
            try { itemsRepository.updateItem(id, item) } catch (_: Exception) {}
        }
    }

    fun deleteItem(id: String) {
        viewModelScope.launch {
            try { itemsRepository.deleteItem(id) } catch (_: Exception) {}
        }
    }

    // =======================
    // Selection & editing
    // =======================
    fun setSelectedItems(items: List<ItemResponse>) { _selectedItems.value = items }

    fun addSelectedItem(item: ItemResponse) {
        if (!_selectedItems.value.any { it.id == item.id }) {
            _selectedItems.value = _selectedItems.value + item
        }
    }

    fun removeSelectedItem(item: ItemResponse) {
        _selectedItems.value = _selectedItems.value.filter { it.id != item.id }
        // Bersihkan juga data editannya jika item dihapus
        clearEditingForItem(item.id ?: "")
    }

    fun clearSelectedItems() {
        _selectedItems.value = emptyList()
        _editingBuyers.value = emptyMap()
        _editingPrices.value = emptyMap()
    }

    fun updateEditingBuyer(itemId: String, name: String) {
        val current = _editingBuyers.value.toMutableMap()
        current[itemId] = name
        _editingBuyers.value = current
    }

    fun updateEditingPrice(itemId: String, price: String) {
        val current = _editingPrices.value.toMutableMap()
        val clean = price.filter { it.isDigit() }
        current[itemId] = clean
        _editingPrices.value = current
    }

    fun clearEditingForItem(itemId: String) {
        val buyers = _editingBuyers.value.toMutableMap()
        val prices = _editingPrices.value.toMutableMap()
        buyers.remove(itemId)
        prices.remove(itemId)
        _editingBuyers.value = buyers
        _editingPrices.value = prices
    }

    // =======================
    // Realtime (SSE)
    // =======================
    fun startRealtimeItems() {
        sseJob?.cancel()
        sseJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                _sseConnected.value = false
                itemsRepository.withRealtimeEvents { evt: RealtimeSse ->
                    evt.id?.let { id ->
                        if (_sseId.value != id) {
                            _sseId.value = id
                            viewModelScope.launch {
                                itemsRepository.subscribeRealtime(id, listOf(COLLECTION_ITEMS))
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
                    "create", "update", "delete" -> fetchItems()
                    else -> Unit
                }
            }
        } catch (_: Exception) {}
    }
}