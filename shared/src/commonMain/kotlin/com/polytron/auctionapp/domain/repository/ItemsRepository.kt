package com.polytron.auctionapp.domain.repository

import com.polytron.auctionapp.domain.model.AuthResult
import com.polytron.auctionapp.domain.model.ItemResponse
import com.polytron.auctionapp.domain.model.RealtimeSse
import com.polytron.auctionapp.domain.model.User

interface ItemsRepository : Repository {
    suspend fun loginWithEmailPassword(email: String, password: String): AuthResult
    suspend fun loginWithToken(token: String)
    suspend fun getUser(id: String): User

    suspend fun getItems(page: Int = 1, perPage: Int = 500): List<ItemResponse>
    suspend fun createItem(item: ItemResponse): ItemResponse
    suspend fun updateItem(id: String, item: ItemResponse): ItemResponse
    suspend fun deleteItem(id: String)

    suspend fun getAuctionUsers(page: Int = 1, perPage: Int = 500): List<com.polytron.auctionapp.domain.model.ItemsUserAuction>
    suspend fun createAuctionUser(user: com.polytron.auctionapp.domain.model.ItemsUserAuction): com.polytron.auctionapp.domain.model.ItemsUserAuction

    suspend fun withRealtimeEvents(onEvent: suspend (RealtimeSse) -> Unit)
    suspend fun subscribeRealtime(clientId: String, collections: List<String>): Boolean
}
