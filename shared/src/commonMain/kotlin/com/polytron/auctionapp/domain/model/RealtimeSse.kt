package com.polytron.auctionapp.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class RealtimeSse(
    val id: String? = null,
    val data: String? = null
) : DomainModel
