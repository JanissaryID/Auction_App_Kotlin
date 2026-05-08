package com.polytron.auctionapp.utils

fun formatCurrencyInput(input: String): String {
    val digitsOnly = input.replace(Regex("\\D"), "")
    val number = digitsOnly.toLongOrNull() ?: return ""
    return formatIndonesianInteger(number.toString())
}

internal fun formatIndonesianInteger(value: String): String {
    val isNegative = value.startsWith("-")
    val digits = if (isNegative) value.drop(1) else value
    val grouped = digits
        .reversed()
        .chunked(3)
        .joinToString(".")
        .reversed()

    return if (isNegative) "-$grouped" else grouped
}
