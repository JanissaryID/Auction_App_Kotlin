package com.polytron.auctionapp.presentation.auction

import com.polytron.auctionapp.domain.model.ItemResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AuctionViewModel {
    private val _selectedItems = MutableStateFlow<List<ItemResponse>>(emptyList())
    val selectedItems: StateFlow<List<ItemResponse>> = _selectedItems.asStateFlow()

    private val _editingBuyers = MutableStateFlow<Map<String, String>>(emptyMap())
    val editingBuyers: StateFlow<Map<String, String>> = _editingBuyers.asStateFlow()

    private val _editingPrices = MutableStateFlow<Map<String, String>>(emptyMap())
    val editingPrices: StateFlow<Map<String, String>> = _editingPrices.asStateFlow()

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

    fun updateEditingBuyer(itemId: String, name: String) {
        _editingBuyers.update { current ->
            current.toMutableMap().apply { put(itemId, name.toWinnerNameCase()) }
        }
    }

    fun updateEditingPrice(itemId: String, price: String) {
        val clean = price.filter { it.isDigit() }
        _editingPrices.update { current ->
            current.toMutableMap().apply { put(itemId, clean) }
        }
    }

    fun updateAllEditingPrices(price: String) {
        val clean = price.filter { it.isDigit() }
        val selectedIds = _selectedItems.value.mapNotNull { it.id }
        _editingPrices.update { current ->
            current.toMutableMap().apply {
                selectedIds.forEach { itemId -> put(itemId, clean) }
            }
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

    private fun String.toWinnerNameCase(): String {
        val lower = lowercase()
        var shouldCapitalize = true

        return buildString(lower.length) {
            lower.forEach { char ->
                if (char.isLetter()) {
                    append(if (shouldCapitalize) char.uppercaseChar() else char)
                    shouldCapitalize = false
                } else {
                    append(char)
                    shouldCapitalize = char.isWhitespace() || char == '-' || char == '\'' || char == '.'
                }
            }
        }
    }
}
