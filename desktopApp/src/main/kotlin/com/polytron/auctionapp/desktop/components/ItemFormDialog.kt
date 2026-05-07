package com.polytron.auctionapp.desktop.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.polytron.auctionapp.shared.model.SharedItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemFormDialog(
    item: SharedItem? = null, // null = create, non-null = edit
    onDismiss: () -> Unit,
    onSave: (SharedItem) -> Unit
) {
    var nameItem by remember { mutableStateOf(item?.nameItem ?: "") }
    var codeItem by remember { mutableStateOf(item?.codeItem ?: "") }
    var buyer by remember { mutableStateOf(item?.buyer ?: "") }
    var price by remember { mutableStateOf(item?.price ?: "") }
    var orderId by remember { mutableStateOf(item?.orderId ?: "") }
    var status by remember { mutableStateOf(item?.status ?: 1) }
    
    var nameError by remember { mutableStateOf(false) }
    var codeError by remember { mutableStateOf(false) }

    val isEditMode = item != null
    val title = if (isEditMode) "Edit Item" else "Create New Item"

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.width(500.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Close")
                    }
                }

                HorizontalDivider()

                // Form Fields
                OutlinedTextField(
                    value = nameItem,
                    onValueChange = { 
                        nameItem = it
                        nameError = false
                    },
                    label = { Text("Item Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = nameError,
                    supportingText = if (nameError) {
                        { Text("Item name is required") }
                    } else null,
                    singleLine = true
                )

                OutlinedTextField(
                    value = codeItem,
                    onValueChange = { 
                        codeItem = it
                        codeError = false
                    },
                    label = { Text("Item Code *") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = codeError,
                    supportingText = if (codeError) {
                        { Text("Item code is required") }
                    } else null,
                    singleLine = true
                )

                OutlinedTextField(
                    value = buyer,
                    onValueChange = { buyer = it },
                    label = { Text("Buyer") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g., 150000") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = orderId,
                    onValueChange = { orderId = it },
                    label = { Text("Order ID") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g., Order-1001") },
                    singleLine = true
                )

                // Status Dropdown
                var expanded by remember { mutableStateOf(false) }
                val statusOptions = mapOf(
                    1 to "Tersedia",
                    2 to "Dibayar",
                    3 to "Diambil"
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = statusOptions[status] ?: "Unknown",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Status") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        statusOptions.forEach { (value, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    status = value
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                HorizontalDivider()

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            // Validation
                            if (nameItem.isBlank()) {
                                nameError = true
                                return@Button
                            }
                            if (codeItem.isBlank()) {
                                codeError = true
                                return@Button
                            }

                            // Create/Update item
                            val newItem = SharedItem(
                                id = item?.id ?: "",
                                nameItem = nameItem,
                                codeItem = codeItem,
                                buyer = buyer.ifBlank { null },
                                price = price.ifBlank { null },
                                orderId = orderId.ifBlank { null },
                                status = status
                            )
                            onSave(newItem)
                        }
                    ) {
                        Text(if (isEditMode) "Update" else "Create")
                    }
                }
            }
        }
    }
}

@Composable
fun DeleteConfirmationDialog(
    itemName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Item") },
        text = { 
            Text("Are you sure you want to delete \"$itemName\"? This action cannot be undone.") 
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
