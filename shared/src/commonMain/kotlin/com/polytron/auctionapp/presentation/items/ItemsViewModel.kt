package com.polytron.auctionapp.presentation.items

import com.polytron.auctionapp.core.logging.AppLogger
import com.polytron.auctionapp.domain.model.ItemResponse
import com.polytron.auctionapp.domain.model.RealtimeEvent
import com.polytron.auctionapp.domain.model.RealtimeSse
import com.polytron.auctionapp.domain.session.SessionManager
import com.polytron.auctionapp.domain.usecase.items.CreateItemUseCase
import com.polytron.auctionapp.domain.usecase.items.DeleteItemUseCase
import com.polytron.auctionapp.domain.usecase.items.FetchItemsUseCase
import com.polytron.auctionapp.domain.usecase.items.UpdateItemUseCase
import com.polytron.auctionapp.domain.usecase.realtime.ObserveItemsRealtimeUseCase
import com.polytron.auctionapp.domain.usecase.realtime.SubscribeItemsRealtimeUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject

class ItemsViewModel(
    private val scope: CoroutineScope,
    private val fetchItemsUseCase: FetchItemsUseCase,
    private val createItemUseCase: CreateItemUseCase,
    private val updateItemUseCase: UpdateItemUseCase,
    private val deleteItemUseCase: DeleteItemUseCase,
    private val observeItemsRealtimeUseCase: ObserveItemsRealtimeUseCase,
    private val subscribeItemsRealtimeUseCase: SubscribeItemsRealtimeUseCase,
    private val sessionManager: SessionManager,
    private val logger: AppLogger
) {
    private val tag = "ItemsViewModel"
    private val collectionItems = "Items"
    private val json = Json { ignoreUnknownKeys = true }

    private val _items = MutableStateFlow<List<ItemResponse>>(emptyList())
    val items: StateFlow<List<ItemResponse>> = _items.asStateFlow()

    private val _toastEvent = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _sseConnected = MutableStateFlow(false)
    private val _sseId = MutableStateFlow<String?>(null)
    private var fetchItemsJob: Job? = null
    private var realtimeRefreshJob: Job? = null
    private var sseJob: Job? = null

    init {
        scope.launch {
            sessionManager.isLoggedIn.collect { loggedIn ->
                if (loggedIn) {
                    fetchItems()
                    startRealtimeItems()
                } else {
                    _items.value = emptyList()
                    sseJob?.cancel()
                    fetchItemsJob?.cancel()
                    realtimeRefreshJob?.cancel()
                    sseJob = null
                    fetchItemsJob = null
                    realtimeRefreshJob = null
                    _sseConnected.value = false
                    _sseId.value = null
                }
            }
        }
    }

    fun showToast(message: String) {
        scope.launch {
            _toastEvent.emit(message)
        }
    }

    fun fetchItems() {
        fetchItemsJob?.cancel()
        val nextJob = scope.launch(start = CoroutineStart.LAZY) {
            _isLoading.value = true
            try {
                _items.value = fetchItemsUseCase(page = 1, perPage = 500)
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    logger.error(tag, "fetchItems error: ${e.message}", e)
                }
            } finally {
                if (fetchItemsJob == coroutineContext[Job]) {
                    _isLoading.value = false
                }
            }
        }
        fetchItemsJob = nextJob
        nextJob.start()
    }

    suspend fun createItem(item: ItemResponse): Result<Unit> {
        return try {
            createItemUseCase(item)
            Result.success(Unit)
        } catch (e: Exception) {
            logger.error(tag, "createItem error: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun patchItem(id: String, item: ItemResponse): Result<Unit> {
        return try {
            updateItemUseCase(id = id, item = item)
            Result.success(Unit)
        } catch (e: Exception) {
            logger.error(tag, "patchItem error: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun deleteItem(id: String): Result<Unit> {
        return try {
            deleteItemUseCase(id)
            Result.success(Unit)
        } catch (e: Exception) {
            logger.error(tag, "deleteItem error: ${e.message}", e)
            Result.failure(e)
        }
    }

    fun startRealtimeItems() {
        sseJob?.cancel()
        sseJob = scope.launch {
            while (isActive) {
                try {
                    _sseConnected.value = false
                    logger.debug(tag, "Connecting to Realtime SSE...")

                    observeItemsRealtimeUseCase { evt: RealtimeSse ->
                        evt.id?.let { id ->
                            if (_sseId.value != id) {
                                _sseId.value = id
                                scope.launch {
                                    try {
                                        subscribeItemsRealtimeUseCase(id, listOf(collectionItems))
                                    } catch (e: Exception) {
                                        logger.error(tag, "SSE subscribe error: ${e.message}", e)
                                    }
                                }
                            }
                            _sseConnected.value = true
                        }
                        handleRealtimeEventPayload(evt.data)
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    _sseConnected.value = false
                    logger.error(tag, "SSE connection error: ${e.message}. Retrying in 5s...", e)
                    delay(5000)
                }
            }
        }
    }

    private fun handleRealtimeEventPayload(jsonString: String?) {
        if (jsonString.isNullOrBlank()) return
        try {
            val element = json.parseToJsonElement(jsonString)
            if (element.jsonObject.containsKey("record")) {
                val event = json.decodeFromJsonElement<RealtimeEvent>(element)
                when (event.action) {
                    "create", "update", "delete" -> scheduleRealtimeRefresh()
                    else -> Unit
                }
            }
        } catch (_: Exception) {
        }
    }

    private fun scheduleRealtimeRefresh() {
        realtimeRefreshJob?.cancel()
        realtimeRefreshJob = scope.launch {
            delay(350)
            fetchItems()
        }
    }
}
