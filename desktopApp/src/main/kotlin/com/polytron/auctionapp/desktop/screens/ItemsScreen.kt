package com.polytron.auctionapp.desktop.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.polytron.auctionapp.domain.model.ItemResponse
import com.polytron.auctionapp.utils.formatRupiah

@Composable
fun ItemsScreen(
    items: List<ItemResponse>,
    isLoading: Boolean,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${items.size} barang",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.weight(1f))
            if (isLoading) {
                CircularProgressIndicator(strokeWidth = 2.dp)
            } else {
                Button(onClick = onRefresh) {
                    Text("Refresh")
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                ItemRowHeader()
                if (items.isEmpty()) {
                    Text(
                        text = "Belum ada data barang.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(18.dp)
                    )
                } else {
                    items.take(24).forEach { item ->
                        ItemRow(item)
                    }
                }
            }
        }
    }
}

@Composable
private fun ItemRowHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HeaderCell("Kode", Modifier.weight(1f))
        HeaderCell("Nama", Modifier.weight(2f))
        HeaderCell("Status", Modifier.weight(1f))
        HeaderCell("Harga dasar", Modifier.weight(1f))
        HeaderCell("Pemenang", Modifier.weight(1.4f))
        HeaderCell("Order", Modifier.weight(1.2f))
    }
}

@Composable
private fun ItemRow(item: ItemResponse) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        BodyCell(item.codeItem.orEmpty(), Modifier.weight(1f))
        BodyCell(item.nameItem.orEmpty(), Modifier.weight(2f))
        BodyCell(statusLabel(item.status), Modifier.weight(1f))
        BodyCell(formatRupiah(item.basePrice), Modifier.weight(1f))
        BodyCell(item.buyer.orEmpty(), Modifier.weight(1.4f))
        BodyCell(item.orderID.orEmpty(), Modifier.weight(1.2f))
    }
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant)
    )
}

@Composable
private fun HeaderCell(text: String, modifier: Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier
    )
}

@Composable
private fun BodyCell(text: String, modifier: Modifier) {
    Text(
        text = text.ifBlank { "-" },
        style = MaterialTheme.typography.bodyMedium,
        modifier = modifier
    )
}

private fun statusLabel(status: Int?): String {
    return when (status) {
        0 -> "Tersedia"
        1 -> "Lelang"
        2 -> "Dibayar"
        3 -> "Diambil"
        else -> "-"
    }
}
