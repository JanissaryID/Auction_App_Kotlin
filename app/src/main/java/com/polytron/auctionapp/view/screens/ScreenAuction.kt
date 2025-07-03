package com.polytron.auctionapp.view.screens

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.polytron.auctionapp.model.Item
import com.polytron.auctionapp.utils.formatCurrencyInput
import com.polytron.auctionapp.view.components.ItemCardAuction
import com.polytron.auctionapp.viewmodel.ItemsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenAuction(
    itemsViewModel: ItemsViewModel = koinInject(),
    navScanBarcode: () -> Unit,
    navListItems: () -> Unit
) {
    val items by itemsViewModel.items.collectAsState()

    var rawAuctionPrice by remember { mutableStateOf("") }
    var auctionPrice by remember { mutableStateOf(formatCurrencyInput(rawAuctionPrice)) }

    var isSubmitting by remember { mutableStateOf(false) }

    val selectedItems = remember { mutableStateListOf<Item>() }
    val isSelectionMode = selectedItems.isNotEmpty()

    var isFabExpanded by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        itemsViewModel.fetchItems()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            Box(modifier = Modifier
                .fillMaxSize(),
//                .padding(16.dp),
                contentAlignment = Alignment.BottomEnd) {

                // Scrim overlay
                if (isFabExpanded) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                isFabExpanded = false
                            }
                    )
                }

                // Sub FAB menu
                AnimatedVisibility(
                    visible = isFabExpanded,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(bottom = 72.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Scan Barcode",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.secondary,
                                        shape = MaterialTheme.shapes.small
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                            FloatingActionButton(
                                onClick = {
                                    isFabExpanded = false
                                    navScanBarcode()
                                },
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = "Scan Barcode")
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Daftar Item",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.secondary,
                                        shape = MaterialTheme.shapes.small
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                            FloatingActionButton(
                                onClick = {
                                    isFabExpanded = false
                                    navListItems()
                                },
                                containerColor = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Daftar Item")
                            }
                        }
                    }
                }

                // Main FAB
                FloatingActionButton(
                    onClick = {
                        isFabExpanded = !isFabExpanded
                    },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = if (isFabExpanded) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = if (isFabExpanded) "Tutup Menu" else "Buka Menu"
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Lelang",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            OutlinedTextField(
                value = auctionPrice,
                onValueChange = {
                    rawAuctionPrice = it.filter { c -> c.isDigit() }
                    auctionPrice = formatCurrencyInput(rawAuctionPrice)
                },
                label = { Text("Harga Dasar") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSubmitting
            )

            if (isSelectionMode) {
                TextButton(
                    onClick = { selectedItems.clear() },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Batal Seleksi")
                }
            }

            if (items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(top = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Tidak ada item yang ditemukan",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .weight(1f)
                ) {
                    items(items) { item ->
                        ItemCardAuction(
                            item = item,
                            onNameChanged = {},
                            onPriceChanged = {}
                        )
                    }
                }
            }
        }
    }
}