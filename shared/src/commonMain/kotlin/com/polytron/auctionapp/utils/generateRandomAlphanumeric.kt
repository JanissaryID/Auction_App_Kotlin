package com.polytron.auctionapp.utils

private const val ALPHANUMERIC_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"

fun generateRandomAlphanumeric(length: Int = 6): String {
    return (1..length)
        .map { ALPHANUMERIC_CHARS.random() }
        .joinToString("")
}
