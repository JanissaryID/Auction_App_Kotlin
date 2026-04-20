package com.polytron.auctionapp.data.local.room

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    // Ambil semua data dengan aliran (Flow) untuk update real-time ke UI
    @Query("SELECT * FROM items_cache ORDER BY updated DESC")
    fun getAllItems(): Flow<List<ItemModel>>

    // Ambil data dengan limit dan offset untuk pagination manual
    @Query("SELECT * FROM items_cache ORDER BY updated DESC LIMIT :limit OFFSET :offset")
    suspend fun getPagedItems(limit: Int, offset: Int): List<ItemModel>

    // Upsert: Jika ID sudah ada, timpa (Update). Jika belum, tambah (Insert).
    // Sangat berguna untuk data dari SSE
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<ItemModel>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ItemModel)

    @Query("DELETE FROM items_cache")
    suspend fun clearAllItems()

    @Query("SELECT COUNT(*) FROM items_cache")
    suspend fun getItemCount(): Int
}