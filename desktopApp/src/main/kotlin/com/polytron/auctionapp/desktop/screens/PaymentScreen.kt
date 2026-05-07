package com.polytron.auctionapp.desktop.screens

import androidx.compose.animation.*
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
fun PaymentScreen() {
    val sharedVm: ItemsSharedViewModel = koinInject()
    val state by sharedVm.state.collectAsState()
    val scope = rememberCoroutineScope()
    
    // Filter unpaid items (status = 1)
    val unpaidItems = state.items.filter { it.status == 1 }
    // Filter paid items (status = 2)
    val paidItems = state.items.filter { it.status == 2 }
    
    var selectedTab by remember { mutableStateOf(0) }
    var selectedItem by remember { mutableStateOf<SharedItem?>(null) }
    var showPaymentDialog by remember { mutableStateOf(false) }
    var selectedSort by remember { mutableStateOf(SortOption.NAME_ASC) }
    var showSuccessMessage by remember { mutableStateOf<String?>(null) }
    var showExportDialog by remember { mutableStateOf(false) }
    
    // Apply sorting
    val sortedUnpaidItems = remember(unpaidItems, selectedSort) {
        sortItems(unpaidItems, selectedSort)
    }
    
    val sortedPaidItems = remember(paidItems, selectedSort) {
        sortItems(paidItems, selectedSort)
    }

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
                    text = "Payment Management",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${unpaidItems.size} unpaid, ${paidItems.size} paid",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExportButton(
                        onClick = { showExportDialog = true },
                        enabled = unpaidItems.isNotEmpty() || paidItems.isNotEmpty()
                    )
                    
                    Button(
                        onClick = { sharedVm.refreshItems() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Icon(Icons.Default.Refresh, null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Refresh")
                    }
                }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Statistics
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard(
                title = "Unpaid",
                value = unpaidItems.size.toString(),
                icon = Icons.Default.PendingActions,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Paid",
                value = paidItems.size.toString(),
                icon = Icons.Default.CheckCircle,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Total Revenue",
                value = "Rp ${formatPrice(paidItems.sumOf { it.price?.replace(Regex("\\D"), "")?.toLongOrNull() ?: 0 })}",
                icon = Icons.Default.AttachMoney,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Tabs
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Unpaid (${unpaidItems.size})") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Paid (${paidItems.size})") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        // Sort Options
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Urutkan:",
                style = MaterialTheme.typography.labelLarge
            )
            
            var showSortMenu by remember { mutableStateOf(false) }
            
            FilterChip(
                selected = true,
                onClick = { showSortMenu = true },
                label = { Text(selectedSort.label) },
                leadingIcon = {
                    Icon(
                        Icons.Default.SwapVert,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
            
            DropdownMenu(
                expanded = showSortMenu,
                onDismissRequest = { showSortMenu = false }
            ) {
                SortOption.entries.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.label) },
                        onClick = {
                            selectedSort = option
                            showSortMenu = false
                        },
                        leadingIcon = {
                            if (selectedSort == option) {
                                Icon(Icons.Default.Check, null, modifier = Modifier.size(20.dp))
                            }
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Content
        val itemsToShow = if (selectedTab == 0) sortedUnpaidItems else sortedPaidItems

        if (itemsToShow.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        if (selectedTab == 0) Icons.Default.Payment else Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (selectedTab == 0) "No unpaid items" else "No paid items",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(itemsToShow, key = { it.id }) { item ->
                    AnimatedItemCard(visible = true) {
                        PaymentItemCard(
                            item = item,
                            isPaid = selectedTab == 1,
                            onMarkAsPaid = {
                                selectedItem = item
                                showPaymentDialog = true
                            }
                        )
                    }
                }
            }
        }
        
        // Success Message Overlay
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

    // Payment Confirmation Dialog
    if (showPaymentDialog && selectedItem != null) {
        PaymentConfirmationDialog(
            item = selectedItem!!,
            onDismiss = { 
                showPaymentDialog = false
                selectedItem = null
            },
            onConfirm = {
                sharedVm.updateItemStatus(selectedItem!!.id, 2) {
                    showSuccessMessage = "Item berhasil ditandai dibayar"
                    showPaymentDialog = false
                    selectedItem = null
                }
            }
        )
    }
    
    // Export Dialog
    if (showExportDialog) {
        ExportPaymentDialog(
            unpaidItems = unpaidItems,
            paidItems = paidItems,
            onDismiss = { showExportDialog = false },
            onExport = { filePath ->
                showSuccessMessage = "Laporan berhasil diekspor"
                showExportDialog = false
            }
        )
    }
}

private fun sortItems(items: List<SharedItem>, sortOption: SortOption): List<SharedItem> {
    return when (sortOption) {
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
}

@Composable
private fun PaymentItemCard(
    item: SharedItem,
    isPaid: Boolean,
    onMarkAsPaid: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPaid) 
                MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
            else 
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
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
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Code: ${item.codeItem ?: "-"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (item.buyer != null) {
                    Text(
                        text = "Buyer: ${item.buyer}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "Price: ${item.price ?: "Rp 0"}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (!isPaid) {
                Button(
                    onClick = onMarkAsPaid,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Icon(Icons.Default.Payment, null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Mark as Paid")
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                        Text(
                            text = "Paid",
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentConfirmationDialog(
    item: SharedItem,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirm Payment") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Mark this item as paid?")
                Text(
                    text = "Item: ${item.nameItem}",
                    fontWeight = FontWeight.Bold
                )
                Text("Price: ${item.price ?: "Rp 0"}")
                if (item.buyer != null) {
                    Text("Buyer: ${item.buyer}")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary
                )
            ) {
                Text("Confirm Payment")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


@Composable
private fun ExportPaymentDialog(
    unpaidItems: List<SharedItem>,
    paidItems: List<SharedItem>,
    onDismiss: () -> Unit,
    onExport: (String) -> Unit
) {
    var isExporting by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Export Laporan Pembayaran") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Laporan akan mencakup:")
                Text("• Belum Dibayar: ${unpaidItems.size} items")
                Text("• Sudah Dibayar: ${paidItems.size} items")
                
                val totalRevenue = paidItems.sumOf { 
                    it.price?.replace(Regex("\\D"), "")?.toLongOrNull() ?: 0 
                }
                Text("• Total Pendapatan: Rp ${formatPrice(totalRevenue)}")
                
                if (isExporting) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isExporting = true
                    val allItems = unpaidItems + paidItems
                    val result = PdfExporter.exportToReport(allItems, "PAYMENT")
                    result.onSuccess { filePath ->
                        onExport(filePath)
                    }.onFailure {
                        onDismiss()
                    }
                },
                enabled = !isExporting
            ) {
                if (isExporting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
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
