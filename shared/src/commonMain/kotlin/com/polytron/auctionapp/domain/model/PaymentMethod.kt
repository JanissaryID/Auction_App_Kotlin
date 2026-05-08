package com.polytron.auctionapp.domain.model

enum class PaymentMethod(val label: String) {
    Cash("Cash"),
    QRIS("QRIS"),
    Credit("Kredit");

    companion object {
        val all: List<PaymentMethod> = entries

        fun fromLabel(label: String): PaymentMethod =
            all.firstOrNull { it.label == label } ?: Cash
    }
}
