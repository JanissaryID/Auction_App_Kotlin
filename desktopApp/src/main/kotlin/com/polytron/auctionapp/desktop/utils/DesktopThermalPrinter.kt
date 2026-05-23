package com.polytron.auctionapp.desktop.utils

import com.polytron.auctionapp.domain.model.ItemResponse
import com.polytron.auctionapp.printing.ThermalPrintFormatter
import javax.print.DocFlavor
import javax.print.PrintServiceLookup
import javax.print.SimpleDoc
import javax.print.attribute.HashPrintRequestAttributeSet

object DesktopThermalPrinter {
    fun availablePrinterNames(): List<String> {
        return PrintServiceLookup.lookupPrintServices(null, null)
            .map { it.name }
            .distinct()
            .sorted()
    }

    fun defaultPrinterName(): String? {
        return PrintServiceLookup.lookupDefaultPrintService()?.name
    }

    fun printTest(printerName: String) {
        printBytes(printerName, ThermalPrintFormatter.buildTestBytes("GKJ Lelang Desktop"))
    }

    fun printItemLabels(printerName: String, items: List<ItemResponse>) {
        printBytes(printerName, ThermalPrintFormatter.buildItemLabelBytes(items))
    }

    fun printAuctionReceipts(
        printerName: String,
        receipts: List<ThermalPrintFormatter.AuctionReceiptItem>
    ) {
        printBytes(printerName, ThermalPrintFormatter.buildAuctionReceiptBytes(receipts))
    }

    fun printPaymentReceipt(
        printerName: String,
        items: List<ItemResponse>,
        orderId: String,
        paymentMethod: String
    ) {
        printBytes(
            printerName,
            ThermalPrintFormatter.buildPaymentReceiptBytes(
                items = items,
                orderId = orderId,
                paymentMethod = paymentMethod
            )
        )
    }

    private fun printBytes(printerName: String, bytes: ByteArray) {
        val service = PrintServiceLookup.lookupPrintServices(null, null)
            .firstOrNull { it.name == printerName }
            ?: error("Printer '$printerName' tidak ditemukan.")

        val doc = SimpleDoc(bytes, DocFlavor.BYTE_ARRAY.AUTOSENSE, null)
        service.createPrintJob().print(doc, HashPrintRequestAttributeSet())
    }
}
