package com.polytron.auctionapp.model


import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class ItemResponse(
    @SerialName("orderID") val orderID: String? = null,
    @SerialName("created") val created: String? = null,
    @SerialName("admin") val admin: String? = null,
    @SerialName("nameItem") val nameItem: String? = null,
    @SerialName("buyer") val buyer: String? = null,
    @SerialName("collectionName") val collectionName: String? = null,
    @SerialName("price") val price: String? = null,
    @SerialName("id") val id: String? = null,
    @SerialName("maxPrice") val maxPrice: String? = null,
    @SerialName("collectionId") val collectionId: String? = null,
    @SerialName("updated") val updated: String? = null,
    @SerialName("codeItem") val codeItem: String? = null,
    @SerialName("basePrice") val basePrice: String? = null,
    @SerialName("status") val status: Int? = null,
    @SerialName("typePayment") val typePayment: String? = null
) : Parcelable