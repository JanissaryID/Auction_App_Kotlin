package com.polytron.auctionapp.utils

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.polytron.auctionapp.domain.model.ItemResponse
import com.polytron.auctionapp.domain.model.PaymentMethod
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook

@RequiresApi(Build.VERSION_CODES.Q)
fun exportItemsToExcel(context: Context, items: List<ItemResponse>): Boolean {
    val fileName = "Laporan Unduh-Unduh.xlsx"

    return try {
        val workbook: Workbook = XSSFWorkbook()
        val sheet: Sheet = workbook.createSheet("Items")

        // Header
        val headers = listOf("Name", "Code", "Base Price", "Max Price", "Price", "Type Payment", "Buyer")
        val headerRow = sheet.createRow(0)
        headers.forEachIndexed { i, title ->
            headerRow.createCell(i).setCellValue(title)
        }

        // Data
        var totalBase = 0
        var totalMax = 0
        var totalPrice = 0
        val paymentCounts = mutableMapOf(
            PaymentMethod.Cash.label to 0,
            PaymentMethod.QRIS.label to 0,
            PaymentMethod.Credit.label to 0
        )

        var rowIndex = 1
        items.forEach { item ->
            val row = sheet.createRow(rowIndex++)
            row.createCell(0).setCellValue(item.nameItem ?: "")
            row.createCell(1).setCellValue(item.codeItem ?: "")

            val base = item.basePrice?.toIntOrNull() ?: 0
            val max = item.maxPrice?.toIntOrNull() ?: 0
            val price = item.price?.toIntOrNull() ?: 0

            row.createCell(2).setCellValue(base.toDouble())
            row.createCell(3).setCellValue(max.toDouble())
            row.createCell(4).setCellValue(price.toDouble())
            row.createCell(5).setCellValue(item.typePayment ?: "")
            row.createCell(6).setCellValue(item.buyer ?: "")

            totalBase += base
            totalMax += max
            totalPrice += price

            val method = item.typePayment ?: ""
            paymentCounts[method] = (paymentCounts[method] ?: 0) + 1
        }

        // Total
        val totalRow = sheet.createRow(rowIndex++)
        totalRow.createCell(0).setCellValue("TOTAL")
        totalRow.createCell(2).setCellValue(totalBase.toDouble())
        totalRow.createCell(3).setCellValue(totalMax.toDouble())
        totalRow.createCell(4).setCellValue(totalPrice.toDouble())

        val qrisRow = sheet.createRow(rowIndex++)
        qrisRow.createCell(0).setCellValue("Jumlah QRIS")
        qrisRow.createCell(1).setCellValue((paymentCounts[PaymentMethod.QRIS.label] ?: 0).toDouble())

        val cashRow = sheet.createRow(rowIndex++)
        cashRow.createCell(0).setCellValue("Jumlah Cash")
        cashRow.createCell(1).setCellValue((paymentCounts[PaymentMethod.Cash.label] ?: 0).toDouble())

        val creditRow = sheet.createRow(rowIndex++)
        creditRow.createCell(0).setCellValue("Jumlah Kredit")
        creditRow.createCell(1).setCellValue((paymentCounts[PaymentMethod.Credit.label] ?: 0).toDouble())

        // Set lebar kolom manual
        val widths = listOf(20, 15, 15, 15, 15, 15, 20)
        widths.forEachIndexed { i, width -> sheet.setColumnWidth(i, width * 256) }

        // Simpan ke folder Documents publik
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS)
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
        if (uri == null) {
            Toast.makeText(context, "Gagal membuat file", Toast.LENGTH_LONG).show()
            return false
        }

        resolver.openOutputStream(uri)?.use { outputStream ->
            workbook.write(outputStream)
        }

        contentValues.clear()
        contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
        resolver.update(uri, contentValues, null, null)

        workbook.close()
        Toast.makeText(context, "Excel disimpan di folder Documents", Toast.LENGTH_LONG).show()
        true
    } catch (e: Exception) {
        Log.e("ExportItemsToExcel", "export failed", e)
        Toast.makeText(context, "Gagal mengekspor Excel. Coba lagi.", Toast.LENGTH_LONG).show()
        false
    }
}
