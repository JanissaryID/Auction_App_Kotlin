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
import androidx.compose.foundation.lazy.items as lazyItems
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Checkbox
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import com.polytron.auctionapp.desktop.components.BodyCell
import com.polytron.auctionapp.desktop.components.DesktopButton as Button
import com.polytron.auctionapp.desktop.components.DesktopDimens
import com.polytron.auctionapp.desktop.components.DesktopOutlinedButton as OutlinedButton
import com.polytron.auctionapp.desktop.components.DesktopLazyTable
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

private data class StatusFilterOption(
    val status: Int?,
    val label: String
)

private val StatusFilterOptions = listOf(
    StatusFilterOption(null, "Semua"),
    StatusFilterOption(0, "Tersedia"),
    StatusFilterOption(1, "Lelang"),
    StatusFilterOption(2, "Dibayar"),
    StatusFilterOption(3, "Diambil")
)

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
    onBulkPrint: (List<ItemResponse>) -> Unit,
    onExportExcel: (List<ItemResponse>) -> Unit,
    onImportExcel: () -> Unit,
    isImportingExcel: Boolean = false
) {
    var searchQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf<Int?>(null) }
    var selectedIds by remember { mutableStateOf(setOf<String>()) }
    val itemIds = remember(items) { items.mapNotNull { it.id }.toSet() }

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
    val filteredItemIds = remember(filteredItems) { filteredItems.mapNotNull { it.id }.toSet() }
    val selectedItemsForActions = remember(items, selectedIds) {
        items.filter { item ->
            item.id != null && selectedIds.contains(item.id)
        }
    }
    val selectedCount = selectedItemsForActions.size

    LaunchedEffect(itemIds) {
        val validSelectedIds = selectedIds.intersect(itemIds)
        if (validSelectedIds != selectedIds) {
            selectedIds = validSelectedIds
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (selectedCount > 0) {
            SelectedItemsToolbar(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                statusFilter = statusFilter,
                onStatusSelected = { statusFilter = it },
                selectedCount = selectedCount,
                onBulkPrint = { onBulkPrint(selectedItemsForActions) },
                onBulkDelete = {
                    onBulkDelete(selectedItemsForActions.mapNotNull { it.id })
                    selectedIds = emptySet()
                },
                onClearSelection = { selectedIds = emptySet() }
            )
        } else {
            DesktopToolbar(
                leading = {
                    ItemSearchAndStatusFilters(
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        statusFilter = statusFilter,
                        onStatusSelected = { statusFilter = it }
                    )
                },
                actions = {
                    Spacer(Modifier.weight(1f))

                    ExcelActionsDropdown(
                        isImportingExcel = isImportingExcel,
                        onImportExcel = onImportExcel,
                        onExportExcel = { onExportExcel(filteredItems) }
                    )

                    Spacer(Modifier.width(8.dp))

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
        }

        Surface(
            modifier = Modifier.fillMaxWidth().weight(1f),
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            DesktopLazyTable(minWidth = ItemsTableMinWidth) {
                item {
                    ItemRowHeader(
                        allSelected = filteredItemIds.isNotEmpty() && filteredItemIds.all { it in selectedIds },
                        onToggleAll = {
                            selectedIds = if (filteredItemIds.isNotEmpty() && filteredItemIds.all { it in selectedIds }) {
                                selectedIds - filteredItemIds
                            } else {
                                selectedIds + filteredItemIds
                            }
                        }
                    )
                }
                if (filteredItems.isEmpty()) {
                    item {
                        Text(
                            text = if (items.isEmpty()) "Belum ada data barang." else "Tidak ada barang yang cocok.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(18.dp)
                        )
                    }
                } else {
                    lazyItems(
                        items = filteredItems,
                        key = { item -> item.id ?: item.codeItem ?: item.hashCode().toString() }
                    ) { item ->
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
private fun ItemSearchAndStatusFilters(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    statusFilter: Int?,
    onStatusSelected: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
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

        StatusFilterDropdown(
            selectedStatus = statusFilter,
            onStatusSelected = onStatusSelected
        )
    }
}

@Composable
private fun SelectedItemsToolbar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    statusFilter: Int?,
    onStatusSelected: (Int?) -> Unit,
    selectedCount: Int,
    onBulkPrint: () -> Unit,
    onBulkDelete: () -> Unit,
    onClearSelection: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Cari barang...", style = MaterialTheme.typography.bodyMedium) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.weight(1f).height(DesktopDimens.ControlHeight),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant
                )
            )
            StatusFilterDropdown(
                selectedStatus = statusFilter,
                onStatusSelected = onStatusSelected
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$selectedCount barang dipilih",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            OutlinedButton(
                onClick = onBulkPrint,
                contentPadding = ButtonDefaults.ButtonWithIconContentPadding
            ) {
                Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Cetak ($selectedCount)")
            }
            OutlinedButton(
                onClick = onBulkDelete,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Hapus Terpilih ($selectedCount)")
            }
            OutlinedButton(
                onClick = onClearSelection,
                contentPadding = ButtonDefaults.ButtonWithIconContentPadding
            ) {
                Text("Batal")
            }
        }
    }
}

@Composable
private fun ExcelActionsDropdown(
    isImportingExcel: Boolean,
    onImportExcel: () -> Unit,
    onExportExcel: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(
            onClick = { if (!isImportingExcel) expanded = true },
            enabled = !isImportingExcel,
            contentPadding = ButtonDefaults.ButtonWithIconContentPadding
        ) {
            if (isImportingExcel) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(8.dp))
            Text(if (isImportingExcel) "Import..." else "Excel")
            Spacer(Modifier.width(4.dp))
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(18.dp))
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Import Excel") },
                leadingIcon = { Icon(Icons.Default.UploadFile, contentDescription = null) },
                onClick = {
                    expanded = false
                    onImportExcel()
                }
            )
            DropdownMenuItem(
                text = { Text("Export Excel") },
                leadingIcon = { Icon(Icons.Default.Download, contentDescription = null) },
                onClick = {
                    expanded = false
                    onExportExcel()
                }
            )
        }
    }
}

@Composable
private fun StatusFilterDropdown(
    selectedStatus: Int?,
    onStatusSelected: (Int?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = StatusFilterOptions
        .firstOrNull { it.status == selectedStatus }
        ?.label
        ?: "Semua"

    Box {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier
                .width(180.dp)
                .height(DesktopDimens.ControlHeight),
            shape = MaterialTheme.shapes.small,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            contentPadding = ButtonDefaults.ButtonWithIconContentPadding
        ) {
            Text(
                text = "Status: $selectedLabel",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            StatusFilterOptions.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label) },
                    onClick = {
                        onStatusSelected(option.status)
                        expanded = false
                    }
                )
            }
        }
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
