package com.polytron.auctionapp.view.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.unit.dp
import com.polytron.auctionapp.utils.formatRupiah
import com.polytron.auctionapp.utils.generateRandomAlphanumeric
import com.polytron.auctionapp.view.components.EmptyItemState
import com.polytron.auctionapp.view.components.FabWithSubmenu
import com.polytron.auctionapp.view.components.PaymentBottomSheet
import com.polytron.auctionapp.view.components.ReceiptCard
import com.polytron.auctionapp.viewmodel.ItemsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenPayment(
    itemsViewModel: ItemsViewModel = koinInject(),
    navScanBarcode: () -> Unit,
    navListItems: () -> Unit,
    navListPayment: () -> Unit,
    onBack: () -> Unit,
) {
    var isSubmitting by remember { mutableStateOf(false) }
    var isFabExpanded by remember { mutableStateOf(false) }

    val selectedItems by itemsViewModel.selectedItems.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var showSheet by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Pembayaran") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(onClick = { navListPayment() }) {
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = "List Payment"
                        )
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
                            showSheet = true
                        },
                        enabled = true,
                        modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                    ) {
                        Text("Proses")
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
            if (selectedItems.isEmpty()) {
                EmptyItemState()
            } else {
                ReceiptCard(selectedItems = selectedItems)
            }
        }
    }

    if (showSheet) {

        val total = selectedItems.sumOf { item ->
            item.price
                ?.replace(Regex("\\D"), "") // hapus semua non-digit (misalnya titik)
                ?.toLongOrNull()
                ?: 0L
        }

        val formattedPrice = remember(total) {
            formatRupiah(total.toString())
        }

        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        ) {
            PaymentBottomSheet(
                total = formattedPrice,
                onDismiss = { showSheet = false },
                onPay = {
                    // proses bayar dengan selectedMethod
                    val orderID = "Order-${generateRandomAlphanumeric()}"
                    coroutineScope.launch {
                        selectedItems.forEach { item ->
                            val updatedItem = item.copy(
                                status = 2,
                                orderID = orderID,
                                typePayment = it.label
                            )
                            itemsViewModel.patchItem(item.id!!, updatedItem)
                            delay(300) // opsional agar smooth
                        }
                        itemsViewModel.clearSelectedItems()
                        showSheet = false
                    }
                }
            )
        }
    }
}