package com.polytron.auctionapp.domain.usecase.items

import com.polytron.auctionapp.domain.model.ItemResponse
import com.polytron.auctionapp.test.FakeItemsRepository
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class FetchItemsUseCaseTest {

    @Test
    fun fetchReturnItemsFromRepository() = runBlocking {
        val repo = FakeItemsRepository()
        repo.itemsToReturn.addAll(
            listOf(
                ItemResponse(id = "1", nameItem = "Item A"),
                ItemResponse(id = "2", nameItem = "Item B")
            )
        )
        val useCase = FetchItemsUseCase(repo)

        val result = useCase(page = 1, perPage = 500)

        assertEquals(2, result.size)
        assertEquals("Item A", result[0].nameItem)
        assertEquals("Item B", result[1].nameItem)
        assertEquals(1, repo.getItemsCount)
    }
}

class CreateItemUseCaseTest {

    @Test
    fun createDelegatesToRepository() = runBlocking {
        val repo = FakeItemsRepository()
        val useCase = CreateItemUseCase(repo)
        val item = ItemResponse(nameItem = "New Item", basePrice = "100000", status = 0)

        useCase(item)

        assertEquals(1, repo.createItemCount)
        assertEquals("New Item", repo.lastCreatedItem?.nameItem)
    }
}

class UpdateItemUseCaseTest {

    @Test
    fun updateDelegatesToRepository() = runBlocking {
        val repo = FakeItemsRepository()
        val useCase = UpdateItemUseCase(repo)
        val item = ItemResponse(nameItem = "Updated", buyer = "Buyer A", price = "500000", status = 1)

        useCase(id = "item-1", item = item)

        assertEquals(1, repo.updateItemCount)
        assertEquals("item-1", repo.lastUpdatedId)
        assertEquals("Updated", repo.lastUpdatedItem?.nameItem)
        assertEquals(1, repo.lastUpdatedItem?.status)
    }
}

class DeleteItemUseCaseTest {

    @Test
    fun deleteDelegatesToRepository() = runBlocking {
        val repo = FakeItemsRepository()
        val useCase = DeleteItemUseCase(repo)

        useCase("item-99")

        assertEquals(1, repo.deleteItemCount)
        assertEquals("item-99", repo.lastDeletedId)
    }
}
