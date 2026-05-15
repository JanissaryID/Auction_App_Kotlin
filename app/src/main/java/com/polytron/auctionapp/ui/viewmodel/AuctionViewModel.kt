package com.polytron.auctionapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.polytron.auctionapp.domain.model.ItemResponse
import com.polytron.auctionapp.domain.repository.ItemsRepository
import com.polytron.auctionapp.presentation.auction.AuctionViewModel as SharedAuctionViewModel

class AuctionViewModel(
    repository: ItemsRepository
) : ViewModel() {
    private val delegate = SharedAuctionViewModel(
        repository = repository,
        scope = viewModelScope
    )

    val selectedItems = delegate.selectedItems
    val editingBuyers = delegate.editingBuyers
    val editingPrices = delegate.editingPrices
    val auctionUsers = delegate.auctionUsers

    fun fetchAuctionUsers() = delegate.fetchAuctionUsers()
    suspend fun saveNewAuctionUsers(names: List<String>, userId: String) = delegate.saveNewAuctionUsers(names, userId)
    fun setSelectedItems(items: List<ItemResponse>) = delegate.setSelectedItems(items)
    fun addSelectedItem(item: ItemResponse) = delegate.addSelectedItem(item)
    fun removeSelectedItem(item: ItemResponse) = delegate.removeSelectedItem(item)
    fun clearSelectedItems() = delegate.clearSelectedItems()
    fun updateEditingBuyer(itemId: String, name: String) = delegate.updateEditingBuyer(itemId, name)
    fun updateEditingPrice(itemId: String, price: String) = delegate.updateEditingPrice(itemId, price)
    fun updateAllEditingPrices(price: String) = delegate.updateAllEditingPrices(price)
    fun clearEditingForItem(itemId: String) = delegate.clearEditingForItem(itemId)
}
