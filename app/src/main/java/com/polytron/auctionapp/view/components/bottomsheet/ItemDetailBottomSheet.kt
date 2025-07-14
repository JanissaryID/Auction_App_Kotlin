package com.polytron.auctionapp.view.components.bottomsheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.polytron.auctionapp.model.ItemResponse
import com.polytron.auctionapp.utils.formatRupiah

@Composable
fun ItemDetailBottomSheet(
    item: ItemResponse,
    onDismissRequest: () -> Unit
) {
    val formattedBasePrice = remember(item.basePrice) { formatRupiah(item.basePrice) }
    val formattedPrice = remember(item.price) { formatRupiah(item.price) }
    val formattedMaxPrice = remember(item.maxPrice) { formatRupiah(item.maxPrice) }

    val statusText = when (item.status) {
        0 -> "Belum Ditawar"
        1 -> "Ditawar"
        2 -> "Dibayar"
        3 -> "Diambil"
        else -> "Tidak Diketahui"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Text(
            text = item.nameItem.orEmpty(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Dua kolom informasi
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                InfoRow("Kode", item.codeItem)
                InfoRow("Pembeli", item.buyer)
                InfoRow("Admin", item.admin)
                InfoRow("Order ID", item.orderID)
            }
            Column(modifier = Modifier.weight(1f)) {
                InfoRow("Harga Awal", formattedBasePrice)
                InfoRow("Harga Sekarang", formattedPrice)
                InfoRow("Harga Maksimum", formattedMaxPrice)
                InfoRow("Pembayaran", item.typePayment)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
        Spacer(modifier = Modifier.height(8.dp))

        InfoRow("Status", statusText)


        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onDismissRequest) {
                Text("Tutup")
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String?) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value ?: "-",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

