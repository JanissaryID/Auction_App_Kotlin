package com.polytron.auctionapp.desktop.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.polytron.auctionapp.domain.model.ItemResponse
import com.polytron.auctionapp.utils.formatRupiah

@Composable
fun DashboardScreen(
    items: List<ItemResponse>,
    isLoggedIn: Boolean,
    userName: String?
) {
    val totalBase = items.sumOf { it.basePrice?.toLongOrNull() ?: 0L }.toString()
    val totalAuction = items.sumOf { it.price?.toLongOrNull() ?: 0L }.toString()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatTile("Total barang", items.size.toString(), Modifier.weight(1f))
            StatTile("Siap lelang", items.count { it.status == 0 }.toString(), Modifier.weight(1f))
            StatTile("Selesai", items.count { it.status == 3 }.toString(), Modifier.weight(1f))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatTile("Nilai dasar", formatRupiah(totalBase), Modifier.weight(1f))
            StatTile("Nilai lelang", formatRupiah(totalAuction), Modifier.weight(1f))
            StatTile(
                "Status login",
                if (isLoggedIn) userName?.takeIf { it.isNotBlank() } ?: "Login" else "Belum login",
                Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
