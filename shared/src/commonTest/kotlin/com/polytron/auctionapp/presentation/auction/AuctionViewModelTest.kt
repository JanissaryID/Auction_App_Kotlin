package com.polytron.auctionapp.presentation.auction

import com.polytron.auctionapp.domain.model.ItemResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuctionViewModelTest {
    @Test
    fun addSelectedItemAvoidsDuplicateIds() {
        val viewModel = AuctionViewModel()
        val item = ItemResponse(id = "item-1", nameItem = "Item 1")

        viewModel.addSelectedItem(item)
        viewModel.addSelectedItem(item.copy(nameItem = "Updated Item 1"))

        assertEquals(1, viewModel.selectedItems.value.size)
        assertEquals("Item 1", viewModel.selectedItems.value.first().nameItem)
    }

    @Test
    fun removeSelectedItemClearsEditingStateForItem() {
        val viewModel = AuctionViewModel()
        val item = ItemResponse(id = "item-1", nameItem = "Item 1")
        viewModel.addSelectedItem(item)
        viewModel.updateEditingBuyer("item-1", "Buyer")
        viewModel.updateEditingPrice("item-1", "Rp 123.000")

        viewModel.removeSelectedItem(item)

        assertTrue(viewModel.selectedItems.value.isEmpty())
        assertTrue(viewModel.editingBuyers.value.isEmpty())
        assertTrue(viewModel.editingPrices.value.isEmpty())
    }

    @Test
    fun updateEditingPriceKeepsDigitsOnly() {
        val viewModel = AuctionViewModel()

        viewModel.updateEditingPrice("item-1", "Rp 1.234.500")

        assertEquals("1234500", viewModel.editingPrices.value["item-1"])
    }

    @Test
    fun clearSelectedItemsClearsSelectionAndEditingState() {
        val viewModel = AuctionViewModel()
        viewModel.addSelectedItem(ItemResponse(id = "item-1"))
        viewModel.updateEditingBuyer("item-1", "Buyer")
        viewModel.updateEditingPrice("item-1", "123")

        viewModel.clearSelectedItems()

        assertTrue(viewModel.selectedItems.value.isEmpty())
        assertTrue(viewModel.editingBuyers.value.isEmpty())
        assertTrue(viewModel.editingPrices.value.isEmpty())
    }
}
