package com.polytron.auctionapp.view.screens

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.polytron.auctionapp.bluetooth.BluetoothHelper
import com.polytron.auctionapp.bluetooth.BluetoothPrinter
import com.polytron.auctionapp.utils.formatRupiah
import com.polytron.auctionapp.utils.generateRandomAlphanumeric
import com.polytron.auctionapp.view.components.EmptyItemState
import com.polytron.auctionapp.view.components.PrinterListDialog
import com.polytron.auctionapp.view.components.ReceiptCard
import com.polytron.auctionapp.view.components.SelectedItemsBottomBar
import com.polytron.auctionapp.view.components.TopAppBarCustom
import com.polytron.auctionapp.view.components.bottomsheet.PaymentBottomSheet
import com.polytron.auctionapp.view.components.fab.FabWithSubmenu
import com.polytron.auctionapp.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenPayment(
    mainViewModel: MainViewModel = koinInject(),
    bluetoothHelper: BluetoothHelper,
    navScanBarcode: () -> Unit,
    navListItems: () -> Unit,
    navListPayment: () -> Unit,
    navBack: () -> Unit,
) {
    val context = LocalContext.current

    var isFabExpanded by remember { mutableStateOf(false) }

    val selectedItems by mainViewModel.selectedItems.collectAsState()
    val printerDevice by mainViewModel.selectedPrinter.collectAsState()
    val showBluetoothDevice by mainViewModel.showBluetoothDevice.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var showSheet by remember { mutableStateOf(false) }

    BackHandler {
        navBack()
        mainViewModel.clearSelectedItems()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBarCustom(
                title = "Pembayaran",
                onBack = {
                    navBack()
                    mainViewModel.clearSelectedItems()
                },
                additionalActions = {
                    IconButton(onClick = { navListPayment() }) {
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = "List Payment"
                        )
                    }
                }
            )
        },
        bottomBar = {
            SelectedItemsBottomBar(
                selectedCount = selectedItems.size,
                buttonText = "Proses",
                onClick = {
                    showSheet = true
                }
            )
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
                ReceiptCard(selectedItems = selectedItems){
                        item -> mainViewModel.removeSelectedItem(item)
                }
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
                    val device = printerDevice
                    if (device != null) {
                        val orderID = "Order-${generateRandomAlphanumeric()}"
                        coroutineScope.launch {
                            selectedItems.forEach { item ->
                                val updatedItem = item.copy(
                                    status = 2,
                                    orderID = orderID,
                                    typePayment = it.label
                                )
                                mainViewModel.patchItem(item.id!!, updatedItem)
                                delay(300) // opsional agar smooth
                            }

                            val printer = BluetoothPrinter()

                            printer.printBarcodeReceipt(
                                item = selectedItems,
                                payment = it.label,
                                orderID = orderID,
                                device = device
                            )

                            mainViewModel.clearSelectedItems()
                            showSheet = false
                        }
                    }
                    else {
                        Toast.makeText(context, "Belum ada printer yang terhubung", Toast.LENGTH_SHORT).show()
                        bluetoothHelper.requestBluetooth {
                            mainViewModel.showBluetoothDevice(true)
                        }
                    }
                }
            )
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