package com.polytron.auctionapp.data.local.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "items_cache") // Siap untuk Caching Database Lokal (Room)
data class ItemModel(
    @PrimaryKey // Penting untuk Caching & SSE (Mapping ID)
    @SerializedName("id")
    val id: String,

    @SerializedName("collectionId")
    val collectionId: String? = null,

    @SerializedName("collectionName")
    val collectionName: String? = null,

    @SerializedName("status")
    val status: Int? = 0,

    @SerializedName("orderID")
    val orderID: String? = null,

    @SerializedName("admin")
    val admin: String? = null,

    @SerializedName("nameItem")
    val nameItem: String? = null,

    @SerializedName("buyer")
    val buyer: String? = null,

    @SerializedName("price")
    val price: String? = null,

    @SerializedName("maxPrice")
    val maxPrice: String? = null,

    @SerializedName("codeItem")
    val codeItem: String? = null,

    @SerializedName("basePrice")
    val basePrice: String? = null,

    @SerializedName("typePayment")
    val typePayment: String? = null,

    @SerializedName("user")
    val user: String? = null,

    @SerializedName("created")
    val created: String? = null,

    @SerializedName("updated")
    val updated: String? = null
)