package com.polytron.auctionapp.bluetooth

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import androidx.annotation.RequiresPermission
import com.polytron.auctionapp.domain.model.ItemResponse
import com.polytron.auctionapp.printing.ThermalPrintFormatter
import java.io.IOException
import java.io.OutputStream
import java.util.UUID

class BluetoothPrinter {

    private companion object {
        val SERIAL_PORT_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")
        const val SERIAL_PORT_CHANNEL = 1
        const val WRITE_CHUNK_SIZE = 256
        const val WRITE_CHUNK_DELAY_MS = 20L
        const val CLOSE_DELAY_MS = 250L
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun testPrinter(device: BluetoothDevice): Boolean {
        return printBytes(device, ThermalPrintFormatter.buildTestBytes("GKJ Lelang Android"))
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun printBarcodeReceipt(
        device: BluetoothDevice,
        items: List<ItemResponse>,
        orderID: String,
        payment: String
    ): Boolean {
        return runCatching {
            ThermalPrintFormatter.buildPaymentReceiptBytes(
                items = items,
                orderId = orderID,
                paymentMethod = payment
            )
        }.fold(
            onSuccess = { bytes -> printBytes(device, bytes) },
            onFailure = { error ->
                error.printStackTrace()
                false
            }
        )
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun printBarcodeLabel(device: BluetoothDevice, itemName: String, itemCode: String): Boolean {
        val item = ItemResponse(nameItem = itemName, codeItem = itemCode)
        return printBarcodeLabels(device, listOf(item))
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun printBarcodeLabels(device: BluetoothDevice, items: List<ItemResponse>): Boolean {
        return runCatching {
            ThermalPrintFormatter.buildItemLabelBytes(items)
        }.fold(
            onSuccess = { bytes -> printBytes(device, bytes) },
            onFailure = { error ->
                error.printStackTrace()
                false
            }
        )
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun printAuctionReceipts(
        device: BluetoothDevice,
        receipts: List<ThermalPrintFormatter.AuctionReceiptItem>
    ): Boolean {
        return runCatching {
            ThermalPrintFormatter.buildAuctionReceiptBytes(receipts)
        }.fold(
            onSuccess = { bytes -> printBytes(device, bytes) },
            onFailure = { error ->
                error.printStackTrace()
                false
            }
        )
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun printBarcodeAuction(
        device: BluetoothDevice,
        name: String,
        price: String,
        code: String,
        itemName: String
    ): Boolean {
        val receipt = ThermalPrintFormatter.AuctionReceiptItem(
            buyerName = name,
            basePrice = null,
            auctionPrice = normalizeRupiahInput(price),
            itemName = itemName,
            itemCode = code
        )
        return printBytes(device, ThermalPrintFormatter.buildAuctionBuyerSlipBytes(receipt))
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun printBarcodeAuctionItems(
        device: BluetoothDevice,
        name: String,
        price: String,
        itemName: String
    ): Boolean {
        val receipt = ThermalPrintFormatter.AuctionReceiptItem(
            buyerName = name,
            basePrice = null,
            auctionPrice = normalizeRupiahInput(price),
            itemName = itemName,
            itemCode = ""
        )
        return printBytes(device, ThermalPrintFormatter.buildAuctionItemSlipBytes(receipt))
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun printBytes(device: BluetoothDevice, bytes: ByteArray): Boolean {
        if (bytes.isEmpty()) return false

        return try {
            device.openPrinterSocket().use { socket ->
                socket.outputStream.use { outputStream ->
                    outputStream.writeChunked(bytes)
                }
            }
            true
        } catch (error: Exception) {
            error.printStackTrace()
            false
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun BluetoothDevice.openPrinterSocket(): BluetoothSocket {
        val uuid = printerUuid()
        val errors = mutableListOf<Throwable>()

        val secureSocket = connectSocket { createRfcommSocketToServiceRecord(uuid) }
        secureSocket.socket?.let { return it }
        errors.add(secureSocket.error ?: IOException("Secure RFCOMM gagal"))

        val insecureSocket = connectSocket { createInsecureRfcommSocketToServiceRecord(uuid) }
        insecureSocket.socket?.let { return it }
        errors.add(insecureSocket.error ?: IOException("Insecure RFCOMM gagal"))

        val fallbackSocket = connectSocket { createRfcommSocketChannel(SERIAL_PORT_CHANNEL) }
        fallbackSocket.socket?.let { return it }
        errors.add(fallbackSocket.error ?: IOException("Fallback RFCOMM channel gagal"))

        throw IOException("Tidak bisa terhubung ke printer Bluetooth ${safeDeviceLabel()}.").apply {
            errors.forEach(::addSuppressed)
        }
    }

    private data class SocketAttempt(
        val socket: BluetoothSocket?,
        val error: Throwable?
    )

    private fun connectSocket(createSocket: () -> BluetoothSocket): SocketAttempt {
        val socket = try {
            createSocket()
        } catch (error: Exception) {
            return SocketAttempt(socket = null, error = error)
        }

        return try {
            socket.connect()
            SocketAttempt(socket = socket, error = null)
        } catch (error: Exception) {
            runCatching { socket.close() }
            SocketAttempt(socket = null, error = error)
        }
    }

    private fun BluetoothDevice.createRfcommSocketChannel(channel: Int): BluetoothSocket {
        val method = javaClass.getMethod("createRfcommSocket", Integer.TYPE)
        return method.invoke(this, channel) as BluetoothSocket
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun BluetoothDevice.safeDeviceLabel(): String {
        return name?.takeIf { it.isNotBlank() } ?: address ?: "tidak dikenal"
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun BluetoothDevice.printerUuid(): UUID {
        return uuids
            ?.map { it.uuid }
            ?.firstOrNull { it == SERIAL_PORT_UUID }
            ?: SERIAL_PORT_UUID
    }

    private fun OutputStream.writeChunked(bytes: ByteArray) {
        var offset = 0
        while (offset < bytes.size) {
            val length = minOf(WRITE_CHUNK_SIZE, bytes.size - offset)
            write(bytes, offset, length)
            flush()
            offset += length
            if (offset < bytes.size) {
                Thread.sleep(WRITE_CHUNK_DELAY_MS)
            }
        }
        flush()
        Thread.sleep(CLOSE_DELAY_MS)
    }

    private fun normalizeRupiahInput(value: String): String {
        return value.filter { char -> char.isDigit() || char == '-' }.ifBlank { value }
    }
}
