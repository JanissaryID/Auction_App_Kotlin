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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.polytron.auctionapp.desktop.components.BodyCell
import com.polytron.auctionapp.desktop.components.DesktopDimens
import com.polytron.auctionapp.desktop.components.DesktopOutlinedButton as OutlinedButton
import com.polytron.auctionapp.desktop.components.DesktopTable
import com.polytron.auctionapp.desktop.components.DesktopToolbar
import com.polytron.auctionapp.desktop.components.HeaderCell
import com.polytron.auctionapp.desktop.components.MetricTile
import com.polytron.auctionapp.desktop.components.StatusBadge
import com.polytron.auctionapp.desktop.components.StatusTone
import com.polytron.auctionapp.domain.model.ItemResponse
import com.polytron.auctionapp.utils.formatRupiah

private val TransactionsTableMinWidth = 1160.dp
private val TransactionsOrderColumn = 190.dp
private val TransactionsBuyerColumn = 300.dp
private val TransactionsCountColumn = 90.dp
private val TransactionsTotalColumn = 160.dp
private val TransactionsMethodColumn = 130.dp
private val TransactionsStatusColumn = 110.dp
private val TransactionsActionsColumn = 60.dp

@Composable
fun TransactionsScreen(
    items: List<ItemResponse>,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onExport: () -> Unit,
    onTransactionDetail: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val transactions = remember(items, searchQuery) {
        items.filter { !it.orderID.isNullOrBlank() }
            .groupBy { it.orderID!! }
            .map { (orderId, orderItems) ->
                val representativeItem = orderItems.first()
                TransactionSummary(
                    orderId = orderId,
                    buyer = representativeItem.buyer.orEmpty(),
                    totalItems = orderItems.size,
                    totalPrice = orderItems.sumOf { it.price?.toLongOrNull() ?: 0L },
                    date = representativeItem.updated ?: representativeItem.created.orEmpty(),
                    paymentMethod = representativeItem.typePayment.orEmpty(),
                    isCompleted = orderItems.all { it.status == 3 }
                )
            }
            .filter { summary ->
                searchQuery.isBlank() ||
                summary.orderId.contains(searchQuery, ignoreCase = true) ||
                summary.buyer.contains(searchQuery, ignoreCase = true)
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
                label = "Total Transaksi",
                value = transactions.size.toString(),
                modifier = Modifier.weight(1f)
            )
            MetricTile(
                label = "Transaksi Selesai",
                value = transactions.count { it.isCompleted }.toString(),
                modifier = Modifier.weight(1f)
            )
            MetricTile(
                label = "Omzet Total",
                value = formatRupiah(transactions.sumOf { it.totalPrice }.toString()),
                modifier = Modifier.weight(1.5f)
            )
        }

        DesktopToolbar(
            leading = {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari Order ID atau Pemenang...", style = MaterialTheme.typography.bodyMedium) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.width(350.dp).height(DesktopDimens.ControlHeight),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    )
                )
            },
            actions = {
                Spacer(Modifier.weight(1f))
                OutlinedButton(
                    onClick = onExport,
                    contentPadding = ButtonDefaults.ButtonWithIconContentPadding
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Export")
                }
                Spacer(Modifier.width(8.dp))
                OutlinedButton(
                    onClick = onRefresh,
                    enabled = !isLoading,
                    contentPadding = ButtonDefaults.ButtonWithIconContentPadding
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Refresh")
                }
            }
        )

        Surface(
            modifier = Modifier.fillMaxWidth().weight(1f),
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            DesktopTable(minWidth = TransactionsTableMinWidth) {
                TransactionRowHeader()
                if (transactions.isEmpty()) {
                    Text(
                        text = if (items.isEmpty()) "Belum ada riwayat transaksi." else "Tidak ada transaksi yang cocok.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(18.dp)
                    )
                } else {
                    transactions.forEach { summary ->
                        TransactionRow(
                            summary = summary,
                            onDetail = { onTransactionDetail(summary.orderId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionRowHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HeaderCell("Order ID", Modifier.width(TransactionsOrderColumn))
        HeaderCell("Pemenang", Modifier.width(TransactionsBuyerColumn))
        HeaderCell("Jumlah", Modifier.width(TransactionsCountColumn))
        HeaderCell("Total Harga", Modifier.width(TransactionsTotalColumn))
        HeaderCell("Metode", Modifier.width(TransactionsMethodColumn))
        HeaderCell("Status", Modifier.width(TransactionsStatusColumn))
        HeaderCell("Aksi", Modifier.width(TransactionsActionsColumn))
    }
}

@Composable
private fun TransactionRow(
    summary: TransactionSummary,
    onDetail: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BodyCell(summary.orderId, Modifier.width(TransactionsOrderColumn))
        BodyCell(summary.buyer, Modifier.width(TransactionsBuyerColumn))
        BodyCell("${summary.totalItems} item", Modifier.width(TransactionsCountColumn))
        BodyCell(formatRupiah(summary.totalPrice.toString()), Modifier.width(TransactionsTotalColumn))
        BodyCell(summary.paymentMethod, Modifier.width(TransactionsMethodColumn))
        
        Box(modifier = Modifier.width(TransactionsStatusColumn)) {
            StatusBadge(
                text = if (summary.isCompleted) "Selesai" else "Proses",
                tone = if (summary.isCompleted) StatusTone.Complete else StatusTone.Warning
            )
        }

        Box(modifier = Modifier.width(TransactionsActionsColumn)) {
            IconButton(
                onClick = onDetail,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Detail",
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



private data class TransactionSummary(
    val orderId: String,
    val buyer: String,
    val totalItems: Int,
    val totalPrice: Long,
    val date: String,
    val paymentMethod: String,
    val isCompleted: Boolean
)
