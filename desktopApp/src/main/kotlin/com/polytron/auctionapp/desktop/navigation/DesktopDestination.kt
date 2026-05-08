package com.polytron.auctionapp.desktop.navigation

enum class DesktopDestination(
    val label: String,
    val title: String,
    val subtitle: String
) {
    Dashboard("Dashboard", "Dashboard", "Ringkasan operasional lelang"),
    Items("Barang", "Barang", "Kelola data barang lelang"),
    Auction("Lelang", "Lelang", "Proses hasil lelang barang"),
    Payment("Pembayaran", "Pembayaran", "Konfirmasi pembayaran pemenang"),
    Pickup("Pengambilan", "Pengambilan", "Konfirmasi barang diambil"),
    Transactions("Transaksi", "Transaksi", "Riwayat transaksi selesai")
}
