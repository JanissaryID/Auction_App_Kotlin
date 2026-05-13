package com.polytron.auctionapp.bluetooth

object EscPosCommands {

    private const val ESC = 0x1B.toByte()
    private const val GS = 0x1D.toByte()

    // ==== ALIGN ====
    val alignLeft = byteArrayOf(ESC, 0x61, 0x00)
    val alignCenter = byteArrayOf(ESC, 0x61, 0x01)
//    val alignRight = byteArrayOf(ESC, 0x61, 0x02)

    // ==== FONT SIZE ====
    val fontNormal = byteArrayOf(ESC, 0x21, 0x00)
    val fontBig = byteArrayOf(ESC, 0x21, 0x30)

    // ==== QR CODE ====
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

    // ==== NEW LINE ====
    fun newLine(count: Int = 1): ByteArray = "\n".repeat(count).toByteArray()

    // ==== STRIP GARIS ====
    fun strip(width: Int = 32): ByteArray = "-".repeat(width).toByteArray()
}
