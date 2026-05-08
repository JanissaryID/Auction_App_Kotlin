package com.polytron.auctionapp.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class RealtimeEvent(
    val action: String,
    val record: ItemResponse
) : DomainModel
