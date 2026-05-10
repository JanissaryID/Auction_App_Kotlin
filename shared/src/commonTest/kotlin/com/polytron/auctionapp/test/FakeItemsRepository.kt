package com.polytron.auctionapp.test

import com.polytron.auctionapp.domain.model.AuthResult
import com.polytron.auctionapp.domain.model.ItemResponse
import com.polytron.auctionapp.domain.model.RealtimeSse
import com.polytron.auctionapp.domain.model.User
import com.polytron.auctionapp.domain.repository.ItemsRepository

/**
 * In-memory fake for ItemsRepository.
 * Records all calls and allows configuration of responses per test.
 *
 * Usage:
 *   val repo = FakeItemsRepository()
 *   repo.itemsToReturn = listOf(item1, item2)
 *   repo.authResultToReturn = AuthResult(...)
 *   repo.loginShouldFail = true   // to simulate errors
 */
class FakeItemsRepository : ItemsRepository {

    // ── Configuration ──────────────────────────────────────────
    var authResultToReturn = AuthResult(
        token = "fake-token",
        userId = "user-1",
        name = "Test User",
        avatar = null
    )
    var userToReturn = User(id = "user-1", email = "gkj@kudus.com", name = "Test User")
    var itemsToReturn = mutableListOf<ItemResponse>()
    var createdItemToReturn: ItemResponse? = null

    var loginShouldFail = false
    var loginErrorMessage = "Login failed"
    var fetchShouldFail = false
    var fetchErrorMessage = "Fetch failed"
    var createShouldFail = false
    var updateShouldFail = false
    var deleteShouldFail = false

    // ── Call tracking ──────────────────────────────────────────
    var loginWithEmailPasswordCount = 0; private set
    var loginWithTokenCount = 0; private set
    var lastLoginToken: String? = null; private set
    var getUserCount = 0; private set
    var getItemsCount = 0; private set
    var createItemCount = 0; private set
    var lastCreatedItem: ItemResponse? = null; private set
    var updateItemCount = 0; private set
    var lastUpdatedId: String? = null; private set
    var lastUpdatedItem: ItemResponse? = null; private set
    var deleteItemCount = 0; private set
    var lastDeletedId: String? = null; private set
    var withRealtimeEventsCount = 0; private set
    var subscribeRealtimeCount = 0; private set

    // ── Implementation ─────────────────────────────────────────
    override suspend fun loginWithEmailPassword(email: String, password: String): AuthResult {
        loginWithEmailPasswordCount++
        if (loginShouldFail) throw RuntimeException(loginErrorMessage)
        return authResultToReturn
    }

    override suspend fun loginWithToken(token: String) {
        loginWithTokenCount++
        lastLoginToken = token
        if (loginShouldFail) throw RuntimeException(loginErrorMessage)
    }

    override suspend fun getUser(id: String): User {
        getUserCount++
        return userToReturn
    }

    override suspend fun getItems(page: Int, perPage: Int): List<ItemResponse> {
        getItemsCount++
        if (fetchShouldFail) throw RuntimeException(fetchErrorMessage)
        return itemsToReturn.toList()
    }

    override suspend fun createItem(item: ItemResponse): ItemResponse {
        createItemCount++
        lastCreatedItem = item
        if (createShouldFail) throw RuntimeException("Create failed")
        val result = createdItemToReturn ?: item.copy(id = "new-${createItemCount}")
        itemsToReturn.add(result)
        return result
    }

    override suspend fun updateItem(id: String, item: ItemResponse): ItemResponse {
        updateItemCount++
        lastUpdatedId = id
        lastUpdatedItem = item
        if (updateShouldFail) throw RuntimeException("Update failed")
        return item.copy(id = id)
    }

    override suspend fun deleteItem(id: String) {
        deleteItemCount++
        lastDeletedId = id
        if (deleteShouldFail) throw RuntimeException("Delete failed")
        itemsToReturn.removeAll { it.id == id }
    }

    override suspend fun withRealtimeEvents(onEvent: suspend (RealtimeSse) -> Unit) {
        withRealtimeEventsCount++
        // No-op: SSE simulation done through separate mechanism
    }

    override suspend fun subscribeRealtime(clientId: String, collections: List<String>): Boolean {
        subscribeRealtimeCount++
        return true
    }

    /** Reset all counters and configuration to default. */
    fun reset() {
        loginWithEmailPasswordCount = 0
        loginWithTokenCount = 0
        lastLoginToken = null
        getUserCount = 0
        getItemsCount = 0
        createItemCount = 0
        lastCreatedItem = null
        updateItemCount = 0
        lastUpdatedId = null
        lastUpdatedItem = null
        deleteItemCount = 0
        lastDeletedId = null
        withRealtimeEventsCount = 0
        subscribeRealtimeCount = 0
        itemsToReturn.clear()
        loginShouldFail = false
        fetchShouldFail = false
        createShouldFail = false
        updateShouldFail = false
        deleteShouldFail = false
    }
}
