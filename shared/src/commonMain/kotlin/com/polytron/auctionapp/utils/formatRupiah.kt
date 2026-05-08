package com.polytron.auctionapp.utils

fun formatRupiah(value: String?): String {
    val number = value?.toLongOrNull() ?: return "-"
    return "Rp ${formatIndonesianInteger(number.toString())}"
}
