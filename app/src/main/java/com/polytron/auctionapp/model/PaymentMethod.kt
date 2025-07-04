package com.polytron.auctionapp.model

enum class PaymentMethod(val label: String) {
    Cash("Cash"),
    QRIS("QRIS"),
    Credit("Kredit");

    companion object {
        val all = entries

        fun fromLabel(label: String): PaymentMethod =
            all.firstOrNull { it.label == label } ?: Cash
    }
}

