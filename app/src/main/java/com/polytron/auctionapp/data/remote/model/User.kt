package com.polytron.auctionapp.data.remote.model

import io.github.agrevster.pocketbaseKotlin.models.utils.BaseModel
import kotlinx.serialization.Serializable

@Serializable
data class User(
    // Karena mewarisi BaseModel, id sudah ada di parent (BaseModel)
    val email: String,
    val name: String,
    val avatar: String? = null
) : BaseModel()
