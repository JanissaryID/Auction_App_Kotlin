package com.polytron.auctionapp.view.screens

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.polytron.auctionapp.bluetooth.BluetoothHelper
import com.polytron.auctionapp.bluetooth.BluetoothPrinter
import com.polytron.auctionapp.data.remote.viewmodel.InventoryViewModel
import com.polytron.auctionapp.utils.formatCurrencyInput
import com.polytron.auctionapp.utils.formatRupiah
import com.polytron.auctionapp.view.components.EmptyItemState
import com.polytron.auctionapp.view.components.PrinterListDialog
import com.polytron.auctionapp.view.components.SelectedItemsBottomBar
import com.polytron.auctionapp.view.components.TopAppBarCustom
import com.polytron.auctionapp.view.components.fab.FabWithSubmenu
import com.polytron.auctionapp.view.components.itemcard.ItemCardAuction
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenAuction(
    inventoryViewModel: InventoryViewModel = koinInject(),
    bluetoothHelper: BluetoothHelper,
    navScanBarcode: () -> Unit,
    navListItems: () -> Unit,
    navBack: () -> Unit,
) {
    val context = LocalContext.current

    var rawAuctionPrice by remember { mutableStateOf("") }
    var auctionPrice by remember { mutableStateOf(formatCurrencyInput(rawAuctionPrice)) }
    var isSubmitting by remember { mutableStateOf(false) }
    var isFabExpanded by remember { mutableStateOf(false) }

    val selectedItems by inventoryViewModel.selectedItems.collectAsState()
    val editingBuyers by inventoryViewModel.editingBuyers.collectAsState()
    val printerDevice by inventoryViewModel.selectedPrinter.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val editingPrices by inventoryViewModel.editingPrices.collectAsState()
    val showBluetoothDevice by inventoryViewModel.showBluetoothDevice.collectAsState()

    val isAuctionPriceValid = rawAuctionPrice.isNotBlank()
    val isAllBuyerFilled = selectedItems.all {
        val edited = editingBuyers[it.id]
        !edited.isNullOrBlank()
    }
    val isSimpanEnabled = selectedItems.isNotEmpty() && isAuctionPriceValid && isAllBuyerFilled

    BackHandler {
        navBack()
        inventoryViewModel.clearSelectedItems()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBarCustom(
                title = "Lelang",
                onBack = {
                    navBack()
                    inventoryViewModel.clearSelectedItems()
                }
            )
        },
        bottomBar = {
            SelectedItemsBottomBar(
                selectedCount = selectedItems.size,
                buttonText = "Simpan",
                isSubmitting = isSubmitting,
                enabled = isSimpanEnabled,
                onClick = {
                    isSubmitting = true
                    coroutineScope.launch {
                        val device = printerDevice

                        if (device != null) {
                            val fallbackPrice = rawAuctionPrice.toIntOrNull()?.toString()

                            selectedItems.forEach { item ->
                                val finalBuyer = editingBuyers[item.id].orEmpty().ifBlank { item.buyer.orEmpty() }
                                val finalPrice = editingPrices[item.id].orEmpty().ifBlank { fallbackPrice.orEmpty() }

                                val updatedItem = item.copy(
                                    buyer = finalBuyer,
                                    price = finalPrice,
                                    status = 1
                                )

                                val printer = BluetoothPrinter()

                                inventoryViewModel.patchItem(item.id!!, updatedItem)

                                printer.printBarcodeAuction(
                                    device = device,
                                    price = formatRupiah(finalPrice),
                                    name = finalBuyer,
                                    itemName = item.nameItem!!,
                                    code = item.codeItem!!,
                                )

                                delay(300)

                                printer.printBarcodeAuctionItems(
                                    device = device,
                                    price = formatRupiah(finalPrice),
                                    name = finalBuyer,
                                    itemName = item.nameItem,
                                )

                                delay(300)
                            }
                        } else {
                            Toast.makeText(context, "Belum ada printer yang terhubung", Toast.LENGTH_SHORT).show()
                            bluetoothHelper.requestBluetooth(
                                onReady = {
                                    // Bluetooth aktif → buka dialog pilih printer
                                    inventoryViewModel.showBluetoothDevice(true)
                                },
                                onFailure = { reason ->
                                    // User nolak permission / nolak enable BT / device nggak support
                                    Toast.makeText(context, "Bluetooth gagal: $reason", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }


                        isSubmitting = false
                        inventoryViewModel.clearSelectedItems()
                        rawAuctionPrice = ""
                        auctionPrice = ""
                    }
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
                                inventoryViewModel.updateEditingBuyer(item.id!!, newName)
                            },
                            onPriceChanged = { newPrice ->
                                inventoryViewModel.updateEditingPrice(item.id!!, newPrice)
                            },
                            onCancelPriceInput = {
                                inventoryViewModel.clearEditingForItem(item.id!!)
                            },
                            onClickDelete = {
                                inventoryViewModel.removeSelectedItem(item)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showBluetoothDevice) {
        PrinterListDialog(
            bluetoothHelper = bluetoothHelper,
            onPrinterSelected = { device ->
                inventoryViewModel.setSelectedPrinter(device)
                inventoryViewModel.showBluetoothDevice(false)
            },
            onDismiss = {
                inventoryViewModel.showBluetoothDevice(false)
            }
        )
    }
}
