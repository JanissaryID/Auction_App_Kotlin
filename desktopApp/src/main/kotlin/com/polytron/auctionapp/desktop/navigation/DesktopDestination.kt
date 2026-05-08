package com.polytron.auctionapp.desktop.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.ui.graphics.vector.ImageVector

enum class DesktopDestination(
    val label: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector
) {
    Dashboard("Dashboard", "Dashboard", "Ringkasan operasional lelang", Icons.Default.Dashboard),
    Items("Barang", "Barang", "Kelola data barang lelang", Icons.Default.Inventory2),
    Auction("Lelang", "Lelang", "Proses hasil lelang barang", Icons.Default.Gavel),
    Payment("Pembayaran", "Pembayaran", "Konfirmasi pembayaran pemenang", Icons.Default.Payments),
    Pickup("Pengambilan", "Pengambilan", "Konfirmasi barang diambil", Icons.Default.TaskAlt),
    Transactions("Transaksi", "Transaksi", "Riwayat transaksi selesai", Icons.AutoMirrored.Filled.ReceiptLong)
}
