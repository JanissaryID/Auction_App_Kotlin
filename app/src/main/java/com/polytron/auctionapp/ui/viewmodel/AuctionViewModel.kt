package com.polytron.auctionapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.polytron.auctionapp.domain.model.ItemResponse
import com.polytron.auctionapp.presentation.auction.AuctionViewModel as SharedAuctionViewModel

class AuctionViewModel(
    private val delegate: SharedAuctionViewModel
) : ViewModel() {
    val selectedItems = delegate.selectedItems
    val editingBuyers = delegate.editingBuyers
    val editingPrices = delegate.editingPrices

    fun setSelectedItems(items: List<ItemResponse>) = delegate.setSelectedItems(items)
    fun addSelectedItem(item: ItemResponse) = delegate.addSelectedItem(item)
    fun removeSelectedItem(item: ItemResponse) = delegate.removeSelectedItem(item)
    fun clearSelectedItems() = delegate.clearSelectedItems()
    fun updateEditingBuyer(itemId: String, name: String) = delegate.updateEditingBuyer(itemId, name)
    fun updateEditingPrice(itemId: String, price: String) = delegate.updateEditingPrice(itemId, price)
    fun clearEditingForItem(itemId: String) = delegate.clearEditingForItem(itemId)
}
