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

    // ==== BARCODE ====
    val barcodeHeight = byteArrayOf(GS, 0x68, 100)  // Tinggi barcode
    val barcodeWidth = byteArrayOf(GS, 0x77, 3)     // Lebar garis
    val barcodeType = byteArrayOf(GS, 0x6B, 0x49)   // CODE128
    val showBarcodeText = byteArrayOf(GS, 0x48, 0x02)

    // ==== NEW LINE ====
    fun newLine(count: Int = 1): ByteArray = "\n".repeat(count).toByteArray()

    // ==== STRIP GARIS ====
    fun strip(width: Int = 32): ByteArray = "-".repeat(width).toByteArray()
}