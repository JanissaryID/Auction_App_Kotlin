package com.polytron.auctionapp.bluetooth

import android.Manifest
import android.bluetooth.BluetoothDevice
import androidx.annotation.RequiresPermission
import com.polytron.auctionapp.bluetooth.EscPosCommands.alignCenter
import com.polytron.auctionapp.bluetooth.EscPosCommands.alignLeft
import com.polytron.auctionapp.bluetooth.EscPosCommands.barcodeHeight
import com.polytron.auctionapp.bluetooth.EscPosCommands.barcodeType
import com.polytron.auctionapp.bluetooth.EscPosCommands.barcodeWidth
import com.polytron.auctionapp.bluetooth.EscPosCommands.fontBig
import com.polytron.auctionapp.bluetooth.EscPosCommands.fontNormal
import com.polytron.auctionapp.bluetooth.EscPosCommands.newLine
import com.polytron.auctionapp.bluetooth.EscPosCommands.showBarcodeText
import com.polytron.auctionapp.bluetooth.EscPosCommands.strip
import com.polytron.auctionapp.model.ItemResponse
import java.io.OutputStream
import java.util.UUID

class BluetoothPrinter {

    private val footer = 32

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

    // === CETAK STRUK PEMBAYARAN DENGAN BARCODE ===
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun printBarcodeReceipt(
        device: BluetoothDevice,
        items: List<ItemResponse>,
        orderID: String,
        payment: String
    ) {
        val uuid = device.uuids?.firstOrNull()?.uuid
            ?: UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")

        try {
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

                    // Barcode
                    os.writeBarcode(orderID)

                    // Footer
                    os.write(strip(footer))
                    os.write(newLine(2))
                    os.flush()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // === CETAK LABEL BARCODE SEDERHANA ===
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun printBarcodeLabel(device: BluetoothDevice, itemName: String, itemCode: String) {
        val uuid = device.uuids?.firstOrNull()?.uuid
            ?: UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")

        try {
            device.createRfcommSocketToServiceRecord(uuid).use { socket ->
                socket.connect()
                socket.outputStream.use { os ->
                    os.write(alignCenter)
                    os.write(itemName.toByteArray())
                    os.write(newLine(2))
                    os.writeBarcode(itemCode)
                    os.write(strip(footer))
                    os.write(newLine(2))
                    os.flush()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // === CETAK BARCODE UNTUK LELANG ===
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
                    os.writeBarcode(code)
                    os.write(strip(footer))
                    os.write(newLine(2))
                    os.flush()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // === CETAK LABEL LELANG TANPA BARCODE ===
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

    // === EXTENSION UNTUK CETAK BARCODE ===
    private fun OutputStream.writeBarcode(code: String) {
        write(alignCenter)
        write(barcodeHeight)
        write(barcodeWidth)
        write(showBarcodeText)
        write(barcodeType)
        write(code.length)
        write(code.toByteArray())
        write(newLine(2))
    }
}

