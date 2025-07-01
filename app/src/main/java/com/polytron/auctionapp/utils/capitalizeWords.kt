package com.polytron.auctionapp.utils

import java.util.Locale

fun capitalizeWords(input: String): String {
    return input.lowercase(Locale.getDefault())
        .split(" ")
        .joinToString(" ") { it.replaceFirstChar { c -> c.uppercaseChar() } }
}
