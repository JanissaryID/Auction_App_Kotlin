package com.polytron.auctionapp.domain.model

enum class TypeScreenBarcode(val label: String) {
    Payment("Payment"),
    Auction("Auction"),
    TakeItems("TakeItems");

    companion object {
        val all: List<TypeScreenBarcode> = entries

        fun fromLabel(label: String): TypeScreenBarcode =
            all.firstOrNull { it.label == label } ?: Auction
    }
}
