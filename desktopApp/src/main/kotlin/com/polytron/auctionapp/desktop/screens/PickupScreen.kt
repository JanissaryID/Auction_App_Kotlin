package com.polytron.auctionapp.desktop.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.polytron.auctionapp.desktop.components.DesktopToolbar
import com.polytron.auctionapp.desktop.components.MetricTile
import com.polytron.auctionapp.domain.model.ItemResponse
import com.polytron.auctionapp.utils.formatRupiah

@Composable
fun PickupScreen(
    items: List<ItemResponse>,
    selectedItems: List<ItemResponse>,
    onAddItem: () -> Unit,
    onBarcodeEntry: () -> Unit,
    onRemoveItem: (ItemResponse) -> Unit,
    onClearAll: () -> Unit,
    onPickupClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MetricTile(
                label = "Siap Diambil",
                value = items.count { it.status == 2 }.toString(),
                modifier = Modifier.weight(1f)
            )
            MetricTile(
                label = "Barang Dipilih",
                value = selectedItems.size.toString(),
                modifier = Modifier.weight(1f)
            )
            MetricTile(
                label = "Sudah Diambil",
                value = items.count { it.status == 3 }.toString(),
                modifier = Modifier.weight(1f)
            )
        }

        DesktopToolbar(
            leading = {
                OutlinedButton(
                    onClick = onAddItem,
                    contentPadding = ButtonDefaults.ButtonWithIconContentPadding
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Pilih Barang")
                }
                Spacer(Modifier.width(8.dp))
                OutlinedButton(
                    onClick = onBarcodeEntry,
                    contentPadding = ButtonDefaults.ButtonWithIconContentPadding
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Input Kode")
                }
            },
            actions = {
                Spacer(Modifier.weight(1f))
                if (selectedItems.isNotEmpty()) {
                    OutlinedButton(
                        onClick = onClearAll,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.ClearAll, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Kosongkan")
                    }
                    Spacer(Modifier.width(8.dp))
                }
                Button(
                    onClick = onPickupClick,
                    enabled = selectedItems.isNotEmpty(),
                    contentPadding = ButtonDefaults.ButtonWithIconContentPadding
                ) {
                    Icon(Icons.Default.Handyman, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Konfirmasi Pengambilan")
                }
            }
        )

        Surface(
            modifier = Modifier.fillMaxWidth().weight(1f),
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .verticalScroll(rememberScrollState())
            ) {
                PickupRowHeader()
                if (selectedItems.isEmpty()) {
                    Text(
                        text = "Belum ada barang yang dipilih untuk pengambilan.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(18.dp)
                    )
                } else {
                    selectedItems.forEach { item ->
                        PickupRow(
                            item = item,
                            onRemove = { onRemoveItem(item) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PickupRowHeader() {
    Row(
        modifier = Modifier
            .width(1000.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HeaderCell("Kode", Modifier.width(100.dp))
        HeaderCell("Nama Barang", Modifier.weight(2f))
        HeaderCell("Pemenang", Modifier.weight(1.5f))
        HeaderCell("Order ID", Modifier.width(180.dp))
        HeaderCell("Metode Bayar", Modifier.width(120.dp))
        HeaderCell("Aksi", Modifier.width(60.dp))
    }
}

@Composable
private fun PickupRow(
    item: ItemResponse,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .width(1000.dp)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BodyCell(item.codeItem.orEmpty(), Modifier.width(100.dp))
        BodyCell(item.nameItem.orEmpty(), Modifier.weight(2f))
        BodyCell(item.buyer.orEmpty(), Modifier.weight(1.5f))
        BodyCell(item.orderID.orEmpty(), Modifier.width(180.dp))
        BodyCell(item.typePayment.orEmpty(), Modifier.width(120.dp))
        
        IconButton(
            onClick = onRemove,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Hapus",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(18.dp)
            )
        }
    }
    HorizontalDivider(
        modifier = Modifier.width(1000.dp),
        color = MaterialTheme.colorScheme.outlineVariant
    )
}

@Composable
private fun HeaderCell(text: String, modifier: Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
    )
}

@Composable
private fun BodyCell(text: String, modifier: Modifier) {
    Text(
        text = text.ifBlank { "-" },
        style = MaterialTheme.typography.bodyMedium,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
    )
}
