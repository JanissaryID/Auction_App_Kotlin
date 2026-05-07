package com.polytron.auctionapp.shared.viewmodel

import com.polytron.auctionapp.shared.model.SharedItem
import com.polytron.auctionapp.shared.repository.ItemsRepository
import com.polytron.auctionapp.shared.usecase.FilterItemsUseCase
import com.polytron.auctionapp.shared.usecase.GroupItemsByNameUseCase
import com.polytron.auctionapp.shared.usecase.GroupItemsByOrderUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ItemsSharedState(
    val searchQuery: String = "",
    val items: List<SharedItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val filteredItems: List<SharedItem>
        get() = FilterItemsUseCase()(items, searchQuery)

    val groupedByName: Map<String, List<SharedItem>>
        get() = GroupItemsByNameUseCase()(filteredItems)

    val groupedByOrderId: Map<String, List<SharedItem>>
        get() = GroupItemsByOrderUseCase()(filteredItems)
}

class ItemsSharedViewModel(
    private val repository: ItemsRepository
) {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    private val _state = MutableStateFlow(ItemsSharedState())
    val state: StateFlow<ItemsSharedState> = _state.asStateFlow()

    init {
        observeItems()
    }

    private fun observeItems() {
        repository.observeItems()
            .onEach { items ->
                _state.update { it.copy(items = items, isLoading = false) }
            }
            .launchIn(viewModelScope)
    }

    fun refreshItems(page: Int = 1, perPage: Int = 500) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                repository.refreshItems(page, perPage)
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun updateSearchQuery(value: String) {
        _state.update { it.copy(searchQuery = value) }
    }

    fun updateItemStatus(id: String, status: Int, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                repository.updateItemStatus(id, status)
                onComplete()
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    fun createItem(item: SharedItem, onComplete: (SharedItem) -> Unit = {}) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val created = repository.createItem(item)
                _state.update { it.copy(isLoading = false) }
                onComplete(created)
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun updateItem(id: String, item: SharedItem, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                repository.updateItem(id, item)
                _state.update { it.copy(isLoading = false) }
                onComplete()
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun deleteItem(id: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                repository.deleteItem(id)
                _state.update { it.copy(isLoading = false) }
                onComplete()
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}
