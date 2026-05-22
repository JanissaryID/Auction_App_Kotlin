package com.polytron.auctionapp.desktop.utils

import com.polytron.auctionapp.domain.model.ItemResponse
import com.polytron.auctionapp.domain.model.PaymentMethod
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

fun exportItemsToExcelDesktop(
    items: List<ItemResponse>,
    includePaymentDetails: Boolean = true,
    enableColumnFilters: Boolean = false
): Result<String> {
    return try {
        val reportName = if (includePaymentDetails) "Laporan Unduh-Unduh" else "Laporan Barang"
        val fileChooser = JFileChooser().apply {
            dialogTitle = "Simpan Laporan Excel"
            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
            selectedFile = File("${reportName.replace(" ", "_")}_$timestamp.xlsx")
            fileFilter = FileNameExtensionFilter("Excel Files (*.xlsx)", "xlsx")
        }

        val result = fileChooser.showSaveDialog(null)
        if (result != JFileChooser.APPROVE_OPTION) {
            return Result.failure(Exception("Export dibatalkan"))
        }

        var file = fileChooser.selectedFile
        if (!file.name.endsWith(".xlsx")) {
            file = File(file.absolutePath + ".xlsx")
        }

        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Items")

        // Header style
        val headerStyle = workbook.createCellStyle().apply {
            val font = workbook.createFont()
            font.bold = true
            setFont(font)
        }

        // Header
        val headers = if (includePaymentDetails) {
            listOf("Nama", "Kode", "Harga Dasar", "Harga Maks", "Harga Lelang", "Tipe Pembayaran", "Pemenang")
        } else {
            listOf("Nama", "Kode", "Harga Dasar", "Harga Maks", "Harga Lelang")
        }
        val headerRow = sheet.createRow(0)
        headers.forEachIndexed { i, title ->
            headerRow.createCell(i).apply {
                setCellValue(title)
                cellStyle = headerStyle
            }
        }

        // Data
        var totalBase = 0L
        var totalMax = 0L
        var totalPrice = 0L
        val paymentCounts = if (includePaymentDetails) {
            mutableMapOf(
                PaymentMethod.Cash.label to 0,
                PaymentMethod.QRIS.label to 0,
                PaymentMethod.Credit.label to 0
            )
        } else {
            null
        }

        var rowIndex = 1
        items.forEach { item ->
            val row = sheet.createRow(rowIndex++)
            row.createCell(0).setCellValue(item.nameItem ?: "")
            row.createCell(1).setCellValue(item.codeItem ?: "")

            val base = item.basePrice?.toLongOrNull() ?: 0L
            val max = item.maxPrice?.toLongOrNull() ?: 0L
            val price = item.price?.toLongOrNull() ?: 0L

            row.createCell(2).setCellValue(base.toDouble())
            row.createCell(3).setCellValue(max.toDouble())
            row.createCell(4).setCellValue(price.toDouble())
            if (includePaymentDetails) {
                row.createCell(5).setCellValue(item.typePayment ?: "")
                row.createCell(6).setCellValue(item.buyer ?: "")
            }

            totalBase += base
            totalMax += max
            totalPrice += price

            paymentCounts?.let { counts ->
                val method = item.typePayment ?: ""
                counts[method] = (counts[method] ?: 0) + 1
            }
        }
        val lastItemRow = rowIndex - 1

        // Total row
        val totalRow = sheet.createRow(rowIndex++)
        totalRow.createCell(0).apply {
            setCellValue("TOTAL")
            cellStyle = headerStyle
        }
        totalRow.createCell(2).setCellValue(totalBase.toDouble())
        totalRow.createCell(3).setCellValue(totalMax.toDouble())
        totalRow.createCell(4).setCellValue(totalPrice.toDouble())

        if (includePaymentDetails && paymentCounts != null) {
            rowIndex++
            val qrisRow = sheet.createRow(rowIndex++)
            qrisRow.createCell(0).setCellValue("Jumlah QRIS")
            qrisRow.createCell(1).setCellValue((paymentCounts[PaymentMethod.QRIS.label] ?: 0).toDouble())

            val cashRow = sheet.createRow(rowIndex++)
            cashRow.createCell(0).setCellValue("Jumlah Cash")
            cashRow.createCell(1).setCellValue((paymentCounts[PaymentMethod.Cash.label] ?: 0).toDouble())

            val creditRow = sheet.createRow(rowIndex++)
            creditRow.createCell(0).setCellValue("Jumlah Kredit")
            creditRow.createCell(1).setCellValue((paymentCounts[PaymentMethod.Credit.label] ?: 0).toDouble())
        }

        if (enableColumnFilters && lastItemRow >= 1) {
            sheet.setAutoFilter(CellRangeAddress(0, lastItemRow, 0, headers.lastIndex))
            sheet.createFreezePane(0, 1)
        }

        // Auto-size columns
        val widths = if (includePaymentDetails) {
            listOf(25, 15, 15, 15, 15, 15, 25)
        } else {
            listOf(25, 15, 15, 15, 15)
        }
        widths.forEachIndexed { i, width -> sheet.setColumnWidth(i, width * 256) }

        // Write
        file.outputStream().use { outputStream ->
            workbook.write(outputStream)
        }
        workbook.close()

        Result.success("Excel disimpan di: ${file.absolutePath}")
    } catch (e: Exception) {
        e.printStackTrace()
        Result.failure(e)
    }
}
