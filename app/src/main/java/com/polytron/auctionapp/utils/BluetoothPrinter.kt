package com.polytron.auctionapp.utils

import android.Manifest
import android.bluetooth.BluetoothDevice
import androidx.annotation.RequiresPermission
import com.polytron.auctionapp.model.ItemResponse
import java.io.OutputStream
import java.util.UUID

class BluetoothPrinter() {

    private val footer = 32

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun testPrinter(device: BluetoothDevice): Boolean {
        val uuid = device.uuids?.firstOrNull()?.uuid
            ?: UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")

        return try {
            val socket = device.createRfcommSocketToServiceRecord(uuid)
            socket.connect()

            val outputStream = socket.outputStream

            val esc = 0x1B.toByte()
            val alignCenter = byteArrayOf(esc, 0x61, 0x01) // ESC a 1 -> center
            val newLine = "\n".repeat(5) // Spasi bawah untuk sobekan

            outputStream.write(newLine.toByteArray())
            outputStream.write(alignCenter)
            outputStream.write("Printer OK\n".toByteArray(charset("UTF-8")))
            outputStream.write(newLine.toByteArray())
            outputStream.flush()

            socket.close()
//            println("Printer berhasil diuji dan mencetak.")
            true
        } catch (e: Exception) {
//            println("Gagal menghubungkan ke printer: ${e.message}")
            false
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun printBarcodeReceipt(
        device: BluetoothDevice,
        item: List<ItemResponse>,
        orderID: String,
        payment: String
    ) {
        val uuid = device.uuids?.firstOrNull()?.uuid
            ?: UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")

        try {
            val socket = device.createRfcommSocketToServiceRecord(uuid)
            socket.connect()

            val outputStream: OutputStream = socket.outputStream

            // ESC/POS Command shortcuts
            val esc = 0x1B.toByte()
            val alignLeft = byteArrayOf(esc, 0x61, 0x00)
            val alignCenter = byteArrayOf(esc, 0x61, 0x01)
            val fontNormal = byteArrayOf(esc, 0x21, 0x00)
            val fontBig = byteArrayOf(esc, 0x21, 0x30)

            // Cetak Judul: NOTA PEMBAYARAN
            outputStream.write(alignCenter)
            outputStream.write(fontBig)
            outputStream.write("NOTA PEMBAYARAN\n".toByteArray())

            // Cetak Order ID
            val orderId = orderID
            outputStream.write(fontNormal)
            outputStream.write("\n$orderId\n\n".toByteArray())

            // Cetak detail barang
            outputStream.write(alignLeft)
            var total = 0
            item.forEach {
                val name = it.nameItem.orEmpty().padEnd(20, ' ').take(20)

                val priceInt = it.price?.toIntOrNull() ?: 0
                val price = "Rp $priceInt"
                total += priceInt

                val line = "%-20s %s".format(name, price)
                outputStream.write("$line\n".toByteArray())
            }

            // Garis pemisah
            outputStream.write("--------------------------------\n".toByteArray())

            // Cetak Total
            val totalStr = "Rp $total"
            val totalLine = "%-20s %s".format("Total", totalStr)
            outputStream.write("$totalLine\n\n".toByteArray())

            // LUNAS besar tengah
            outputStream.write(alignCenter)
            outputStream.write(fontBig)
            outputStream.write("LUNAS\n".toByteArray())

            // Tipe pembayaran
            outputStream.write(fontNormal)
            outputStream.write("$payment\n\n".toByteArray())

            // Cetak Barcode (pakai Order ID)
            outputStream.writeBarcodeOrder(orderId)

            // Footer
            val strip = "-".repeat(footer)
            outputStream.write("$strip\n\n".toByteArray())

            outputStream.flush()
            Thread.sleep(1000)
            socket.close()

//            println("Struk berhasil dicetak.")
        } catch (e: Exception) {
            e.printStackTrace()
//            println("Gagal mencetak struk: ${e.message}")
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun printBarcodeLabel(device: BluetoothDevice, itemName: String, itemCode: String) {
        val uuid = device.uuids?.firstOrNull()?.uuid
            ?: UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")

        try {
            val socket = device.createRfcommSocketToServiceRecord(uuid)
            socket.connect()

            val outputStream: OutputStream = socket.outputStream
            val esc = 0x1B.toByte()
            val alignCenter = byteArrayOf(esc, 0x61, 0x01)
            val alignLeft = byteArrayOf(esc, 0x61, 0x00)

            outputStream.write(alignLeft)

            outputStream.write(alignCenter)
            outputStream.write(itemName.toByteArray())
            outputStream.write("\n\n".toByteArray())

            outputStream.writeBarcode(itemCode)

            // Kembali ke mode teks biasa
            outputStream.write(alignLeft)

            // Cetak tanda garis sobek
            val strip = "-".repeat(footer)
            outputStream.write("$strip\n\n".toByteArray())

            outputStream.flush()
            socket.close()

//            println("Label barcode berhasil dicetak.")
        } catch (e: Exception) {
            e.printStackTrace()
//            println("Gagal mencetak barcode: ${e.message}")
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun printBarcodeAuction(
        device: BluetoothDevice,
        name: String,
        price: String,
        code: String,
        itemName: String
    ) {
        val uuid = device.uuids?.firstOrNull()?.uuid
            ?: UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")

        try {
            val socket = device.createRfcommSocketToServiceRecord(uuid)
            socket.connect()

            val outputStream: OutputStream = socket.outputStream

            // ESC/POS Command shortcuts
            val esc = 0x1B.toByte()
            val alignLeft = byteArrayOf(esc, 0x61, 0x00)
            val alignCenter = byteArrayOf(esc, 0x61, 0x01)
            val fontNormal = byteArrayOf(esc, 0x21, 0x00)
            val fontBig = byteArrayOf(esc, 0x21, 0x30)

            // Print header (centered & big)
            outputStream.write(alignCenter)
            outputStream.write(fontBig)
            outputStream.write("$name\n\n".toByteArray())

            // Print item info (left & normal font)
            outputStream.write(alignLeft)
            outputStream.write(fontNormal)
            outputStream.write("Barang : $itemName\n".toByteArray())
            outputStream.write("Harga  : $price\n\n".toByteArray())

            // Print barcode (centered)
            outputStream.write(alignCenter)
            outputStream.writeBarcode(code) // <-- pastikan extension function `writeBarcode` tersedia

            // Print strip separator
            outputStream.write(alignLeft)
            val strip = "-".repeat(footer)
            outputStream.write("\n$strip\n\n".toByteArray())

            // Finalize
            outputStream.flush()
            Thread.sleep(1000)
            socket.close()

//            println("Label barcode berhasil dicetak.")
        } catch (e: Exception) {
            e.printStackTrace()
//            println("Gagal mencetak barcode: ${e.message}")
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun printBarcodeAuctionItems(
        device: BluetoothDevice,
        name: String,
        price: String,
        itemName: String
    ) {
        val uuid = device.uuids?.firstOrNull()?.uuid
            ?: UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")

        try {
            val socket = device.createRfcommSocketToServiceRecord(uuid)
            socket.connect()

            val outputStream: OutputStream = socket.outputStream

            // ESC/POS Command shortcuts
            val esc = 0x1B.toByte()
            val alignLeft = byteArrayOf(esc, 0x61, 0x00)
            val alignCenter = byteArrayOf(esc, 0x61, 0x01)
            val fontNormal = byteArrayOf(esc, 0x21, 0x00)
            val fontBig = byteArrayOf(esc, 0x21, 0x30)

            // Print header (centered & big)
            outputStream.write(alignCenter)
            outputStream.write(fontBig)
            outputStream.write("Barang\n\n".toByteArray())

            // Print header (centered & big)
            outputStream.write(alignCenter)
            outputStream.write(fontBig)
            outputStream.write("$name\n\n".toByteArray())

            // Print item info (left & normal font)
            outputStream.write(alignLeft)
            outputStream.write(fontNormal)
            outputStream.write("Barang : $itemName\n".toByteArray())
            outputStream.write("Harga  : $price\n\n".toByteArray())

            // Print strip separator
            outputStream.write(alignLeft)
            val strip = "-".repeat(footer)
            outputStream.write("\n$strip\n\n".toByteArray())

            // Finalize
            outputStream.flush()
            Thread.sleep(1000)
            socket.close()

//            println("Label barcode berhasil dicetak.")
        } catch (e: Exception) {
            e.printStackTrace()
//            println("Gagal mencetak barcode: ${e.message}")
        }
    }

    private fun OutputStream.writeBarcode(code: String) {
        val esc = 0x1B.toByte()
        val gs = 0x1D.toByte()

        val alignCenter = byteArrayOf(esc, 0x61, 0x01) // ESC a 1 (center)
        val barcodeSelect = byteArrayOf(gs, 0x6B, 0x49) // GS k 73 = CODE128
        val barcodeHeight = byteArrayOf(gs, 0x68, 100)  // Tinggi barcode
        val barcodeWidth = byteArrayOf(gs, 0x77, 3)     // Lebar garis (2–6)
        val showBarcodeText = byteArrayOf(gs, 0x48, 0x02) // Tampilkan text di bawah barcode

        write(alignCenter)
        write(barcodeHeight)
        write(barcodeWidth)
        write(showBarcodeText)

        write(barcodeSelect)
        write(code.length)
        write(code.toByteArray())
        write("\n".toByteArray())

//        write(code.toByteArray()) // Kode di bawah barcode
        write("\n\n".toByteArray())
    }

    private fun OutputStream.writeBarcodeOrder(code: String) {
        val esc = 0x1B.toByte()
        val gs = 0x1D.toByte()

        val alignCenter = byteArrayOf(esc, 0x61, 0x01) // ESC a 1 (center)
        val barcodeType = byteArrayOf(gs, 0x6B, 0x49) // GS k 73 = CODE128
        val barcodeHeight = byteArrayOf(gs, 0x68, 100)  // Barcode height
        val barcodeWidth = byteArrayOf(gs, 0x77, 2)     // Line width (2-6)
        val showBarcodeText = byteArrayOf(gs, 0x48, 0x02) // Show text below

        write(alignCenter)
        write(barcodeHeight)
        write(barcodeWidth)
        write(showBarcodeText)

        write(barcodeType)
        write(byteArrayOf(code.length.toByte()))
        write(code.toByteArray())   // Data barcode

        write("\n\n".toByteArray())
    }
}

