package com.polytron.auctionapp.viewmodel

import android.bluetooth.BluetoothDevice
import com.polytron.auctionapp.data.datastore.UserPreferences
import com.polytron.auctionapp.model.ItemResponse
import com.polytron.auctionapp.model.RealtimeEvent
import com.polytron.auctionapp.utils.BluetoothHelper
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import io.github.agrevster.pocketbaseKotlin.PocketbaseClient
import io.github.agrevster.pocketbaseKotlin.dsl.login
import io.github.agrevster.pocketbaseKotlin.models.AuthRecord
import io.github.agrevster.pocketbaseKotlin.toJsonPrimitive
import io.ktor.client.plugins.sse.sse
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.URLProtocol
import io.ktor.http.contentType
import io.ktor.http.path
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject

class MainViewModel(
    private val userPreferences: UserPreferences,
) : ViewModel() {

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

    val sseIsConencted = MutableStateFlow(false)
    val sseId = MutableStateFlow<String?>(null)

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

    private val client = PocketbaseClient(
        baseUrl = {
            protocol = URLProtocol.HTTPS
            host = "17f4c36f2413.ngrok-free.app"
//            host = "192.168.1.12"
//            port = 8090
        }
    )

    private val collection = "Items"
    private var sseJob: Job? = null

    init {
        viewModelScope.launch {
            userPreferences.userEmail.collectLatest { _email.value = it.orEmpty() }
        }
        viewModelScope.launch {
            userPreferences.userPassword.collectLatest { _password.value = it.orEmpty() }
        }
        viewModelScope.launch {
            userPreferences.userIdUser.collectLatest { _idUser.value = it.orEmpty() }
        }
        viewModelScope.launch {
            userPreferences.userToken.collectLatest {
                _token.value = it
                val loggedIn = !it.isNullOrEmpty()
                _isLoggedIn.value = loggedIn
                _showSuccessLogin.value = loggedIn
                if (loggedIn) {
                    client.login(it)
                    fetchItems()
                    subscribeRealtimeItems()
                }
            }
        }
    }

    fun onEmailChange(value: String) { _email.value = value }
    fun onPasswordChange(value: String) { _password.value = value }
    fun toggleBluetooth() { _isBluetoothConnected.value = !_isBluetoothConnected.value }

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

                val loginResult = client.records.authWithPassword<AuthRecord>(
                    collection = "users",
                    email = _email.value,
                    password = _password.value
                )

                val token = loginResult.token
                val userId = loginResult.record.id.orEmpty()

                _token.value = token
                _idUser.value = userId
                _isLoggedIn.value = true
                _showSuccessLogin.value = true

                userPreferences.saveLogin(_email.value, _password.value, token, userId)
                client.login(token)
                onSuccess()
            } catch (e: Exception) {
                errorMessage.value = "Login gagal: ${e.localizedMessage ?: "Unknown error"}"
                onError(errorMessage.value!!)
            } finally {
                isLoading.value = false
            }
        }
    }

    fun fetchItems() {
        viewModelScope.launch {
            try {
                val fetched = client.records.getList<ItemResponse>(collection, page = 1, perPage = 500)
                _items.value = fetched.items.reversed()
            } catch (e: Exception) {
//                Log.e("MainViewModel", "Fetch items failed", e)
            }
        }
    }

    fun createItem(item: ItemResponse) {
        viewModelScope.launch {
            try {
                val created = client.records.create<ItemResponse>(collection, Json.encodeToString(item))
//                Log.d("MainViewModel", "Item created: $created")
//                fetchItems()
            } catch (e: Exception) {
//                Log.e("MainViewModel", "Create item failed", e)
            }
        }
    }

    fun patchItem(id: String, item: ItemResponse) {
        viewModelScope.launch {
            try {
                val updated = client.records.update<ItemResponse>(
                    id = id,
                    sub = collection,
                    body = Json.encodeToString(item)
                )
//                Log.d("MainViewModel", "Item updated: $updated")
//                fetchItems()
            } catch (e: Exception) {
//                Log.e("MainViewModel", "Patch item failed", e)
            }
        }
    }

    fun deleteItem(id: String) {
        viewModelScope.launch {
            try {
                client.records.delete(id = id, sub = collection)
//                fetchItems()
            } catch (e: Exception) {
//                Log.e("MainViewModel", "Delete item failed", e)
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

    fun subscribeRealtimeItems() {
        sseJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                if (!sseIsConencted.value){
                    client.httpClient.sse(path = "/api/realtime"){
                        incoming
                            .flowOn(Dispatchers.IO)
                            .onEach { sseEvent ->
                                sseEvent.id?.let { id ->
                                    sseId.value = id
                                    sseIsConencted.value = true

                                    viewModelScope.launch {
                                        sendSubscribeRequest()
                                    }
                                    processEvent(sseEvent.data)
                                }
                            }
                            .collect {}
                    }
                }
                else{
//                    Log.d("SseClient", "Already Connected")
                    sseIsConencted.value = false
                }
            } catch (e: Exception) {
//                Log.e("MainViewModel", "Realtime subscription failed", e)
                sseIsConencted.value = false
            }
        }
    }

    private suspend fun sendSubscribeRequest(): Boolean {
        val body = mapOf(
            "clientId" to sseId.value!!.toJsonPrimitive(),
            "subscriptions" to JsonArray(listOf("Items".toJsonPrimitive()))
        )
        val response = client.httpClient.post {
            url {
                path("/api/realtime")
            }
            contentType(ContentType.Application.Json)
            setBody(body)
        }
//        Log.d("SseClient", "Response SSE : $response")
        return true
    }

    fun processEvent(jsonString: String?) {
        val json = Json { ignoreUnknownKeys = true }
//        println("Json String ${jsonString}")
        try {
            val element = json.parseToJsonElement(jsonString!!)
            if (element.jsonObject.containsKey("record")) {
                val event = json.decodeFromJsonElement<RealtimeEvent>(element)
                when (event.action) {
                    "create" -> {
                        fetchItems()
//                        println("Create ${event.record}")
                    }
                    "update" -> {
                        fetchItems()
//                        println("Update ${event.record}")
                    }
                    "delete" -> {
                        fetchItems()
//                        println("Delete ${event.record}")
                    }
                    else -> {
//                        println("Action tidak dikenal: ${event.action}")
                    }
                }
            } else {
//                println("Data tidak mengandung record, abaikan.")
            }
        } catch (e: Exception) {
//            println("Gagal parsing JSON: ${e.message}")
        }
    }

}