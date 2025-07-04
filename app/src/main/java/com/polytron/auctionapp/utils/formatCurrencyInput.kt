package com.polytron.auctionapp.utils

import java.text.NumberFormat
import java.util.Locale

fun formatCurrencyInput(input: String): String {
    val digitsOnly = input.replace(Regex("\\D"), "")
    return digitsOnly.toLongOrNull()?.let {
        NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID")).format(it)
    } ?: ""
}