package com.polytron.auctionapp.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ItemResponse(
    val id: String? = null,
    val collectionId: String? = null,
    val collectionName: String? = null,
    @SerialName("orderID")
    val orderID: String? = null,
    val admin: String? = null,
    val nameItem: String? = null,
    val buyer: String? = null,
    val price: String? = null,
    val maxPrice: String? = null,
    val codeItem: String? = null,
    val basePrice: String? = null,
    val user: String? = null,
    val status: Int? = null,
    val typePayment: String? = null,
    val created: String? = null,
    val updated: String? = null
) : DomainModel
