package com.polytron.auctionapp.view.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.polytron.auctionapp.utils.formatCurrencyInput
import com.polytron.auctionapp.view.components.EmptyItemState
import com.polytron.auctionapp.view.components.FabWithSubmenu
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
    navListItems: () -> Unit,
    onBack: () -> Unit,
) {
    var rawAuctionPrice by remember { mutableStateOf("") }
    var auctionPrice by remember { mutableStateOf(formatCurrencyInput(rawAuctionPrice)) }
    var isSubmitting by remember { mutableStateOf(false) }
    var isFabExpanded by remember { mutableStateOf(false) }

    val selectedItems by itemsViewModel.selectedItems.collectAsState()
    val editingBuyers by itemsViewModel.editingBuyers.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val editingPrices by itemsViewModel.editingPrices.collectAsState()

    val isAuctionPriceValid = rawAuctionPrice.isNotBlank()
    val isAllBuyerFilled = selectedItems.all {
        val edited = editingBuyers[it.id]
        !edited.isNullOrBlank()
    }
    val isSimpanEnabled = selectedItems.isNotEmpty() && isAuctionPriceValid && isAllBuyerFilled

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Lelang") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    titleContentColor = MaterialTheme.colorScheme.primary,
                    navigationIconContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        bottomBar = {
            if (selectedItems.isNotEmpty()) {
                BottomAppBar(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "${selectedItems.size} barang dipilih",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = {
                            isSubmitting = true
                            coroutineScope.launch {
                                val fallbackPrice = rawAuctionPrice.toIntOrNull()?.toString()

                                selectedItems.forEach { item ->
                                    val finalBuyer = editingBuyers[item.id].orEmpty().ifBlank { item.buyer.orEmpty() }
                                    val finalPrice = editingPrices[item.id].orEmpty().ifBlank { fallbackPrice.orEmpty() }

                                    val updatedItem = item.copy(
                                        buyer = finalBuyer,
                                        price = finalPrice,
                                        status = 1
                                    )
                                    itemsViewModel.patchItem(item.id!!, updatedItem)
                                    delay(300) // opsional agar smooth
                                }

                                isSubmitting = false
                                itemsViewModel.clearSelectedItems()
                                rawAuctionPrice = ""
                                auctionPrice = ""
                            }
                        },
                        enabled = !isSubmitting && isSimpanEnabled,
                        modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Simpan")
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            FabWithSubmenu(
                isFabExpanded = isFabExpanded,
                onFabToggle = { isFabExpanded = !isFabExpanded },
                onDismissRequest = { isFabExpanded = false },
                navScanBarcode = navScanBarcode,
                navListItems = navListItems
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = auctionPrice,
                onValueChange = {
                    rawAuctionPrice = it.filter(Char::isDigit)
                    auctionPrice = formatCurrencyInput(rawAuctionPrice)
                },
                label = { Text("Harga Lelang") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                enabled = !isSubmitting
            )

            if (selectedItems.isEmpty()) {
                EmptyItemState()
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    modifier = Modifier.fillMaxWidth().weight(1f)
                ) {
                    items(selectedItems) { item ->
                        ItemCardAuction(
                            item = item,
                            currentBuyer = editingBuyers[item.id].orEmpty(),
                            currentPrice = editingPrices[item.id].orEmpty(),
                            onNameChanged = { newName ->
                                itemsViewModel.updateEditingBuyer(item.id!!, newName)
                            },
                            onPriceChanged = { newPrice ->
                                itemsViewModel.updateEditingPrice(item.id!!, newPrice)
                            },
                            onCancelPriceInput = {
                                itemsViewModel.clearEditingForItem(item.id!!)
                            }
                        )
                    }
                }
            }
        }
    }
}
