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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCodeScanner
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.polytron.auctionapp.desktop.components.BodyCell
import com.polytron.auctionapp.desktop.components.DesktopButton as Button
import com.polytron.auctionapp.desktop.components.DesktopDimens
import com.polytron.auctionapp.desktop.components.DesktopOutlinedButton as OutlinedButton
import com.polytron.auctionapp.desktop.components.DesktopTable
import com.polytron.auctionapp.desktop.components.DesktopToolbar
import com.polytron.auctionapp.desktop.components.HeaderCell
import com.polytron.auctionapp.desktop.components.MetricTile
import com.polytron.auctionapp.desktop.components.StatusBadge
import com.polytron.auctionapp.desktop.components.StatusTone
import com.polytron.auctionapp.domain.model.ItemResponse
import com.polytron.auctionapp.utils.formatCurrencyInput
import com.polytron.auctionapp.utils.formatRupiah

private val AuctionTableMinWidth = 1280.dp
private val AuctionCodeColumn = 130.dp
private val AuctionNameColumn = 300.dp
private val AuctionBasePriceColumn = 140.dp
private val AuctionMaxPriceColumn = 140.dp
private val AuctionBuyerColumn = 240.dp
private val AuctionPriceColumn = 160.dp
private val AuctionActionsColumn = 60.dp
private val AuctionHistoryTableMinWidth = 1180.dp
private val AuctionHistoryCodeColumn = 120.dp
private val AuctionHistoryNameColumn = 280.dp
private val AuctionHistoryBuyerColumn = 240.dp
private val AuctionHistoryPriceColumn = 160.dp
private val AuctionHistoryStatusColumn = 130.dp
private val AuctionHistoryActionsColumn = 80.dp

@Composable
fun AuctionScreen(
    items: List<ItemResponse>,
    selectedItems: List<ItemResponse>,
    editingBuyers: Map<String, String>,
    editingPrices: Map<String, String>,
    onAddItem: () -> Unit,
    onRemoveItem: (ItemResponse) -> Unit,
    onClearAll: () -> Unit,
    onBuyerChange: (String, String) -> Unit,
    onPriceChange: (String, String) -> Unit,
    onApplyPriceToAll: (String) -> Unit,
    onPrintReceipts: () -> Unit,
    onReprintAuctionReceipt: (ItemResponse) -> Unit,
    onSubmit: suspend () -> Unit
) {
    var globalPrice by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val auctionHistory = remember(items) {
        items.filter { item ->
            (item.status ?: -1) >= 1 &&
                !item.buyer.isNullOrBlank() &&
                !item.price.isNullOrBlank()
        }.sortedByDescending { it.updated ?: it.created.orEmpty() }
    }
    val isReadyToSubmit = selectedItems.isNotEmpty() && selectedItems.all { item ->
        val itemId = item.id ?: ""
        editingBuyers[itemId]?.isNotBlank() == true && 
        editingPrices[itemId]?.isNotBlank() == true
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
                label = "Barang Tersedia",
                value = items.count { it.status == 0 }.toString(),
                modifier = Modifier.weight(1f)
            )
            MetricTile(
                label = "Barang Dipilih",
                value = selectedItems.size.toString(),
                modifier = Modifier.weight(1f)
            )
            MetricTile(
                label = "Menunggu Pembayaran",
                value = items.count { it.status == 1 }.toString(),
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
            },
            actions = {
                Spacer(Modifier.weight(1f))
                if (selectedItems.isNotEmpty()) {
                    OutlinedTextField(
                        value = formatCurrencyInput(globalPrice),
                        onValueChange = { globalPrice = it.filter { char -> char.isDigit() } },
                        placeholder = { Text("Harga semua") },
                        modifier = Modifier.width(180.dp).height(DesktopDimens.ControlHeight),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )
                    Spacer(Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = { onApplyPriceToAll(globalPrice) },
                        enabled = globalPrice.isNotBlank(),
                        contentPadding = ButtonDefaults.ButtonWithIconContentPadding
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Terapkan")
                    }
                    Spacer(Modifier.width(8.dp))
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
                    OutlinedButton(
                        onClick = onPrintReceipts,
                        enabled = isReadyToSubmit && !isSubmitting,
                        contentPadding = ButtonDefaults.ButtonWithIconContentPadding
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Cetak Nota")
                    }
                    Spacer(Modifier.width(8.dp))
                }
                Button(
                    onClick = {
                        isSubmitting = true
                        scope.launch {
                            try {
                                onSubmit()
                            } finally {
                                isSubmitting = false
                            }
                        }
                    },
                    enabled = isReadyToSubmit && !isSubmitting,
                    contentPadding = ButtonDefaults.ButtonWithIconContentPadding
                ) {
                    if (isSubmitting) {
                        androidx.compose.material3.CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text("Simpan & Cetak")
                }
            }
        )

        Surface(
            modifier = Modifier.fillMaxWidth().weight(1.1f),
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            DesktopTable(minWidth = AuctionTableMinWidth) {
                AuctionRowHeader()
                if (selectedItems.isEmpty()) {
                    Text(
                        text = "Belum ada barang yang dipilih untuk lelang.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(18.dp)
                    )
                } else {
                    selectedItems.forEach { item ->
                        val itemId = item.id ?: ""
                        AuctionRow(
                            item = item,
                            buyer = editingBuyers[itemId] ?: "",
                            price = formatCurrencyInput(editingPrices[itemId] ?: ""),
                            onBuyerChange = { onBuyerChange(itemId, it) },
                            onPriceChange = { onPriceChange(itemId, it) },
                            onRemove = { onRemoveItem(item) }
                        )
                    }
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth().weight(0.9f),
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Daftar Lelang",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                DesktopTable(
                    minWidth = AuctionHistoryTableMinWidth,
                    modifier = Modifier.weight(1f)
                ) {
                    AuctionHistoryRowHeader()
                    if (auctionHistory.isEmpty()) {
                        Text(
                            text = "Belum ada data lelang tersimpan.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(18.dp)
                        )
                    } else {
                        auctionHistory.forEach { item ->
                            AuctionHistoryRow(
                                item = item,
                                onPrint = { onReprintAuctionReceipt(item) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AuctionRowHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HeaderCell("Kode", Modifier.width(AuctionCodeColumn))
        HeaderCell("Nama Barang", Modifier.width(AuctionNameColumn))
        HeaderCell("Harga Dasar", Modifier.width(AuctionBasePriceColumn))
        HeaderCell("Harga Maks", Modifier.width(AuctionMaxPriceColumn))
        HeaderCell("Pemenang", Modifier.width(AuctionBuyerColumn))
        HeaderCell("Harga Lelang", Modifier.width(AuctionPriceColumn))
        HeaderCell("Aksi", Modifier.width(AuctionActionsColumn))
    }
}

@Composable
private fun AuctionRow(
    item: ItemResponse,
    buyer: String,
    price: String,
    onBuyerChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BodyCell(item.codeItem.orEmpty(), Modifier.width(AuctionCodeColumn))
        BodyCell(item.nameItem.orEmpty(), Modifier.width(AuctionNameColumn))
        BodyCell(formatRupiah(item.basePrice), Modifier.width(AuctionBasePriceColumn))
        BodyCell(formatRupiah(item.maxPrice), Modifier.width(AuctionMaxPriceColumn))
        
        OutlinedTextField(
            value = buyer,
            onValueChange = onBuyerChange,
            placeholder = { Text("Nama pemenang") },
            modifier = Modifier.width(AuctionBuyerColumn).height(DesktopDimens.ControlHeight),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            )
        )

        OutlinedTextField(
            value = price,
            onValueChange = onPriceChange,
            placeholder = { Text("Harga") },
            modifier = Modifier.width(AuctionPriceColumn).height(DesktopDimens.ControlHeight),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            )
        )
        
        Box(modifier = Modifier.width(AuctionActionsColumn)) {
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
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant)
    )
}

@Composable
private fun AuctionHistoryRowHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HeaderCell("Kode", Modifier.width(AuctionHistoryCodeColumn))
        HeaderCell("Nama Barang", Modifier.width(AuctionHistoryNameColumn))
        HeaderCell("Pemenang", Modifier.width(AuctionHistoryBuyerColumn))
        HeaderCell("Harga Lelang", Modifier.width(AuctionHistoryPriceColumn))
        HeaderCell("Status", Modifier.width(AuctionHistoryStatusColumn))
        HeaderCell("Aksi", Modifier.width(AuctionHistoryActionsColumn))
    }
}

@Composable
private fun AuctionHistoryRow(
    item: ItemResponse,
    onPrint: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BodyCell(item.codeItem.orEmpty(), Modifier.width(AuctionHistoryCodeColumn))
        BodyCell(item.nameItem.orEmpty(), Modifier.width(AuctionHistoryNameColumn))
        BodyCell(item.buyer.orEmpty(), Modifier.width(AuctionHistoryBuyerColumn))
        BodyCell(formatRupiah(item.price), Modifier.width(AuctionHistoryPriceColumn))
        Box(modifier = Modifier.width(AuctionHistoryStatusColumn)) {
            StatusBadge(
                text = auctionStatusLabel(item.status),
                tone = auctionStatusTone(item.status)
            )
        }
        Box(modifier = Modifier.width(AuctionHistoryActionsColumn)) {
            IconButton(
                onClick = onPrint,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Print,
                    contentDescription = "Cetak ulang nota lelang",
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

private fun auctionStatusLabel(status: Int?): String {
    return when (status) {
        1 -> "Lelang"
        2 -> "Dibayar"
        3 -> "Diambil"
        else -> "-"
    }
}

private fun auctionStatusTone(status: Int?): StatusTone {
    return when (status) {
        1 -> StatusTone.Warning
        2 -> StatusTone.Paid
        3 -> StatusTone.Complete
        else -> StatusTone.Neutral
    }
}
