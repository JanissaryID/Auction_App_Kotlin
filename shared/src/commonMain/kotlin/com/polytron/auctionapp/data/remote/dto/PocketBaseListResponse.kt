package com.polytron.auctionapp.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class PocketBaseListResponse<T>(
    val page: Int = 1,
    val perPage: Int = 0,
    val totalItems: Int = 0,
    val totalPages: Int = 0,
    val items: List<T> = emptyList()
)
