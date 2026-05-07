package com.polytron.auctionapp.desktop.utils

import com.polytron.auctionapp.shared.model.SharedItem
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object PdfExporter {
    
    /**
     * Export items to a simple text-based report
     * Note: For production, consider using libraries like Apache PDFBox or iText
     */
    fun exportToReport(
        items: List<SharedItem>,
        reportType: String,
        outputPath: String? = null
    ): Result<String> {
        return try {
            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
            val fileName = outputPath ?: "AuctionReport_${reportType}_$timestamp.txt"
            val file = File(fileName)
            
            val content = buildString {
                appendLine("=" .repeat(80))
                appendLine("AUCTION APP - LAPORAN $reportType")
                appendLine("=" .repeat(80))
                appendLine("Tanggal: ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))}")
                appendLine("Total Items: ${items.size}")
                appendLine("=" .repeat(80))
                appendLine()
                
                when (reportType) {
                    "PAYMENT" -> appendPaymentReport(items)
                    "PICKUP" -> appendPickupReport(items)
                    "AUCTION" -> appendAuctionReport(items)
                    else -> appendGeneralReport(items)
                }
                
                appendLine()
                appendLine("=" .repeat(80))
                appendLine("End of Report")
                appendLine("=" .repeat(80))
            }
            
            file.writeText(content)
            Result.success(file.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun StringBuilder.appendPaymentReport(items: List<SharedItem>) {
        val unpaid = items.filter { it.status == 1 }
        val paid = items.filter { it.status == 2 }
        
        appendLine("RINGKASAN PEMBAYARAN")
        appendLine("-" .repeat(80))
        appendLine("Belum Dibayar: ${unpaid.size} items")
        appendLine("Sudah Dibayar: ${paid.size} items")
        
        val totalRevenue = paid.sumOf { 
            it.price?.replace(Regex("\\D"), "")?.toLongOrNull() ?: 0 
        }
        appendLine("Total Pendapatan: Rp ${formatPrice(totalRevenue)}")
        appendLine()
        
        if (unpaid.isNotEmpty()) {
            appendLine("DAFTAR BELUM DIBAYAR:")
            appendLine("-" .repeat(80))
            unpaid.forEachIndexed { index, item ->
                appendLine("${index + 1}. ${item.nameItem ?: "Unknown"}")
                appendLine("   Kode: ${item.codeItem ?: "-"}")
                appendLine("   Harga: ${item.price ?: "Rp 0"}")
                appendLine("   Pembeli: ${item.buyer ?: "-"}")
                appendLine()
            }
        }
        
        if (paid.isNotEmpty()) {
            appendLine("DAFTAR SUDAH DIBAYAR:")
            appendLine("-" .repeat(80))
            paid.forEachIndexed { index, item ->
                appendLine("${index + 1}. ${item.nameItem ?: "Unknown"}")
                appendLine("   Kode: ${item.codeItem ?: "-"}")
                appendLine("   Harga: ${item.price ?: "Rp 0"}")
                appendLine("   Pembeli: ${item.buyer ?: "-"}")
                appendLine()
            }
        }
    }
    
    private fun StringBuilder.appendPickupReport(items: List<SharedItem>) {
        val ready = items.filter { it.status == 2 }
        val taken = items.filter { it.status == 3 }
        
        appendLine("RINGKASAN PENGAMBILAN BARANG")
        appendLine("-" .repeat(80))
        appendLine("Siap Diambil: ${ready.size} items")
        appendLine("Sudah Diambil: ${taken.size} items")
        appendLine()
        
        if (ready.isNotEmpty()) {
            appendLine("DAFTAR SIAP DIAMBIL:")
            appendLine("-" .repeat(80))
            ready.forEachIndexed { index, item ->
                appendLine("${index + 1}. ${item.nameItem ?: "Unknown"}")
                appendLine("   Kode: ${item.codeItem ?: "-"}")
                appendLine("   Order ID: ${item.orderId ?: "-"}")
                appendLine("   Pembeli: ${item.buyer ?: "-"}")
                appendLine("   Harga: ${item.price ?: "Rp 0"}")
                appendLine()
            }
        }
        
        if (taken.isNotEmpty()) {
            appendLine("DAFTAR SUDAH DIAMBIL:")
            appendLine("-" .repeat(80))
            taken.forEachIndexed { index, item ->
                appendLine("${index + 1}. ${item.nameItem ?: "Unknown"}")
                appendLine("   Kode: ${item.codeItem ?: "-"}")
                appendLine("   Order ID: ${item.orderId ?: "-"}")
                appendLine("   Pembeli: ${item.buyer ?: "-"}")
                appendLine("   Harga: ${item.price ?: "Rp 0"}")
                appendLine()
            }
        }
    }
    
    private fun StringBuilder.appendAuctionReport(items: List<SharedItem>) {
        val available = items.filter { it.status == 1 }
        
        appendLine("RINGKASAN LELANG")
        appendLine("-" .repeat(80))
        appendLine("Barang Tersedia: ${available.size} items")
        
        val totalValue = available.sumOf { 
            it.price?.replace(Regex("\\D"), "")?.toLongOrNull() ?: 0 
        }
        appendLine("Total Nilai: Rp ${formatPrice(totalValue)}")
        appendLine()
        
        if (available.isNotEmpty()) {
            appendLine("DAFTAR BARANG TERSEDIA:")
            appendLine("-" .repeat(80))
            available.forEachIndexed { index, item ->
                appendLine("${index + 1}. ${item.nameItem ?: "Unknown"}")
                appendLine("   Kode: ${item.codeItem ?: "-"}")
                appendLine("   Harga Awal: ${item.price ?: "Rp 0"}")
                appendLine()
            }
        }
    }
    
    private fun StringBuilder.appendGeneralReport(items: List<SharedItem>) {
        val byStatus = items.groupBy { it.status }
        
        appendLine("RINGKASAN UMUM")
        appendLine("-" .repeat(80))
        appendLine("Tersedia (Status 1): ${byStatus[1]?.size ?: 0} items")
        appendLine("Dibayar (Status 2): ${byStatus[2]?.size ?: 0} items")
        appendLine("Diambil (Status 3): ${byStatus[3]?.size ?: 0} items")
        appendLine()
        
        appendLine("DAFTAR SEMUA BARANG:")
        appendLine("-" .repeat(80))
        items.forEachIndexed { index, item ->
            val statusText = when (item.status) {
                1 -> "Tersedia"
                2 -> "Dibayar"
                3 -> "Diambil"
                else -> "Unknown"
            }
            
            appendLine("${index + 1}. ${item.nameItem ?: "Unknown"}")
            appendLine("   Kode: ${item.codeItem ?: "-"}")
            appendLine("   Status: $statusText")
            appendLine("   Harga: ${item.price ?: "Rp 0"}")
            if (item.buyer != null) {
                appendLine("   Pembeli: ${item.buyer}")
            }
            if (item.orderId != null) {
                appendLine("   Order ID: ${item.orderId}")
            }
            appendLine()
        }
    }
    
    private fun formatPrice(price: Long): String {
        return String.format("%,d", price).replace(',', '.')
    }
}
