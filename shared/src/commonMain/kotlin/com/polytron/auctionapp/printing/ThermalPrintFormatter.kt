package com.polytron.auctionapp.printing

import com.polytron.auctionapp.domain.model.ItemResponse
import com.polytron.auctionapp.utils.formatRupiah

object ThermalPrintFormatter {
    private const val PAPER_WIDTH = 32
    private const val MAX_QR_CODE_CHARS = 255

    data class AuctionReceiptItem(
        val buyerName: String,
        val basePrice: String?,
        val auctionPrice: String?,
        val itemName: String,
        val itemCode: String
    )

    fun buildTestBytes(appName: String = "GKJ Lelang"): ByteArray = buildBytes {
        write(EscPos.init)
        write(EscPos.alignCenter)
        write(EscPos.fontBig)
        writeText("PRINTER OK\n")
        write(EscPos.fontNormal)
        writeText("$appName\n")
        write(EscPos.strip())
        write(EscPos.newLine(4))
    }

    fun buildItemLabelBytes(items: List<ItemResponse>): ByteArray {
        require(items.isNotEmpty()) { "Tidak ada barang untuk dicetak." }

        return buildBytes {
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
    }

    fun buildAuctionReceiptBytes(receipts: List<AuctionReceiptItem>): ByteArray {
        require(receipts.isNotEmpty()) { "Tidak ada nota lelang untuk dicetak." }

        return buildBytes {
            receipts.forEach { receipt ->
                writeAuctionBuyerSlip(receipt)
                writeAuctionItemSlip(receipt)
            }
        }
    }

    fun buildAuctionBuyerSlipBytes(receipt: AuctionReceiptItem): ByteArray = buildBytes {
        writeAuctionBuyerSlip(receipt)
    }

    fun buildAuctionItemSlipBytes(receipt: AuctionReceiptItem): ByteArray = buildBytes {
        writeAuctionItemSlip(receipt)
    }

    fun buildPaymentReceiptBytes(
        items: List<ItemResponse>,
        orderId: String,
        paymentMethod: String
    ): ByteArray {
        require(items.isNotEmpty()) { "Tidak ada barang pembayaran untuk dicetak." }

        val total = items.sumOf { it.price?.toLongOrNull() ?: 0L }
        return buildBytes {
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
    }

    private fun ByteWriter.writeAuctionBuyerSlip(receipt: AuctionReceiptItem) {
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

    private fun ByteWriter.writeAuctionItemSlip(receipt: AuctionReceiptItem) {
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

    private fun ByteWriter.writeLabeledValue(label: String, value: String?) {
        val cleanValue = value.orEmpty().ifBlank { "-" }
        val prefix = "${label.padEnd(12)}: "
        val lineWidth = (PAPER_WIDTH - prefix.length).coerceAtLeast(8)
        val chunks = cleanValue.chunked(lineWidth).ifEmpty { listOf("-") }

        chunks.forEachIndexed { index, chunk ->
            writeText(if (index == 0) prefix else " ".repeat(prefix.length))
            writeText("$chunk\n")
        }
    }

    private fun ByteWriter.writeQrCode(code: String) {
        val cleanCode = code.trim().ifBlank { "-" }.take(MAX_QR_CODE_CHARS)
        val data = cleanCode.encodeToByteArray()

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

    private fun buildBytes(block: ByteWriter.() -> Unit): ByteArray {
        return ByteWriter().apply(block).toByteArray()
    }

    private class ByteWriter {
        private val bytes = mutableListOf<Byte>()

        fun write(value: ByteArray) {
            value.forEach(bytes::add)
        }

        fun writeText(text: String) {
            write(text.encodeToByteArray())
        }

        fun toByteArray(): ByteArray = ByteArray(bytes.size) { index -> bytes[index] }
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

        fun newLine(count: Int = 1): ByteArray = "\n".repeat(count).encodeToByteArray()
        fun strip(width: Int = PAPER_WIDTH): ByteArray = "-".repeat(width).encodeToByteArray()
    }
}
