package com.polytron.auctionapp.data.remote.repository

import com.polytron.auctionapp.data.remote.model.AuthResult
import com.polytron.auctionapp.data.remote.model.RealtimeSse
import com.polytron.auctionapp.model.ItemResponse

interface ItemsRepository {
    // Auth
    suspend fun loginWithEmailPassword(email: String, password: String): AuthResult
    suspend fun loginWithToken(token: String)

    // CRUD
    suspend fun getItems(page: Int = 1, perPage: Int = 500): List<ItemResponse>
    suspend fun createItem(item: ItemResponse): ItemResponse
    suspend fun updateItem(id: String, item: ItemResponse): ItemResponse
    suspend fun deleteItem(id: String)

    // Realtime (SSE)
    /**
     * Menjalankan SSE sampai coroutine dibatalkan oleh caller (ViewModel).
     * Tidak ada coroutine scope yang dimiliki repository.
     * onEvent dipanggil setiap kali ada event baru.
     */
    suspend fun withRealtimeEvents(onEvent: suspend (RealtimeSse) -> Unit)

    /**
     * Subscribe ke koleksi realtime (PocketBase).
     */
    suspend fun subscribeRealtime(clientId: String, collections: List<String>): Boolean
}