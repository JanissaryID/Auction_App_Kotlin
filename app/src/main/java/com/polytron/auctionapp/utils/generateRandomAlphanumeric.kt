package com.polytron.auctionapp.utils

fun generateRandomAlphanumeric(length: Int = 6): String {
    val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    return (1..length)
        .map { chars.random() }
        .joinToString("")
}
