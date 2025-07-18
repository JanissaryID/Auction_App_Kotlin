package com.polytron.auctionapp.model


import kotlinx.serialization.Serializable

@Serializable
data class RealtimeEvent(
    val action: String,
    val record: ItemResponse
)