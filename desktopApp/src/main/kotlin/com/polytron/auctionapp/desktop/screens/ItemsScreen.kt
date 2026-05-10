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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
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
import com.polytron.auctionapp.desktop.components.DesktopToolbar
import com.polytron.auctionapp.desktop.components.HeaderCell
import com.polytron.auctionapp.desktop.components.StatusBadge
import com.polytron.auctionapp.desktop.components.StatusTone
import com.polytron.auctionapp.domain.model.ItemResponse
import com.polytron.auctionapp.utils.formatRupiah

@Composable
fun ItemsScreen(
    items: List<ItemResponse>,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onAddItem: () -> Unit,
    onEditItem: (String) -> Unit,
    onDeleteItem: (String) -> Unit,
    onItemDetail: (String) -> Unit,
    onBulkDelete: (List<String>) -> Unit
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
                    placeholder = { Text("Cari barang...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.width(300.dp).height(40.dp),
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .verticalScroll(rememberScrollState())
            ) {
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
                            onDelete = { item.id?.let(onDeleteItem) }
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
        modifier = Modifier.height(40.dp),
        shape = MaterialTheme.shapes.extraLarge,
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
            .widthIn(min = 1200.dp).fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = allSelected,
            onCheckedChange = { onToggleAll() },
            modifier = Modifier.size(36.dp)
        )
        HeaderCell("Kode", Modifier.width(100.dp))
        HeaderCell("Nama", Modifier.weight(2f))
        HeaderCell("Status", Modifier.width(100.dp))
        HeaderCell("Harga Dasar", Modifier.width(120.dp))
        HeaderCell("Harga Maks", Modifier.width(120.dp))
        HeaderCell("Pemenang", Modifier.weight(1.2f))
        HeaderCell("Harga Lelang", Modifier.width(120.dp))
        HeaderCell("Order ID", Modifier.width(140.dp))
        HeaderCell("Aksi", Modifier.width(120.dp))
    }
}

@Composable
private fun ItemRow(
    item: ItemResponse,
    selected: Boolean,
    onToggleSelect: () -> Unit,
    onDetail: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .widthIn(min = 1200.dp).fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = selected,
            onCheckedChange = { onToggleSelect() },
            modifier = Modifier.size(36.dp)
        )
        BodyCell(item.codeItem.orEmpty(), Modifier.width(100.dp))
        BodyCell(item.nameItem.orEmpty(), Modifier.weight(2f))
        StatusCell(item.status, Modifier.width(100.dp))
        BodyCell(formatRupiah(item.basePrice), Modifier.width(120.dp))
        BodyCell(formatRupiah(item.maxPrice), Modifier.width(120.dp))
        BodyCell(item.buyer.orEmpty(), Modifier.weight(1.2f))
        BodyCell(formatRupiah(item.price), Modifier.width(120.dp))
        BodyCell(item.orderID.orEmpty(), Modifier.width(140.dp))
        
        Row(
            modifier = Modifier.width(120.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ActionIcon(Icons.Default.Info, "Detail", onDetail)
            ActionIcon(Icons.Default.Edit, "Edit", onEdit)
            ActionIcon(Icons.Default.Delete, "Hapus", onDelete, color = MaterialTheme.colorScheme.error)
        }
    }
    Spacer(
        modifier = Modifier
            .widthIn(min = 1200.dp).fillMaxWidth()
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
        modifier = Modifier.size(32.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = color,
            modifier = Modifier.size(18.dp)
        )
    }
}



@Composable
private fun StatusCell(status: Int?, modifier: Modifier) {
    StatusBadge(
        text = statusLabel(status),
        tone = statusTone(status),
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

private fun statusTone(status: Int?): StatusTone {
    return when (status) {
        0 -> StatusTone.Ready
        1 -> StatusTone.Warning
        2 -> StatusTone.Paid
        3 -> StatusTone.Complete
        else -> StatusTone.Neutral
    }
}
