package com.polytron.auctionapp.model

import io.github.agrevster.pocketbaseKotlin.models.Record
import kotlinx.serialization.Serializable

@Serializable
data class ItemResponse(
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
    val typePayment: String? = null
) : Record()