package com.polytron.auctionapp.printing

import com.polytron.auctionapp.domain.model.ItemResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ThermalPrintFormatterTest {
    @Test
    fun auctionReceiptContainsDesktopReceiptText() {
        val text = ThermalPrintFormatter.buildAuctionReceiptBytes(
            listOf(
                ThermalPrintFormatter.AuctionReceiptItem(
                    buyerName = "Budi",
                    basePrice = "100000",
                    auctionPrice = "125000",
                    itemName = "Vas Antik",
                    itemCode = "ITM-001"
                )
            )
        ).decodeToString()

        assertTrue(text.contains("Budi\n\n"))
        assertTrue(text.contains("Barang\n\n"))
        assertTrue(text.contains("Barang : Vas Antik\n"))
        assertTrue(text.contains("Harga  : Rp 125.000\n\n"))
        assertTrue(text.contains("ITM-001"))
    }

    @Test
    fun paymentReceiptFormatsRowsAndTotal() {
        val text = ThermalPrintFormatter.buildPaymentReceiptBytes(
            items = listOf(
                ItemResponse(nameItem = "Barang Satu", price = "10000"),
                ItemResponse(nameItem = "Barang Dua", price = "25000")
            ),
            orderId = "Order-ABC123",
            paymentMethod = "Tunai"
        ).decodeToString()

        assertEquals(2, text.split("NOTA PEMBAYARAN\n").size - 1)
        assertTrue(text.contains("PELANGGAN\n"))
        assertTrue(text.contains("AUDIT\n"))
        assertTrue(text.contains("Order-ABC123"))
        assertTrue(text.contains("Barang Satu"))
        assertTrue(text.contains("Rp 10.000"))
        assertTrue(text.contains("Total       : Rp 35.000\n"))
        assertTrue(text.contains("LUNAS\n"))
        assertTrue(text.contains("Tunai"))
    }

    @Test
    fun itemLabelRequiresAtLeastOneItem() {
        assertFailsWith<IllegalArgumentException> {
            ThermalPrintFormatter.buildItemLabelBytes(emptyList())
        }
    }
}
