package com.polytron.auctionapp.bluetooth

import android.Manifest
import android.bluetooth.BluetoothDevice
import androidx.annotation.RequiresPermission
import com.polytron.auctionapp.bluetooth.EscPosCommands.alignCenter
import com.polytron.auctionapp.bluetooth.EscPosCommands.alignLeft
import com.polytron.auctionapp.bluetooth.EscPosCommands.fontBig
import com.polytron.auctionapp.bluetooth.EscPosCommands.fontNormal
import com.polytron.auctionapp.bluetooth.EscPosCommands.newLine
import com.polytron.auctionapp.bluetooth.EscPosCommands.qrErrorCorrection
import com.polytron.auctionapp.bluetooth.EscPosCommands.qrModel
import com.polytron.auctionapp.bluetooth.EscPosCommands.qrPrint
import com.polytron.auctionapp.bluetooth.EscPosCommands.qrSize
import com.polytron.auctionapp.bluetooth.EscPosCommands.qrStoreData
import com.polytron.auctionapp.bluetooth.EscPosCommands.strip
import com.polytron.auctionapp.domain.model.ItemResponse
import java.io.OutputStream
import java.util.UUID

class BluetoothPrinter {

    private val footer = 32

    private companion object {
        const val MAX_QR_CODE_CHARS = 255
    }

    // === CETAK TEST PRINTER ===
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun testPrinter(device: BluetoothDevice): Boolean {
        val uuid = device.uuids?.firstOrNull()?.uuid
            ?: UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")

        return try {
            device.createRfcommSocketToServiceRecord(uuid).use { socket ->
                socket.connect()
                socket.outputStream.use { os ->
                    os.write(newLine(5))
                    os.write(alignCenter)
                    os.write("Printer OK\n".toByteArray(Charsets.UTF_8))
                    os.write(newLine(5))
                    os.flush()
                }
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    // === CETAK STRUK PEMBAYARAN DENGAN QR CODE ===
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun printBarcodeReceipt(
        device: BluetoothDevice,
        items: List<ItemResponse>,
        orderID: String,
        payment: String
    ): Boolean {
        val uuid = device.uuids?.firstOrNull()?.uuid
            ?: UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")

        return try {
            device.createRfcommSocketToServiceRecord(uuid).use { socket ->
                socket.connect()
                socket.outputStream.use { os ->
                    // Header nota
                    os.write(alignCenter)
                    os.write(fontBig)
                    os.write("NOTA PEMBAYARAN\n".toByteArray())

                    // Order ID
                    os.write(fontNormal)
                    os.write("\n$orderID\n\n".toByteArray())

                    // Detail barang
                    os.write(alignLeft)
                    var total = 0
                    items.forEach {
                        val name = it.nameItem.orEmpty().padEnd(20, ' ').take(20)
                        val priceInt = it.price?.toIntOrNull() ?: 0
                        total += priceInt
                        val price = "Rp $priceInt"
                        os.write("%-20s %s\n".format(name, price).toByteArray())
                    }

                    // Total
                    os.write("--------------------------------\n".toByteArray())
                    os.write("%-20s %s\n\n".format("Total", "Rp $total").toByteArray())

                    // Status pembayaran
                    os.write(alignCenter)
                    os.write(fontBig)
                    os.write("LUNAS\n".toByteArray())
                    os.write(fontNormal)
                    os.write("$payment\n\n".toByteArray())

                    // QR Code
                    os.writeQrCode(orderID)

                    // Footer
                    os.write(strip(footer))
                    os.write(newLine(2))
                    os.flush()
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // === CETAK LABEL QR CODE SEDERHANA ===
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun printBarcodeLabel(device: BluetoothDevice, itemName: String, itemCode: String) {
        val uuid = device.uuids?.firstOrNull()?.uuid
            ?: UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")

        try {
            device.createRfcommSocketToServiceRecord(uuid).use { socket ->
                socket.connect()
                socket.outputStream.use { os ->
                    os.writeBarcodeLabel(itemName, itemCode)
                    os.flush()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // === CETAK BANYAK LABEL QR CODE DALAM SATU KONEKSI ===
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun printBarcodeLabels(device: BluetoothDevice, items: List<ItemResponse>): Boolean {
        if (items.isEmpty()) return false

        val uuid = device.uuids?.firstOrNull()?.uuid
            ?: UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")

        return try {
            device.createRfcommSocketToServiceRecord(uuid).use { socket ->
                socket.connect()
                socket.outputStream.use { os ->
                    items.forEach { item ->
                        os.writeBarcodeLabel(
                            itemName = item.nameItem.orEmpty(),
                            itemCode = item.codeItem.orEmpty()
                        )
                    }
                    os.flush()
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // === CETAK QR CODE UNTUK LELANG ===
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
            device.createRfcommSocketToServiceRecord(uuid).use { socket ->
                socket.connect()
                socket.outputStream.use { os ->
                    os.write(alignCenter)
                    os.write(fontBig)
                    os.write("$name\n\n".toByteArray())

                    os.write(alignLeft)
                    os.write(fontNormal)
                    os.write("Barang : $itemName\n".toByteArray())
                    os.write("Harga  : $price\n\n".toByteArray())

                    os.write(alignCenter)
                    os.writeQrCode(code)
                    os.write(strip(footer))
                    os.write(newLine(2))
                    os.flush()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // === CETAK LABEL LELANG TANPA QR CODE ===
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
            device.createRfcommSocketToServiceRecord(uuid).use { socket ->
                socket.connect()
                socket.outputStream.use { os ->
                    os.write(alignCenter)
                    os.write(fontBig)
                    os.write("Barang\n\n".toByteArray())

                    os.write(fontBig)
                    os.write("$name\n\n".toByteArray())

                    os.write(alignLeft)
                    os.write(fontNormal)
                    os.write("Barang : $itemName\n".toByteArray())
                    os.write("Harga  : $price\n\n".toByteArray())

                    os.write(strip(footer))
                    os.write(newLine(2))
                    os.flush()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // === EXTENSION UNTUK CETAK QR CODE ===
    private fun OutputStream.writeQrCode(code: String) {
        val cleanCode = code.trim().ifBlank { "-" }.take(MAX_QR_CODE_CHARS)
        val data = cleanCode.toByteArray(Charsets.UTF_8)

        write(alignCenter)
        write(qrModel)
        write(qrSize())
        write(qrErrorCorrection)
        write(qrStoreData(data))
        write(qrPrint)
        write(newLine())
        write(cleanCode.toByteArray(Charsets.UTF_8))
        write(newLine(2))
    }

    private fun OutputStream.writeBarcodeLabel(itemName: String, itemCode: String) {
        write(alignCenter)
        write(itemName.ifBlank { "-" }.toByteArray(Charsets.UTF_8))
        write(newLine(2))
        writeQrCode(itemCode)
        write(strip(footer))
        write(newLine(2))
    }
}
