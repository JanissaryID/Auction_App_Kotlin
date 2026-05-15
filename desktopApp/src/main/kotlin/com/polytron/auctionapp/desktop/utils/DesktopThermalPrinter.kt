package com.polytron.auctionapp.desktop.utils

import com.polytron.auctionapp.domain.model.ItemResponse
import com.polytron.auctionapp.utils.formatRupiah
import java.io.ByteArrayOutputStream
import javax.print.DocFlavor
import javax.print.PrintServiceLookup
import javax.print.SimpleDoc
import javax.print.attribute.HashPrintRequestAttributeSet

object DesktopThermalPrinter {
    private const val PAPER_WIDTH = 32
    private const val MAX_QR_CODE_CHARS = 255

    data class AuctionReceiptItem(
        val buyerName: String,
        val basePrice: String?,
        val auctionPrice: String?,
        val itemName: String,
        val itemCode: String
    )

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
        val bytes = buildBytes {
            write(EscPos.init)
            write(EscPos.alignCenter)
            write(EscPos.fontBig)
            writeText("PRINTER OK\n")
            write(EscPos.fontNormal)
            writeText("Auction App Desktop\n")
            write(EscPos.strip())
            write(EscPos.newLine(4))
        }
        printBytes(printerName, bytes)
    }

    fun printItemLabels(printerName: String, items: List<ItemResponse>) {
        require(items.isNotEmpty()) { "Tidak ada barang untuk dicetak." }

        val bytes = buildBytes {
            items.forEach { item ->
                val itemName = item.nameItem.orEmpty()
                val itemCode = item.codeItem.orEmpty()
                val basePrice = formatRupiah(item.basePrice)
                val maxPrice = formatRupiah(item.maxPrice)

                write(EscPos.init)
                write(EscPos.alignCenter)
                writeText(itemName)
                write(EscPos.newLine(1))
                write(EscPos.fontNormal)
                writeText("Dasar : $basePrice\n")
                writeText("Maks  : $maxPrice\n")
                write(EscPos.newLine(1))
                writeQrCode(itemCode)
                write(EscPos.strip())
                write(EscPos.newLine(2))
            }
        }

        printBytes(printerName, bytes)
    }

    fun printAuctionReceipts(printerName: String, receipts: List<AuctionReceiptItem>) {
        require(receipts.isNotEmpty()) { "Tidak ada nota lelang untuk dicetak." }

        val bytes = buildBytes {
            receipts.forEach { receipt ->
                writeBarcodeAuction(receipt)
                writeBarcodeAuctionItems(receipt)
            }
        }

        printBytes(printerName, bytes)
    }

    fun printPaymentReceipt(
        printerName: String,
        items: List<ItemResponse>,
        orderId: String,
        paymentMethod: String
    ) {
        require(items.isNotEmpty()) { "Tidak ada barang pembayaran untuk dicetak." }

        val total = items.sumOf { it.price?.toLongOrNull() ?: 0L }
        val bytes = buildBytes {
            write(EscPos.init)
            write(EscPos.alignCenter)
            write(EscPos.fontBig)
            writeText("NOTA PEMBAYARAN\n")
            write(EscPos.fontNormal)
            writeText("\n$orderId\n\n")

            write(EscPos.alignLeft)
            items.forEach { item ->
                val name = item.nameItem.orEmpty().ifBlank { "-" }.take(20).padEnd(20)
                val price = formatRupiah(item.price).take(11).padStart(11)
                writeText("$name $price\n")
            }

            write(EscPos.strip())
            writeLabeledValue("Total", formatRupiah(total.toString()))
            write(EscPos.newLine())

            write(EscPos.alignCenter)
            write(EscPos.fontBig)
            writeText("LUNAS\n")
            write(EscPos.fontNormal)
            writeText("$paymentMethod\n\n")
            writeQrCode(orderId)
            write(EscPos.strip())
            write(EscPos.newLine(4))
        }

        printBytes(printerName, bytes)
    }

    private fun printBytes(printerName: String, bytes: ByteArray) {
        val service = PrintServiceLookup.lookupPrintServices(null, null)
            .firstOrNull { it.name == printerName }
            ?: error("Printer '$printerName' tidak ditemukan.")

        val doc = SimpleDoc(bytes, DocFlavor.BYTE_ARRAY.AUTOSENSE, null)
        service.createPrintJob().print(doc, HashPrintRequestAttributeSet())
    }

    private fun ByteArrayOutputStream.writeBarcodeAuction(receipt: AuctionReceiptItem) {
        write(EscPos.init)
        write(EscPos.alignCenter)
        write(EscPos.fontBig)
        writeText("${receipt.buyerName}\n\n")

        write(EscPos.alignLeft)
        write(EscPos.fontNormal)
        writeText("Barang : ${receipt.itemName}\n")
        writeText("Harga  : ${formatRupiah(receipt.auctionPrice)}\n\n")

        write(EscPos.alignCenter)
        writeQrCode(receipt.itemCode)
        write(EscPos.strip())
        write(EscPos.newLine(2))
    }

    private fun ByteArrayOutputStream.writeBarcodeAuctionItems(receipt: AuctionReceiptItem) {
        write(EscPos.init)
        write(EscPos.alignCenter)
        write(EscPos.fontBig)
        writeText("Barang\n\n")

        write(EscPos.fontBig)
        writeText("${receipt.buyerName}\n\n")

        write(EscPos.alignLeft)
        write(EscPos.fontNormal)
        writeText("Barang : ${receipt.itemName}\n")
        writeText("Harga  : ${formatRupiah(receipt.auctionPrice)}\n\n")

        write(EscPos.strip())
        write(EscPos.newLine(2))
    }

    private fun ByteArrayOutputStream.writeLabeledValue(label: String, value: String?) {
        val cleanValue = value.orEmpty().ifBlank { "-" }
        val prefix = "${label.padEnd(12)}: "
        val lineWidth = (PAPER_WIDTH - prefix.length).coerceAtLeast(8)
        val chunks = cleanValue.chunked(lineWidth).ifEmpty { listOf("-") }

        chunks.forEachIndexed { index, chunk ->
            if (index == 0) {
                writeText(prefix)
            } else {
                writeText(" ".repeat(prefix.length))
            }
            writeText("$chunk\n")
        }
    }

    private fun ByteArrayOutputStream.writeWrapped(text: String, width: Int) {
        text.ifBlank { "-" }
            .chunked(width)
            .forEach { writeText("$it\n") }
    }

    private fun ByteArrayOutputStream.writeQrCode(code: String) {
        val cleanCode = code.trim().ifBlank { "-" }.take(MAX_QR_CODE_CHARS)
        val data = cleanCode.toByteArray(Charsets.UTF_8)

        write(EscPos.alignCenter)
        write(EscPos.qrModel)
        write(EscPos.qrSize())
        write(EscPos.qrErrorCorrection)
        write(EscPos.qrStoreData(data))
        write(EscPos.qrPrint)
        write(EscPos.newLine())
        writeText(cleanCode)
        write(EscPos.newLine(2))
    }

    private fun ByteArrayOutputStream.writeText(text: String) {
        write(text.toByteArray(Charsets.UTF_8))
    }

    private fun buildBytes(block: ByteArrayOutputStream.() -> Unit): ByteArray {
        return ByteArrayOutputStream().apply(block).toByteArray()
    }

    private object EscPos {
        private const val ESC = 0x1B.toByte()
        private const val GS = 0x1D.toByte()

        val init = byteArrayOf(ESC, 0x40)
        val alignLeft = byteArrayOf(ESC, 0x61, 0x00)
        val alignCenter = byteArrayOf(ESC, 0x61, 0x01)
        val fontNormal = byteArrayOf(ESC, 0x21, 0x00)
        val fontBig = byteArrayOf(ESC, 0x21, 0x30)
        val qrModel = byteArrayOf(GS, 0x28, 0x6B, 0x04, 0x00, 0x31, 0x41, 0x32, 0x00)
        val qrErrorCorrection = byteArrayOf(GS, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x45, 0x31)
        val qrPrint = byteArrayOf(GS, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x51, 0x30)

        fun qrSize(size: Int = 6): ByteArray =
            byteArrayOf(GS, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x43, size.coerceIn(1, 16).toByte())

        fun qrStoreData(data: ByteArray): ByteArray {
            val length = data.size + 3
            val pL = (length and 0xFF).toByte()
            val pH = ((length shr 8) and 0xFF).toByte()
            return byteArrayOf(GS, 0x28, 0x6B, pL, pH, 0x31, 0x50, 0x30) + data
        }

        fun newLine(count: Int = 1): ByteArray = "\n".repeat(count).toByteArray()
        fun strip(width: Int = PAPER_WIDTH): ByteArray = "-".repeat(width).toByteArray()
    }
}
