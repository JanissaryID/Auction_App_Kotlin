package com.polytron.auctionapp.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class AuthResult(
    val token: String,
    val userId: String,
    val name: String? = null,
    val avatar: String? = null
) : DomainModel
