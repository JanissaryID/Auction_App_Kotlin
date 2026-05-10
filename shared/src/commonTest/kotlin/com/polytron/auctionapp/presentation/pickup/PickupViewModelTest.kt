package com.polytron.auctionapp.presentation.pickup

import com.polytron.auctionapp.domain.model.ItemResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PickupViewModelTest {

    @Test
    fun addSelectedItemAddsStatusTwoItem() {
        val vm = PickupViewModel()
        val item = ItemResponse(id = "pk-1", nameItem = "Item PK1", status = 2, orderID = "Order-ABC")

        vm.addSelectedItem(item)

        assertEquals(1, vm.selectedItems.value.size)
        assertEquals("pk-1", vm.selectedItems.value[0].id)
    }

    @Test
    fun addSelectedItemPreventsDuplicates() {
        val vm = PickupViewModel()
        val item = ItemResponse(id = "pk-1", nameItem = "Item PK1", status = 2)

        vm.addSelectedItem(item)
        vm.addSelectedItem(item)

        assertEquals(1, vm.selectedItems.value.size)
    }

    @Test
    fun removeSelectedItemRemovesById() {
        val vm = PickupViewModel()
        val item1 = ItemResponse(id = "pk-1", status = 2)
        val item2 = ItemResponse(id = "pk-2", status = 2)

        vm.addSelectedItem(item1)
        vm.addSelectedItem(item2)
        vm.removeSelectedItem(item1)

        assertEquals(1, vm.selectedItems.value.size)
        assertEquals("pk-2", vm.selectedItems.value[0].id)
    }

    @Test
    fun clearSelectedItemsEmptiesList() {
        val vm = PickupViewModel()
        vm.addSelectedItem(ItemResponse(id = "pk-1", status = 2))
        vm.addSelectedItem(ItemResponse(id = "pk-2", status = 2))

        vm.clearSelectedItems()

        assertTrue(vm.selectedItems.value.isEmpty())
    }

    @Test
    fun setSelectedItemsReplacesCurrentList() {
        val vm = PickupViewModel()
        vm.addSelectedItem(ItemResponse(id = "old-1", status = 2))

        val newItems = listOf(
            ItemResponse(id = "new-1", status = 2),
            ItemResponse(id = "new-2", status = 2),
            ItemResponse(id = "new-3", status = 2)
        )
        vm.setSelectedItems(newItems)

        assertEquals(3, vm.selectedItems.value.size)
    }
}
