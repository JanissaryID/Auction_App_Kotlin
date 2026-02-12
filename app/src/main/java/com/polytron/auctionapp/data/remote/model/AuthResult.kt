package com.polytron.auctionapp.data.remote.model

data class AuthResult(
    val token: String,
    val userId: String,
    val name: String?,
    val avatar: String?
)