package com.polytron.auctionapp.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ItemsUserAuction(
    val id: String? = null,
    val collectionId: String? = null,
    val collectionName: String? = null,
    val name: String? = null,
    val user: String? = null,
    val created: String? = null,
    val updated: String? = null
) : DomainModel
