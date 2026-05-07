package com.polytron.auctionapp.desktop.data.remote

data class AuthResult(
    val token: String,
    val userId: String,
    val name: String?,
    val avatar: String?
)
