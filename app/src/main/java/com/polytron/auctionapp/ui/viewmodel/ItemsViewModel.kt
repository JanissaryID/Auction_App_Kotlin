package com.polytron.auctionapp.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.polytron.auctionapp.data.remote.model.RealtimeSse
import com.polytron.auctionapp.data.remote.repository.ItemsRepository
import com.polytron.auctionapp.data.session.SessionManager
import com.polytron.auctionapp.model.ItemResponse
import com.polytron.auctionapp.model.RealtimeEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject

/**
 * Handles Items CRUD operations and Realtime SSE subscription.
 * Automatically fetches items when session becomes active and stops SSE on logout.
 */
class ItemsViewModel(
    private val itemsRepository: ItemsRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val TAG = "ItemsViewModel"
    private val COLLECTION_ITEMS = "Items"

    // --- Items state ---
    private val _items = MutableStateFlow<List<ItemResponse>>(emptyList())
    val items: StateFlow<List<ItemResponse>> = _items.asStateFlow()

    // --- Loading / Error ---
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // --- SSE ---
    private val _sseConnected = MutableStateFlow(false)
    private val _sseId = MutableStateFlow<String?>(null)
    private var sseJob: Job? = null

    init {
        // React to session state changes
        viewModelScope.launch {
            sessionManager.isLoggedIn.collect { loggedIn ->
                if (loggedIn) {
                    fetchItems()
                    startRealtimeItems()
                } else {
                    // Session ended: clear data and stop SSE
                    _items.value = emptyList()
                    sseJob?.cancel()
                    sseJob = null
                    _sseConnected.value = false
                    _sseId.value = null
                }
            }
        }
    }

    // =========================================================================
    // CRUD
    // =========================================================================
    fun fetchItems() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _items.value = itemsRepository.getItems(page = 1, perPage = 500)
            } catch (e: Exception) {
                Log.e(TAG, "fetchItems error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createItem(item: ItemResponse) {
        viewModelScope.launch {
            try {
                itemsRepository.createItem(item)
            } catch (e: Exception) {
                Log.e(TAG, "createItem error: ${e.message}")
            }
        }
    }

    fun patchItem(id: String, item: ItemResponse) {
        viewModelScope.launch {
            try {
                itemsRepository.updateItem(id, item)
            } catch (e: Exception) {
                Log.e(TAG, "patchItem error: ${e.message}")
            }
        }
    }

    fun deleteItem(id: String) {
        viewModelScope.launch {
            try {
                itemsRepository.deleteItem(id)
            } catch (e: Exception) {
                Log.e(TAG, "deleteItem error: ${e.message}")
            }
        }
    }

    // =========================================================================
    // Realtime (SSE)
    // =========================================================================
    fun startRealtimeItems() {
        sseJob?.cancel()
        sseJob = viewModelScope.launch(Dispatchers.IO) {
            while (this.isActive) {
                try {
                    _sseConnected.value = false
                    Log.d(TAG, "Connecting to Realtime SSE...")

                    itemsRepository.withRealtimeEvents { evt: RealtimeSse ->
                        evt.id?.let { id ->
                            if (_sseId.value != id) {
                                _sseId.value = id
                                viewModelScope.launch {
                                    try {
                                        itemsRepository.subscribeRealtime(id, listOf(COLLECTION_ITEMS))
                                    } catch (e: Exception) {
                                        Log.e(TAG, "SSE subscribe error: ${e.message}")
                                    }
                                }
                            }
                            _sseConnected.value = true
                        }
                        handleRealtimeEventPayload(evt.data)
                    }
                } catch (e: Exception) {
                    _sseConnected.value = false
                    Log.e(TAG, "SSE connection error: ${e.message}. Retrying in 5s...")
                    kotlinx.coroutines.delay(5000)
                }
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
