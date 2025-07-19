package com.polytron.auctionapp.view.screens

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.polytron.auctionapp.model.ItemResponse
import com.polytron.auctionapp.utils.BluetoothHelper
import com.polytron.auctionapp.utils.BluetoothPrinter
import com.polytron.auctionapp.view.components.PrinterListDialog
import com.polytron.auctionapp.view.components.TopAppBarCustom
import com.polytron.auctionapp.view.components.itemcard.ItemCardPayment
import com.polytron.auctionapp.viewmodel.MainViewModel
import org.koin.compose.koinInject

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenListPayment(
    mainViewModel: MainViewModel = koinInject(),
    bluetoothHelper: BluetoothHelper,
    navBack: () -> Unit,
) {
    val context = LocalContext.current

    val items by mainViewModel.items.collectAsState()
    val filteredItemsStatTwo = items.filter { it.status == 2 }

    var searchQuery by remember { mutableStateOf("") }

    val selectedItemsState by mainViewModel.selectedItems.collectAsState()
    val printerDevice by mainViewModel.selectedPrinter.collectAsState()
    val showBluetoothDevice by mainViewModel.showBluetoothDevice.collectAsState()
    val selectedItems = remember { mutableStateListOf<ItemResponse>() }
    var isInitialized by remember { mutableStateOf(false) }

    LaunchedEffect(selectedItemsState) {
        if (!isInitialized) {
            selectedItems.clear()
            selectedItems.addAll(selectedItemsState)
            isInitialized = true
        }
    }

    val filteredItems = filteredItemsStatTwo.filter {
        it.nameItem?.contains(searchQuery, ignoreCase = true) == true ||
                it.codeItem?.contains(searchQuery, ignoreCase = true) == true ||
                it.buyer?.contains(searchQuery, ignoreCase = true) == true
    }

    val groupedFilteredItems = filteredItems.groupBy { it.orderID.orEmpty() }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBarCustom(
                title = "Daftar Pembayaran",
                onBack = { navBack() },
                showRefresh = true,
                onRefresh = { mainViewModel.fetchItems() },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Cari berdasarkan nama atau kode") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                singleLine = true
            )

            if (filteredItems.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
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
                    contentPadding = PaddingValues(vertical = 16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    groupedFilteredItems.forEach { (orderId, itemList) ->
                        item {
                            ItemCardPayment(
                                orderId = orderId,
                                items = itemList,
                                takeItemScreen = false,
                                onClick = {
                                    val printer = BluetoothPrinter()
                                    val device = printerDevice

                                    if (device != null) {
                                        printer.printBarcodeReceipt(
                                            item = itemList,
                                            payment = itemList[0].typePayment.orEmpty(),
                                            orderID = orderId,
                                            device = device
                                        )
                                    } else {
                                        Toast.makeText(context, "Belum ada printer yang terhubung", Toast.LENGTH_SHORT).show()
                                        bluetoothHelper.requestBluetooth {
                                            mainViewModel.showBluetoothDevice(true)
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showBluetoothDevice) {
        PrinterListDialog(
            bluetoothHelper = bluetoothHelper,
            onPrinterSelected = { device ->
                mainViewModel.setSelectedPrinter(device)
                mainViewModel.showBluetoothDevice(false)

                val printer = BluetoothPrinter()
//                printer.testPrinter(device)
            },
            onDismiss = {
                mainViewModel.showBluetoothDevice(false)
            }
        )
    }
}