package com.polytron.auctionapp.utils

import java.text.NumberFormat
import java.util.Locale

fun formatRupiah(value: String?): String {
    val number = value?.toLongOrNull() ?: return "-"
    val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))
    return "Rp ${formatter.format(number)}"
}