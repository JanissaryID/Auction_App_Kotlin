package com.polytron.auctionapp.desktop.screens

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
import com.polytron.auctionapp.desktop.components.StatCard
import com.polytron.auctionapp.desktop.components.formatPrice
import com.polytron.auctionapp.shared.model.SharedItem
import com.polytron.auctionapp.shared.viewmodel.ItemsSharedViewModel
import org.koin.compose.koinInject

@Composable
fun AuctionScreen() {
    val sharedVm: ItemsSharedViewModel = koinInject()
    val state by sharedVm.state.collectAsState()
    
    // Filter only available items (status = 1)
    val availableItems = state.items.filter { it.status == 1 }
    
    var selectedItem by remember { mutableStateOf<SharedItem?>(null) }
    var showStartAuctionDialog by remember { mutableStateOf(false) }

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
                    text = "Auction Management",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${availableItems.size} items available for auction",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

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

        Spacer(modifier = Modifier.height(24.dp))

        // Statistics Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard(
                title = "Available",
                value = availableItems.size.toString(),
                icon = Icons.Default.Inventory,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Total Value",
                value = "Rp ${formatPrice(availableItems.sumOf { it.price?.replace(Regex("\\D"), "")?.toLongOrNull() ?: 0 })}",
                icon = Icons.Default.AttachMoney,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Items List
        if (availableItems.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Inventory,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "No items available for auction",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(availableItems) { item ->
                    AuctionItemCard(
                        item = item,
                        onStartAuction = {
                            selectedItem = item
                            showStartAuctionDialog = true
                        }
                    )
                }
            }
        }
    }

    // Start Auction Dialog
    if (showStartAuctionDialog && selectedItem != null) {
        StartAuctionDialog(
            item = selectedItem!!,
            onDismiss = { 
                showStartAuctionDialog = false
                selectedItem = null
            },
            onConfirm = { startingPrice ->
                // In real app, this would start an auction
                // For now, just show success message
                showStartAuctionDialog = false
                selectedItem = null
            }
        )
    }
}

@Composable
private fun AuctionItemCard(
    item: SharedItem,
    onStartAuction: () -> Unit
) {
    Card(
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
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Code: ${item.codeItem ?: "-"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (item.price != null) {
                    Text(
                        text = "Starting Price: ${item.price}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Button(
                onClick = onStartAuction,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Default.Gavel, null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Start Auction")
            }
        }
    }
}

@Composable
private fun StartAuctionDialog(
    item: SharedItem,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var startingPrice by remember { mutableStateOf(item.price ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Start Auction") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Start auction for: ${item.nameItem}")
                OutlinedTextField(
                    value = startingPrice,
                    onValueChange = { startingPrice = it },
                    label = { Text("Starting Price") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(startingPrice) }) {
                Text("Start")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
