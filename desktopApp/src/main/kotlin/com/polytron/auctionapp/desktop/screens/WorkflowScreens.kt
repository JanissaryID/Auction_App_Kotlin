package com.polytron.auctionapp.desktop.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.polytron.auctionapp.domain.model.ItemResponse

@Composable
fun AuctionScreen(
    items: List<ItemResponse>,
    selectedItemCount: Int
) {
    WorkflowSummary(
        title = "Lelang",
        rows = listOf(
            "Barang tersedia" to items.count { it.status == 0 }.toString(),
            "Barang dipilih" to selectedItemCount.toString()
        )
    )
}

@Composable
fun PaymentScreen(items: List<ItemResponse>) {
    WorkflowSummary(
        title = "Pembayaran",
        rows = listOf(
            "Menunggu pembayaran" to items.count { it.status == 1 }.toString(),
            "Sudah dibayar" to items.count { it.status == 2 }.toString()
        )
    )
}

@Composable
fun PickupScreen(items: List<ItemResponse>) {
    WorkflowSummary(
        title = "Pengambilan",
        rows = listOf(
            "Menunggu pengambilan" to items.count { it.status == 2 }.toString(),
            "Selesai" to items.count { it.status == 3 }.toString()
        )
    )
}

@Composable
fun TransactionsScreen(items: List<ItemResponse>) {
    WorkflowSummary(
        title = "Transaksi",
        rows = listOf(
            "Transaksi selesai" to items.count { it.status == 3 }.toString(),
            "Order tercatat" to items.count { !it.orderID.isNullOrBlank() }.toString()
        )
    )
}

@Composable
private fun WorkflowSummary(
    title: String,
    rows: List<Pair<String, String>>
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                rows.forEach { (label, value) ->
                    Text(
                        text = "$label: $value",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}
