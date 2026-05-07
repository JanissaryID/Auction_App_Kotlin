package com.polytron.auctionapp.desktop.data.remote

import io.github.agrevster.pocketbaseKotlin.models.utils.BaseModel
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val email: String,
    val name: String,
    val avatar: String? = null
) : BaseModel()
