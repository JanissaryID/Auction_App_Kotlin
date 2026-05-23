package com.polytron.auctionapp.desktop.utils

import com.fazecast.jSerialComm.SerialPort
import com.polytron.auctionapp.domain.model.ItemResponse
import com.polytron.auctionapp.printing.ThermalPrintFormatter
import java.io.OutputStream
import javax.print.DocFlavor
import javax.print.PrintServiceLookup
import javax.print.SimpleDoc
import javax.print.attribute.HashPrintRequestAttributeSet

object DesktopThermalPrinter {
    private const val WINDOWS_PRINTER_PREFIX = "Windows printer: "
    private const val SERIAL_PRINTER_PREFIX = "Bluetooth/Serial: "
    private const val SERIAL_BAUD_RATE = 9600
    private const val WRITE_CHUNK_SIZE = 256
    private const val WRITE_CHUNK_DELAY_MS = 20L
    private const val CLOSE_DELAY_MS = 250L

    fun availablePrinterNames(): List<String> {
        val windowsPrinters = PrintServiceLookup.lookupPrintServices(null, null)
            .map { it.name }
            .distinct()
            .sorted()
            .map { "$WINDOWS_PRINTER_PREFIX$it" }

        val serialPrinters = runCatching {
            SerialPort.getCommPorts()
                .map { it.toPrinterLabel() }
                .distinct()
                .sorted()
        }.getOrDefault(emptyList())

        return windowsPrinters + serialPrinters
    }

    fun defaultPrinterName(): String? {
        return PrintServiceLookup.lookupDefaultPrintService()
            ?.name
            ?.let { "$WINDOWS_PRINTER_PREFIX$it" }
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
        if (printerName.startsWith(SERIAL_PRINTER_PREFIX)) {
            printSerialPort(printerName.toSerialPortName(), bytes)
            return
        }

        val printServiceName = printerName.removePrefix(WINDOWS_PRINTER_PREFIX)
        val service = PrintServiceLookup.lookupPrintServices(null, null)
            .firstOrNull { it.name == printServiceName }
            ?: error("Printer '$printServiceName' tidak ditemukan.")

        val doc = SimpleDoc(bytes, DocFlavor.BYTE_ARRAY.AUTOSENSE, null)
        service.createPrintJob().print(doc, HashPrintRequestAttributeSet())
    }

    private fun printSerialPort(portName: String, bytes: ByteArray) {
        val port = SerialPort.getCommPorts()
            .firstOrNull { it.systemPortName.equals(portName, ignoreCase = true) }
            ?: error("Port Bluetooth '$portName' tidak ditemukan.")

        port.setComPortParameters(
            SERIAL_BAUD_RATE,
            8,
            SerialPort.ONE_STOP_BIT,
            SerialPort.NO_PARITY
        )
        port.setFlowControl(SerialPort.FLOW_CONTROL_DISABLED)
        port.setComPortTimeouts(SerialPort.TIMEOUT_WRITE_BLOCKING, 0, 10_000)

        if (!port.openPort(1_000)) {
            error("Port Bluetooth '$portName' tidak bisa dibuka. Pastikan printer sudah dipasangkan dan tidak sedang dipakai aplikasi lain.")
        }

        try {
            port.outputStream.use { outputStream ->
                outputStream.writeChunked(bytes)
            }
        } finally {
            port.closePort()
        }
    }

    private fun SerialPort.toPrinterLabel(): String {
        val description = listOf(descriptivePortName, portDescription)
            .firstOrNull { it.isNotBlank() && !it.equals(systemPortName, ignoreCase = true) }
            ?.let { " - $it" }
            .orEmpty()
        return "$SERIAL_PRINTER_PREFIX$systemPortName$description"
    }

    private fun String.toSerialPortName(): String {
        return removePrefix(SERIAL_PRINTER_PREFIX)
            .substringBefore(" - ")
            .trim()
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
}
