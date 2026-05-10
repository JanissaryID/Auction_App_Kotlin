package com.polytron.auctionapp.presentation.payment

import com.polytron.auctionapp.domain.model.ItemResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PaymentViewModelTest {

    @Test
    fun addSelectedItemAddsItemToList() {
        val vm = PaymentViewModel()
        val item = ItemResponse(id = "p-1", nameItem = "Item P1", status = 1, buyer = "Buyer", price = "100000")

        vm.addSelectedItem(item)

        assertEquals(1, vm.selectedItems.value.size)
        assertEquals("p-1", vm.selectedItems.value[0].id)
    }

    @Test
    fun addSelectedItemPreventsStatusOneItemDuplicates() {
        val vm = PaymentViewModel()
        val item = ItemResponse(id = "p-1", nameItem = "Item P1", status = 1)

        vm.addSelectedItem(item)
        vm.addSelectedItem(item.copy(nameItem = "Different Name"))

        assertEquals(1, vm.selectedItems.value.size)
        assertEquals("Item P1", vm.selectedItems.value[0].nameItem)
    }

    @Test
    fun removeSelectedItemRemovesFromList() {
        val vm = PaymentViewModel()
        val item1 = ItemResponse(id = "p-1", nameItem = "Item P1", status = 1)
        val item2 = ItemResponse(id = "p-2", nameItem = "Item P2", status = 1)

        vm.addSelectedItem(item1)
        vm.addSelectedItem(item2)
        vm.removeSelectedItem(item1)

        assertEquals(1, vm.selectedItems.value.size)
        assertEquals("p-2", vm.selectedItems.value[0].id)
    }

    @Test
    fun clearSelectedItemsEmptiesList() {
        val vm = PaymentViewModel()
        vm.addSelectedItem(ItemResponse(id = "p-1", status = 1))
        vm.addSelectedItem(ItemResponse(id = "p-2", status = 1))

        vm.clearSelectedItems()

        assertTrue(vm.selectedItems.value.isEmpty())
    }

    @Test
    fun setSelectedItemsReplacesCurrentList() {
        val vm = PaymentViewModel()
        vm.addSelectedItem(ItemResponse(id = "old-1", status = 1))

        val newItems = listOf(
            ItemResponse(id = "new-1", status = 1),
            ItemResponse(id = "new-2", status = 1)
        )
        vm.setSelectedItems(newItems)

        assertEquals(2, vm.selectedItems.value.size)
        assertEquals("new-1", vm.selectedItems.value[0].id)
    }
}
