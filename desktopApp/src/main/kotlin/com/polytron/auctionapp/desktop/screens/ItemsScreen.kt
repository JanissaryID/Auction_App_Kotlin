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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Checkbox
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import com.polytron.auctionapp.desktop.components.BodyCell
import com.polytron.auctionapp.desktop.components.DesktopButton as Button
import com.polytron.auctionapp.desktop.components.DesktopDimens
import com.polytron.auctionapp.desktop.components.DesktopOutlinedButton as OutlinedButton
import com.polytron.auctionapp.desktop.components.DesktopTable
import com.polytron.auctionapp.desktop.components.DesktopToolbar
import com.polytron.auctionapp.desktop.components.HeaderCell
import com.polytron.auctionapp.desktop.components.StatusBadge
import com.polytron.auctionapp.desktop.components.StatusTone
import com.polytron.auctionapp.domain.model.ItemResponse
import com.polytron.auctionapp.utils.formatRupiah

private val ItemsTableMinWidth = 1600.dp
private val ItemsSelectColumn = 36.dp
private val ItemsCodeColumn = 110.dp
private val ItemsNameColumn = 300.dp
private val ItemsStatusColumn = 110.dp
private val ItemsBasePriceColumn = 130.dp
private val ItemsMaxPriceColumn = 130.dp
private val ItemsBuyerColumn = 200.dp
private val ItemsAuctionPriceColumn = 130.dp
private val ItemsOrderColumn = 170.dp
private val ItemsActionsColumn = 128.dp

@Composable
fun ItemsScreen(
    items: List<ItemResponse>,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onAddItem: () -> Unit,
    onEditItem: (String) -> Unit,
    onDeleteItem: (String) -> Unit,
    onItemDetail: (String) -> Unit,
    onPrintItem: (ItemResponse) -> Unit,
    onBulkDelete: (List<String>) -> Unit,
    onBulkPrint: (List<ItemResponse>) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf<Int?>(null) }
    var selectedIds by remember { mutableStateOf(setOf<String>()) }

    val filteredItems = remember(items, searchQuery, statusFilter) {
        items.filter { item ->
            val matchesSearch = searchQuery.isBlank() ||
                item.nameItem?.contains(searchQuery, ignoreCase = true) == true ||
                item.codeItem?.contains(searchQuery, ignoreCase = true) == true ||
                item.buyer?.contains(searchQuery, ignoreCase = true) == true ||
                item.orderID?.contains(searchQuery, ignoreCase = true) == true

            val matchesStatus = statusFilter == null || item.status == statusFilter

            matchesSearch && matchesStatus
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        DesktopToolbar(
            leading = {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari barang...", style = MaterialTheme.typography.bodyMedium) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.width(300.dp).height(DesktopDimens.ControlHeight),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )

                Spacer(Modifier.width(8.dp))

                StatusFilterRow(
                    selectedStatus = statusFilter,
                    onStatusSelected = { statusFilter = it }
                )
            },
            actions = {
                Spacer(Modifier.weight(1f))

                if (selectedIds.isNotEmpty()) {
                    OutlinedButton(
                        onClick = {
                            val selectedItems = filteredItems.filter { item ->
                                item.id != null && selectedIds.contains(item.id)
                            }
                            onBulkPrint(selectedItems)
                        },
                        contentPadding = ButtonDefaults.ButtonWithIconContentPadding
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Cetak Terpilih (${selectedIds.size})")
                    }
                    Spacer(Modifier.width(8.dp))

                    OutlinedButton(
                        onClick = {
                            onBulkDelete(selectedIds.toList())
                            selectedIds = emptySet()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Hapus Terpilih (${selectedIds.size})")
                    }
                    Spacer(Modifier.width(8.dp))
                }

                OutlinedButton(
                    onClick = onRefresh,
                    enabled = !isLoading,
                    contentPadding = ButtonDefaults.ButtonWithIconContentPadding
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Refresh")
                }

                Spacer(Modifier.width(8.dp))

                Button(
                    onClick = onAddItem,
                    contentPadding = ButtonDefaults.ButtonWithIconContentPadding
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Tambah Barang")
                }
            }
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            DesktopTable(minWidth = ItemsTableMinWidth) {
                ItemRowHeader(
                    allSelected = filteredItems.isNotEmpty() && selectedIds.size == filteredItems.size,
                    onToggleAll = {
                        selectedIds = if (selectedIds.size == filteredItems.size) {
                            emptySet()
                        } else {
                            filteredItems.mapNotNull { it.id }.toSet()
                        }
                    }
                )
                if (filteredItems.isEmpty()) {
                    Text(
                        text = if (items.isEmpty()) "Belum ada data barang." else "Tidak ada barang yang cocok.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(18.dp)
                    )
                } else {
                    filteredItems.forEach { item ->
                        ItemRow(
                            item = item,
                            selected = selectedIds.contains(item.id),
                            onToggleSelect = {
                                item.id?.let { id ->
                                    selectedIds = if (selectedIds.contains(id)) {
                                        selectedIds - id
                                    } else {
                                        selectedIds + id
                                    }
                                }
                            },
                            onDetail = { item.id?.let(onItemDetail) },
                            onEdit = { item.id?.let(onEditItem) },
                            onDelete = { item.id?.let(onDeleteItem) },
                            onPrint = { onPrintItem(item) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusFilterRow(
    selectedStatus: Int?,
    onStatusSelected: (Int?) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatusFilterChip("Semua", selectedStatus == null) { onStatusSelected(null) }
        StatusFilterChip("Tersedia", selectedStatus == 0) { onStatusSelected(0) }
        StatusFilterChip("Lelang", selectedStatus == 1) { onStatusSelected(1) }
        StatusFilterChip("Dibayar", selectedStatus == 2) { onStatusSelected(2) }
        StatusFilterChip("Diambil", selectedStatus == 3) { onStatusSelected(3) }
    }
}

@Composable
private fun StatusFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.height(DesktopDimens.ControlHeight),
        shape = MaterialTheme.shapes.small,
        colors = if (selected) {
            ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        } else {
            ButtonDefaults.outlinedButtonColors()
        },
        border = if (selected) {
            BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        },
        contentPadding = PaddingValues(horizontal = 12.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun ItemRowHeader(
    allSelected: Boolean,
    onToggleAll: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = allSelected,
            onCheckedChange = { onToggleAll() },
            modifier = Modifier.size(ItemsSelectColumn)
        )
        HeaderCell("Kode", Modifier.width(ItemsCodeColumn))
        HeaderCell("Nama", Modifier.width(ItemsNameColumn))
        HeaderCell("Status", Modifier.width(ItemsStatusColumn))
        HeaderCell("Harga Dasar", Modifier.width(ItemsBasePriceColumn))
        HeaderCell("Harga Maks", Modifier.width(ItemsMaxPriceColumn))
        HeaderCell("Pemenang", Modifier.width(ItemsBuyerColumn))
        HeaderCell("Harga Lelang", Modifier.width(ItemsAuctionPriceColumn))
        HeaderCell("Order ID", Modifier.width(ItemsOrderColumn))
        HeaderCell("Aksi", Modifier.width(ItemsActionsColumn))
    }
}

@Composable
private fun ItemRow(
    item: ItemResponse,
    selected: Boolean,
    onToggleSelect: () -> Unit,
    onDetail: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onPrint: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = selected,
            onCheckedChange = { onToggleSelect() },
            modifier = Modifier.size(ItemsSelectColumn)
        )
        BodyCell(item.codeItem.orEmpty(), Modifier.width(ItemsCodeColumn))
        BodyCell(item.nameItem.orEmpty(), Modifier.width(ItemsNameColumn))
        StatusCell(item.status, Modifier.width(ItemsStatusColumn))
        BodyCell(formatRupiah(item.basePrice), Modifier.width(ItemsBasePriceColumn))
        BodyCell(formatRupiah(item.maxPrice), Modifier.width(ItemsMaxPriceColumn))
        BodyCell(item.buyer.orEmpty(), Modifier.width(ItemsBuyerColumn))
        BodyCell(formatRupiah(item.price), Modifier.width(ItemsAuctionPriceColumn))
        BodyCell(item.orderID.orEmpty(), Modifier.width(ItemsOrderColumn))
        
        Row(
            modifier = Modifier.width(ItemsActionsColumn),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            ActionIcon(Icons.Default.Info, "Detail", onDetail)
            ActionIcon(Icons.Default.Edit, "Edit", onEdit)
            ActionIcon(Icons.Default.Print, "Cetak", onPrint)
            ActionIcon(Icons.Default.Delete, "Hapus", onDelete, color = MaterialTheme.colorScheme.error)
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
private fun ActionIcon(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(30.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = color,
            modifier = Modifier.size(17.dp)
        )
    }
}



@Composable
private fun StatusCell(status: Int?, modifier: Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        StatusBadge(
            text = statusLabel(status),
            tone = statusTone(status)
        )
    }
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

private fun statusTone(status: Int?): StatusTone {
    return when (status) {
        0 -> StatusTone.Ready
        1 -> StatusTone.Warning
        2 -> StatusTone.Paid
        3 -> StatusTone.Complete
        else -> StatusTone.Neutral
    }
}
