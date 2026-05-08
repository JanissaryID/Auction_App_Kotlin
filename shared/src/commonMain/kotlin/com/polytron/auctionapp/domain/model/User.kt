package com.polytron.auctionapp.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String? = null,
    val email: String = "",
    val name: String = "",
    val avatar: String? = null
) : DomainModel
