package com.polytron.auctionapp.data.remote.dto

import com.polytron.auctionapp.domain.model.User
import kotlinx.serialization.Serializable

@Serializable
internal data class PocketBaseAuthResponse(
    val token: String,
    val record: User? = null
)
