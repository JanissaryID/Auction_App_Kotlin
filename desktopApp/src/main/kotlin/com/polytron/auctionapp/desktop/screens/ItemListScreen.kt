package com.polytron.auctionapp.desktop.screens

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.polytron.auctionapp.desktop.components.*
import com.polytron.auctionapp.desktop.utils.PdfExporter
import com.polytron.auctionapp.shared.model.SharedItem
import com.polytron.auctionapp.shared.viewmodel.ItemsSharedViewModel
import org.koin.compose.koinInject
import kotlinx.coroutines.launch

@Composable
fun ItemListScreen() {
    val sharedVm: ItemsSharedViewModel = koinInject()
    val state by sharedVm.state.collectAsState()
    val scope = rememberCoroutineScope()
    
    var selectedItem by remember { mutableStateOf<SharedItem?>(null) }
    var selectedSort by remember { mutableStateOf(SortOption.NAME_ASC) }
    var selectedStatus by remember { mutableStateOf(StatusFilter.ALL) }
    var selectedItems by remember { mutableStateOf(setOf<String>()) }
    var showSuccessMessage by remember { mutableStateOf<String?>(null) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<SharedItem?>(null) }
    var deletingItem by remember { mutableStateOf<SharedItem?>(null) }

    LaunchedEffect(Unit) {
        sharedVm.refreshItems()
    }
    
    // Apply filters and sorting
    val filteredAndSortedItems = remember(state.filteredItems, selectedSort, selectedStatus) {
        var items = state.filteredItems
        
        // Apply status filter
        if (selectedStatus != StatusFilter.ALL) {
            items = items.filter { it.status == selectedStatus.value }
        }
        
        // Apply sorting
        items = when (selectedSort) {
            SortOption.NAME_ASC -> items.sortedBy { it.nameItem?.lowercase() }
            SortOption.NAME_DESC -> items.sortedByDescending { it.nameItem?.lowercase() }
            SortOption.PRICE_ASC -> items.sortedBy { 
                it.price?.replace(Regex("\\D"), "")?.toLongOrNull() ?: 0 
            }
            SortOption.PRICE_DESC -> items.sortedByDescending { 
                it.price?.replace(Regex("\\D"), "")?.toLongOrNull() ?: 0 
            }
            SortOption.CODE_ASC -> items.sortedBy { it.codeItem?.lowercase() }
            SortOption.CODE_DESC -> items.sortedByDescending { it.codeItem?.lowercase() }
        }
        
        items
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Daftar Barang",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${filteredAndSortedItems.size} items ditampilkan dari ${state.items.size} total",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Add Item Button
                    Button(
                        onClick = { showCreateDialog = true }
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tambah Item")
                    }

                    ExportButton(
                        onClick = { showExportDialog = true },
                        enabled = filteredAndSortedItems.isNotEmpty()
                    )
                    
                    IconButton(
                        onClick = { sharedVm.refreshItems() }
                    ) {
                        Icon(Icons.Default.Refresh, "Refresh")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            // Bulk Action Bar
            BulkActionBar(
                selectedCount = selectedItems.size,
                onClearSelection = { selectedItems = emptySet() },
                onBulkAction = { action ->
                    scope.launch {
                        when (action) {
                            "mark_paid" -> {
                                selectedItems.forEach { id ->
                                    sharedVm.updateItemStatus(id, 2) {}
                                }
                                showSuccessMessage = "${selectedItems.size} item ditandai dibayar"
                                selectedItems = emptySet()
                            }
                            "mark_taken" -> {
                                selectedItems.forEach { id ->
                                    sharedVm.updateItemStatus(id, 3) {}
                                }
                                showSuccessMessage = "${selectedItems.size} item ditandai diambil"
                                selectedItems = emptySet()
                            }
                            "delete" -> {
                                selectedItems.forEach { id ->
                                    sharedVm.deleteItem(id) {}
                                }
                                showSuccessMessage = "${selectedItems.size} item dihapus"
                                selectedItems = emptySet()
                            }
                        }
                    }
                }
            )
            
            if (selectedItems.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { sharedVm.updateSearchQuery(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Cari berdasarkan nama atau kode...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (state.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { sharedVm.updateSearchQuery("") }) {
                            Icon(Icons.Default.Clear, "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))
            
            // Filter and Sort Bar
            FilterAndSortBar(
                selectedSort = selectedSort,
                onSortChange = { selectedSort = it },
                selectedStatus = selectedStatus,
                onStatusChange = { selectedStatus = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Items List
            if (state.isLoading) {
                LoadingAnimation()
            } else if (filteredAndSortedItems.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.SearchOff,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Tidak ada item ditemukan",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (selectedStatus != StatusFilter.ALL || state.searchQuery.isNotEmpty()) {
                            TextButton(onClick = {
                                selectedStatus = StatusFilter.ALL
                                sharedVm.updateSearchQuery("")
                            }) {
                                Text("Reset Filter")
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredAndSortedItems, key = { it.id }) { item ->
                        AnimatedItemCard(visible = true) {
                            SelectableItemCard(
                                item = item,
                                isSelected = selectedItems.contains(item.id),
                                onSelect = {
                                    selectedItems = if (selectedItems.contains(item.id)) {
                                        selectedItems - item.id
                                    } else {
                                        selectedItems + item.id
                                    }
                                },
                                onClick = { selectedItem = item }
                            )
                        }
                    }
                }
            }
        }
        
        // Success Message
        showSuccessMessage?.let { message ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                SuccessAnimation(
                    message = message,
                    onDismiss = { showSuccessMessage = null }
                )
            }
        }
    }

    // Detail Dialog with Edit/Delete
    selectedItem?.let { item ->
        ItemDetailDialog(
            item = item,
            onDismiss = { selectedItem = null },
            onEdit = {
                editingItem = item
                selectedItem = null
            },
            onDelete = {
                deletingItem = item
                selectedItem = null
            }
        )
    }

    // Create Dialog
    if (showCreateDialog) {
        ItemFormDialog(
            item = null,
            onDismiss = { showCreateDialog = false },
            onSave = { newItem ->
                sharedVm.createItem(newItem) {
                    showSuccessMessage = "Item berhasil ditambahkan"
                    showCreateDialog = false
                }
            }
        )
    }

    // Edit Dialog
    editingItem?.let { item ->
        ItemFormDialog(
            item = item,
            onDismiss = { editingItem = null },
            onSave = { updatedItem ->
                sharedVm.updateItem(item.id, updatedItem) {
                    showSuccessMessage = "Item berhasil diperbarui"
                    editingItem = null
                }
            }
        )
    }

    // Delete Confirmation
    deletingItem?.let { item ->
        DeleteConfirmationDialog(
            itemName = item.nameItem ?: "Unknown",
            onDismiss = { deletingItem = null },
            onConfirm = {
                sharedVm.deleteItem(item.id) {
                    showSuccessMessage = "Item berhasil dihapus"
                    deletingItem = null
                }
            }
        )
    }
    
    // Export Dialog
    if (showExportDialog) {
        ExportDialog(
            items = filteredAndSortedItems,
            onDismiss = { showExportDialog = false },
            onExport = { filePath ->
                showSuccessMessage = "Laporan berhasil diekspor ke:\n$filePath"
                showExportDialog = false
            }
        )
    }
}

@Composable
private fun SelectableItemCard(
    item: SharedItem,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onSelect() }
                )
                
                Column(
                    modifier = Modifier.weight(1f).clickable { onClick() }
                ) {
                    Text(
                        text = item.nameItem ?: "Unknown",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Kode: ${item.codeItem ?: "-"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = item.price ?: "Rp 0",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                StatusBadge(status = item.status)
            }
        }
    }
}

@Composable
private fun ExportDialog(
    items: List<SharedItem>,
    onDismiss: () -> Unit,
    onExport: (String) -> Unit
) {
    var exportType by remember { mutableStateOf("GENERAL") }
    var isExporting by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Export Laporan") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Pilih jenis laporan yang ingin diekspor:")
                
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = exportType == "GENERAL",
                            onClick = { exportType = "GENERAL" }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Laporan Umum (${items.size} items)")
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = exportType == "PAYMENT",
                            onClick = { exportType = "PAYMENT" }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Laporan Pembayaran")
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = exportType == "PICKUP",
                            onClick = { exportType = "PICKUP" }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Laporan Pengambilan")
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = exportType == "AUCTION",
                            onClick = { exportType = "AUCTION" }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Laporan Lelang")
                    }
                }
                
                if (isExporting) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isExporting = true
                    val result = PdfExporter.exportToReport(items, exportType)
                    result.onSuccess { filePath ->
                        onExport(filePath)
                    }.onFailure {
                        // Handle error
                        onDismiss()
                    }
                },
                enabled = !isExporting
            ) {
                if (isExporting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Export")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isExporting
            ) {
                Text("Batal")
            }
        }
    )
}

@Composable
private fun GroupHeader(
    title: String,
    count: Int
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "$count item",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ItemCard(
    item: SharedItem,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.nameItem ?: "Unknown",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Kode: ${item.codeItem ?: "-"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = item.price ?: "Rp 0",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                StatusBadge(status = item.status)
            }
        }
    }
}

@Composable
private fun StatusBadge(status: Int?) {
    val (text, color) = when (status) {
        1 -> "Tersedia" to MaterialTheme.colorScheme.primary
        2 -> "Dibayar" to MaterialTheme.colorScheme.tertiary
        3 -> "Diambil" to MaterialTheme.colorScheme.secondary
        else -> "Unknown" to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.2f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}
