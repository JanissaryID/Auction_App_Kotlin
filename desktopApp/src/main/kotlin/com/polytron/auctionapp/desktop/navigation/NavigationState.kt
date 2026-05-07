package com.polytron.auctionapp.desktop.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Navigation destinations for desktop app
 */
enum class NavDestination(
    val title: String,
    val icon: ImageVector
) {
    ITEM_LIST("Daftar Barang", Icons.AutoMirrored.Filled.List),
    AUCTION("Lelang", Icons.Default.Gavel),
    PAYMENT("Pembayaran", Icons.Default.Payments),
    TAKE_ITEMS("Ambil Barang", Icons.Default.Inventory2),
    TRANSACTIONS("Transaksi", Icons.Default.Receipt)
}

/**
 * Navigation state holder
 */
class NavigationState {
    var currentDestination by mutableStateOf(NavDestination.ITEM_LIST)
        private set

    fun navigateTo(destination: NavDestination) {
        currentDestination = destination
    }
}
