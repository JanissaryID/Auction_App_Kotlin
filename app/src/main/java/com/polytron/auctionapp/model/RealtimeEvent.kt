package com.polytron.auctionapp.model


import io.github.agrevster.pocketbaseKotlin.models.Record
import kotlinx.serialization.Serializable

@Serializable
data class ItemRecord(
    val name: String,
    val quantity: Int,
) : Record()