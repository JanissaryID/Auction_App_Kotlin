package com.polytron.auctionapp.presentation.auction

import com.polytron.auctionapp.domain.model.ItemResponse
import com.polytron.auctionapp.test.FakeItemsRepository
import kotlinx.coroutines.test.TestScope
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuctionViewModelTest {
    @Test
    fun addSelectedItemAvoidsDuplicateIds() {
        val viewModel = createViewModel()
        val item = ItemResponse(id = "item-1", nameItem = "Item 1")

        viewModel.addSelectedItem(item)
        viewModel.addSelectedItem(item.copy(nameItem = "Updated Item 1"))

        assertEquals(1, viewModel.selectedItems.value.size)
        assertEquals("Item 1", viewModel.selectedItems.value.first().nameItem)
    }

    @Test
    fun removeSelectedItemClearsEditingStateForItem() {
        val viewModel = createViewModel()
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
        val viewModel = createViewModel()

        viewModel.updateEditingPrice("item-1", "Rp 1.234.500")

        assertEquals("1234500", viewModel.editingPrices.value["item-1"])
    }

    @Test
    fun updateEditingBuyerCapitalizesWinnerName() {
        val viewModel = createViewModel()

        viewModel.updateEditingBuyer("item-1", "budi santoso")

        assertEquals("Budi Santoso", viewModel.editingBuyers.value["item-1"])
    }

    @Test
    fun updateAllEditingPricesAppliesCleanPriceToSelectedItems() {
        val viewModel = createViewModel()
        viewModel.setSelectedItems(
            listOf(
                ItemResponse(id = "item-1"),
                ItemResponse(id = "item-2"),
                ItemResponse(id = null)
            )
        )
        viewModel.updateEditingPrice("item-2", "1")

        viewModel.updateAllEditingPrices("Rp 250.000")

        assertEquals("250000", viewModel.editingPrices.value["item-1"])
        assertEquals("250000", viewModel.editingPrices.value["item-2"])
        assertEquals(2, viewModel.editingPrices.value.size)
    }

    @Test
    fun clearSelectedItemsClearsSelectionAndEditingState() {
        val viewModel = createViewModel()
        viewModel.addSelectedItem(ItemResponse(id = "item-1"))
        viewModel.updateEditingBuyer("item-1", "Buyer")
        viewModel.updateEditingPrice("item-1", "123")

        viewModel.clearSelectedItems()

        assertTrue(viewModel.selectedItems.value.isEmpty())
        assertTrue(viewModel.editingBuyers.value.isEmpty())
        assertTrue(viewModel.editingPrices.value.isEmpty())
    }

    private fun createViewModel(): AuctionViewModel {
        return AuctionViewModel(
            repository = FakeItemsRepository(),
            scope = TestScope()
        )
    }
}
