package com.polytron.auctionapp.desktop.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.polytron.auctionapp.desktop.components.BodyCell
import com.polytron.auctionapp.desktop.components.DesktopButton as Button
import com.polytron.auctionapp.desktop.components.DesktopOutlinedButton as OutlinedButton
import com.polytron.auctionapp.desktop.components.DesktopTable
import com.polytron.auctionapp.desktop.components.DesktopToolbar
import com.polytron.auctionapp.desktop.components.HeaderCell
import com.polytron.auctionapp.desktop.components.MetricTile
import com.polytron.auctionapp.desktop.components.StatusBadge
import com.polytron.auctionapp.desktop.components.StatusTone
import com.polytron.auctionapp.domain.model.ItemResponse
import com.polytron.auctionapp.utils.formatRupiah

private val PaymentTableMinWidth = 920.dp
private val PaymentCodeColumn = 110.dp
private val PaymentNameColumn = 280.dp
private val PaymentBuyerColumn = 220.dp
private val PaymentPriceColumn = 160.dp
private val PaymentActionsColumn = 60.dp
private val PaymentHistoryTableMinWidth = 1120.dp
private val PaymentHistoryOrderColumn = 190.dp
private val PaymentHistoryBuyerColumn = 260.dp
private val PaymentHistoryCountColumn = 90.dp
private val PaymentHistoryTotalColumn = 160.dp
private val PaymentHistoryMethodColumn = 130.dp
private val PaymentHistoryStatusColumn = 120.dp
private val PaymentHistoryActionsColumn = 80.dp

@Composable
fun PaymentScreen(
    items: List<ItemResponse>,
    selectedItems: List<ItemResponse>,
    onAddItem: () -> Unit,
    onBarcodeEntry: () -> Unit,
    onRemoveItem: (ItemResponse) -> Unit,
    onClearAll: () -> Unit,
    onPayClick: () -> Unit,
    onReprintPaymentReceipt: (String) -> Unit
) {
    val totalAmount = selectedItems.sumOf { it.price?.toLongOrNull() ?: 0L }
    val paymentHistory = remember(items) {
        items.filter { !it.orderID.isNullOrBlank() && (it.status ?: -1) >= 2 }
            .groupBy { it.orderID.orEmpty() }
            .map { (orderId, orderItems) ->
                val representativeItem = orderItems.first()
                PaymentHistorySummary(
                    orderId = orderId,
                    buyer = representativeItem.buyer.orEmpty(),
                    totalItems = orderItems.size,
                    totalPrice = orderItems.sumOf { it.price?.toLongOrNull() ?: 0L },
                    paymentMethod = representativeItem.typePayment.orEmpty(),
                    isCompleted = orderItems.all { it.status == 3 },
                    date = representativeItem.updated ?: representativeItem.created.orEmpty()
                )
            }
            .sortedByDescending { it.date }
    }

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
            modifier = Modifier.fillMaxWidth().weight(1.05f),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                DesktopTable(minWidth = PaymentTableMinWidth) {
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

        Surface(
            modifier = Modifier.fillMaxWidth().weight(0.95f),
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Daftar Pembayaran",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                DesktopTable(
                    minWidth = PaymentHistoryTableMinWidth,
                    modifier = Modifier.weight(1f)
                ) {
                    PaymentHistoryRowHeader()
                    if (paymentHistory.isEmpty()) {
                        Text(
                            text = "Belum ada pembayaran tersimpan.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(18.dp)
                        )
                    } else {
                        paymentHistory.forEach { summary ->
                            PaymentHistoryRow(
                                summary = summary,
                                onPrint = { onReprintPaymentReceipt(summary.orderId) }
                            )
                        }
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
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HeaderCell("Kode", Modifier.width(PaymentCodeColumn))
        HeaderCell("Nama Barang", Modifier.width(PaymentNameColumn))
        HeaderCell("Pemenang", Modifier.width(PaymentBuyerColumn))
        HeaderCell("Harga Lelang", Modifier.width(PaymentPriceColumn))
        HeaderCell("Aksi", Modifier.width(PaymentActionsColumn))
    }
}

@Composable
private fun PaymentRow(
    item: ItemResponse,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BodyCell(item.codeItem.orEmpty(), Modifier.width(PaymentCodeColumn))
        BodyCell(item.nameItem.orEmpty(), Modifier.width(PaymentNameColumn))
        BodyCell(item.buyer.orEmpty(), Modifier.width(PaymentBuyerColumn))
        BodyCell(formatRupiah(item.price), Modifier.width(PaymentPriceColumn))
        
        Box(modifier = Modifier.width(PaymentActionsColumn)) {
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
    }
    HorizontalDivider(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.outlineVariant
    )
}


@Composable
private fun MaterialTheme.outlineVariant() = colorScheme.outlineVariant

@Composable
private fun PaymentHistoryRowHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HeaderCell("Order ID", Modifier.width(PaymentHistoryOrderColumn))
        HeaderCell("Pemenang", Modifier.width(PaymentHistoryBuyerColumn))
        HeaderCell("Jumlah", Modifier.width(PaymentHistoryCountColumn))
        HeaderCell("Total", Modifier.width(PaymentHistoryTotalColumn))
        HeaderCell("Metode", Modifier.width(PaymentHistoryMethodColumn))
        HeaderCell("Status", Modifier.width(PaymentHistoryStatusColumn))
        HeaderCell("Aksi", Modifier.width(PaymentHistoryActionsColumn))
    }
}

@Composable
private fun PaymentHistoryRow(
    summary: PaymentHistorySummary,
    onPrint: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BodyCell(summary.orderId, Modifier.width(PaymentHistoryOrderColumn))
        BodyCell(summary.buyer, Modifier.width(PaymentHistoryBuyerColumn))
        BodyCell("${summary.totalItems} item", Modifier.width(PaymentHistoryCountColumn))
        BodyCell(formatRupiah(summary.totalPrice.toString()), Modifier.width(PaymentHistoryTotalColumn))
        BodyCell(summary.paymentMethod, Modifier.width(PaymentHistoryMethodColumn))
        Box(modifier = Modifier.width(PaymentHistoryStatusColumn)) {
            StatusBadge(
                text = if (summary.isCompleted) "Diambil" else "Dibayar",
                tone = if (summary.isCompleted) StatusTone.Complete else StatusTone.Paid
            )
        }
        Box(modifier = Modifier.width(PaymentHistoryActionsColumn)) {
            IconButton(
                onClick = onPrint,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Print,
                    contentDescription = "Cetak ulang struk pembayaran",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
    HorizontalDivider(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.outlineVariant
    )
}

private data class PaymentHistorySummary(
    val orderId: String,
    val buyer: String,
    val totalItems: Int,
    val totalPrice: Long,
    val paymentMethod: String,
    val isCompleted: Boolean,
    val date: String
)
