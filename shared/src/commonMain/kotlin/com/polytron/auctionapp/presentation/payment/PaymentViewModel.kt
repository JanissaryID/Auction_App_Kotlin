package com.polytron.auctionapp.presentation.payment

import com.polytron.auctionapp.domain.model.ItemResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class PaymentViewModel {
    private val _selectedItems = MutableStateFlow<List<ItemResponse>>(emptyList())
    val selectedItems: StateFlow<List<ItemResponse>> = _selectedItems.asStateFlow()

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
    }

    fun clearSelectedItems() {
        _selectedItems.update { emptyList() }
    }
}
