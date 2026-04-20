package com.polytron.auctionapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.polytron.auctionapp.model.ItemResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Manages item selection state and per-item editing fields (buyer name, price).
 * Used by Auction and Payment screens.
 */
class AuctionViewModel : ViewModel() {

    // --- Selected items ---
    private val _selectedItems = MutableStateFlow<List<ItemResponse>>(emptyList())
    val selectedItems: StateFlow<List<ItemResponse>> = _selectedItems.asStateFlow()

    // --- Per-item editing state ---
    private val _editingBuyers = MutableStateFlow<Map<String, String>>(emptyMap())
    val editingBuyers: StateFlow<Map<String, String>> = _editingBuyers.asStateFlow()

    private val _editingPrices = MutableStateFlow<Map<String, String>>(emptyMap())
    val editingPrices: StateFlow<Map<String, String>> = _editingPrices.asStateFlow()

    // =========================================================================
    // Selection
    // =========================================================================
    fun setSelectedItems(items: List<ItemResponse>) {
        _selectedItems.update { items }
    }

    fun addSelectedItem(item: ItemResponse) {
        _selectedItems.update { current ->
            if (!current.any { it.id == item.id }) current + item else current
        }
    }

    fun removeSelectedItem(item: ItemResponse) {
        _selectedItems.update { current ->
            current.filter { it.id != item.id }
        }
        clearEditingForItem(item.id ?: "")
    }

    fun clearSelectedItems() {
        _selectedItems.update { emptyList() }
        _editingBuyers.update { emptyMap() }
        _editingPrices.update { emptyMap() }
    }

    // =========================================================================
    // Editing
    // =========================================================================
    fun updateEditingBuyer(itemId: String, name: String) {
        _editingBuyers.update { current ->
            current.toMutableMap().apply { put(itemId, name) }
        }
    }

    fun updateEditingPrice(itemId: String, price: String) {
        val clean = price.filter { it.isDigit() }
        _editingPrices.update { current ->
            current.toMutableMap().apply { put(itemId, clean) }
        }
    }

    fun clearEditingForItem(itemId: String) {
        _editingBuyers.update { current ->
            current.toMutableMap().apply { remove(itemId) }
        }
        _editingPrices.update { current ->
            current.toMutableMap().apply { remove(itemId) }
        }
    }
}
