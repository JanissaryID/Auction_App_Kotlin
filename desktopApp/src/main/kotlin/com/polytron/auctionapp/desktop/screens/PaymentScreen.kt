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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Payments
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.polytron.auctionapp.desktop.components.BodyCell
import com.polytron.auctionapp.desktop.components.DesktopToolbar
import com.polytron.auctionapp.desktop.components.HeaderCell
import com.polytron.auctionapp.desktop.components.MetricTile
import com.polytron.auctionapp.domain.model.ItemResponse
import com.polytron.auctionapp.utils.formatRupiah

@Composable
fun PaymentScreen(
    items: List<ItemResponse>,
    selectedItems: List<ItemResponse>,
    onAddItem: () -> Unit,
    onBarcodeEntry: () -> Unit,
    onRemoveItem: (ItemResponse) -> Unit,
    onClearAll: () -> Unit,
    onPayClick: () -> Unit
) {
    val totalAmount = selectedItems.sumOf { it.price?.toLongOrNull() ?: 0L }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MetricTile(
                label = "Menunggu Pembayaran",
                value = items.count { it.status == 1 }.toString(),
                modifier = Modifier.weight(1f)
            )
            MetricTile(
                label = "Barang Dipilih",
                value = selectedItems.size.toString(),
                modifier = Modifier.weight(1f)
            )
            MetricTile(
                label = "Total Pembayaran",
                value = formatRupiah(totalAmount.toString()),
                modifier = Modifier.weight(1.5f)
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
                    onClick = onPayClick,
                    enabled = selectedItems.isNotEmpty(),
                    contentPadding = ButtonDefaults.ButtonWithIconContentPadding
                ) {
                    Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Bayar Sekarang")
                }
            }
        )

        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                modifier = Modifier.weight(1f),
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
                    PaymentRowHeader()
                    if (selectedItems.isEmpty()) {
                        Text(
                            text = "Belum ada barang yang dipilih untuk pembayaran.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(18.dp)
                        )
                    } else {
                        selectedItems.forEach { item ->
                            PaymentRow(
                                item = item,
                                onRemove = { onRemoveItem(item) }
                            )
                        }
                    }
                }
            }

            // Summary Panel
            Surface(
                modifier = Modifier.width(320.dp),
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Ringkasan Pesanan",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    HorizontalDivider(color = MaterialTheme.outlineVariant())
                    
                    selectedItems.forEach { item ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = item.nameItem.orEmpty(),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = formatRupiah(item.price),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(Modifier.weight(1f))
                    HorizontalDivider(color = MaterialTheme.outlineVariant())
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = formatRupiah(totalAmount.toString()),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentRowHeader() {
    Row(
        modifier = Modifier
            .widthIn(min = 800.dp).fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HeaderCell("Kode", Modifier.width(100.dp))
        HeaderCell("Nama Barang", Modifier.weight(2f))
        HeaderCell("Pemenang", Modifier.weight(1.5f))
        HeaderCell("Harga Lelang", Modifier.width(150.dp))
        HeaderCell("Aksi", Modifier.width(60.dp))
    }
}

@Composable
private fun PaymentRow(
    item: ItemResponse,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .widthIn(min = 800.dp).fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BodyCell(item.codeItem.orEmpty(), Modifier.width(100.dp))
        BodyCell(item.nameItem.orEmpty(), Modifier.weight(2f))
        BodyCell(item.buyer.orEmpty(), Modifier.weight(1.5f))
        BodyCell(formatRupiah(item.price), Modifier.width(150.dp))
        
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
        modifier = Modifier.widthIn(min = 800.dp).fillMaxWidth(),
        color = MaterialTheme.colorScheme.outlineVariant
    )
}


@Composable
private fun MaterialTheme.outlineVariant() = colorScheme.outlineVariant
