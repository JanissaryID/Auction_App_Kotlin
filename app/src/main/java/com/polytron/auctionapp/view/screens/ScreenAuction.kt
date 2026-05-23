package com.polytron.auctionapp.view.screens

import android.annotation.SuppressLint
import android.util.Log
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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.lifecycle.ViewModelStoreOwner
import com.polytron.auctionapp.bluetooth.BluetoothHelper
import com.polytron.auctionapp.bluetooth.BluetoothPrinter
import com.polytron.auctionapp.printing.ThermalPrintFormatter
import com.polytron.auctionapp.ui.viewmodel.AuctionViewModel
import com.polytron.auctionapp.ui.viewmodel.ItemsViewModel
import com.polytron.auctionapp.ui.viewmodel.PrinterViewModel
import com.polytron.auctionapp.utils.formatCurrencyInput
import com.polytron.auctionapp.view.components.EmptyItemState
import com.polytron.auctionapp.view.components.SelectedItemsBottomBar
import com.polytron.auctionapp.view.components.TopAppBarCustom
import com.polytron.auctionapp.view.components.dialog.NoPrinterDialog
import com.polytron.auctionapp.view.components.dialog.PrinterListDialog
import com.polytron.auctionapp.view.components.fab.FabWithSubmenu
import com.polytron.auctionapp.view.components.itemcard.ItemCardAuction
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.compose.viewmodel.koinViewModel

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenAuction(
    itemsViewModel: ItemsViewModel = koinViewModel(
        viewModelStoreOwner = LocalContext.current as ViewModelStoreOwner
    ),
    auctionViewModel: AuctionViewModel = koinViewModel(
        viewModelStoreOwner = LocalContext.current as ViewModelStoreOwner
    ),
    printerViewModel: PrinterViewModel = koinViewModel(
        viewModelStoreOwner = LocalContext.current as ViewModelStoreOwner
    ),
    bluetoothHelper: BluetoothHelper,
    navScanBarcode: () -> Unit,
    navListItems: () -> Unit,
    navBack: () -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val selectedItems by auctionViewModel.selectedItems.collectAsState()
    val editingBuyers by auctionViewModel.editingBuyers.collectAsState()
    val editingPrices by auctionViewModel.editingPrices.collectAsState()
    val printerDevice by printerViewModel.selectedPrinter.collectAsState()
    val showBluetoothDevice by printerViewModel.showBluetoothDevice.collectAsState()
    val auctionUsers by auctionViewModel.auctionUsers.collectAsState()

    var rawAuctionPrice by remember { mutableStateOf("") }
    var auctionPrice by remember { mutableStateOf(formatCurrencyInput(rawAuctionPrice)) }
    var isSubmitting by remember { mutableStateOf(false) }
    var submittingText by remember { mutableStateOf("Memproses...") }
    var isFabExpanded by remember { mutableStateOf(false) }
    var showNoPrinterDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        auctionViewModel.fetchAuctionUsers()
    }

    val isPriceFilledProperly = selectedItems.isNotEmpty() && selectedItems.all { item ->
        val hasManualPrice = !editingPrices[item.id].isNullOrBlank()
        val hasGlobalPrice = rawAuctionPrice.isNotBlank()
        hasManualPrice || hasGlobalPrice
    }
    val isAllBuyerFilled = selectedItems.isNotEmpty() && selectedItems.all { item ->
        val editedName = editingBuyers[item.id]
        !editedName.isNullOrBlank() || !item.buyer.isNullOrBlank()
    }
    val isSimpanEnabled = isPriceFilledProperly && isAllBuyerFilled && !isSubmitting

    val onSaveData = { shouldPrint: Boolean ->
        submittingText = if (shouldPrint) "Menyimpan & mencetak..." else "Menyimpan..."
        isSubmitting = true
        showNoPrinterDialog = false
        coroutineScope.launch {
            try {
                val itemsToSave = selectedItems.toList()
                val receipts = itemsToSave.mapNotNull { item ->
                    val itemId = item.id ?: return@mapNotNull null
                    val finalPrice = editingPrices[itemId].orEmpty().ifBlank { rawAuctionPrice }
                    val finalBuyer = editingBuyers[itemId].orEmpty().ifBlank { item.buyer.orEmpty() }

                    ThermalPrintFormatter.AuctionReceiptItem(
                        buyerName = finalBuyer,
                        basePrice = item.basePrice,
                        auctionPrice = finalPrice,
                        itemName = item.nameItem.orEmpty(),
                        itemCode = item.codeItem.orEmpty()
                    )
                }

                itemsToSave.forEach { item ->
                    val itemId = item.id ?: return@forEach
                    val finalPrice = editingPrices[itemId].orEmpty().ifBlank { rawAuctionPrice }
                    val finalBuyer = editingBuyers[itemId].orEmpty().ifBlank { item.buyer.orEmpty() }
                    val updatedItem = item.copy(buyer = finalBuyer, price = finalPrice, status = 1)

                    itemsViewModel.patchItem(itemId, updatedItem)
                }

                val printSuccess = if (shouldPrint) {
                    val device = printerDevice
                    if (device != null && receipts.isNotEmpty()) {
                        withContext(Dispatchers.IO) {
                            BluetoothPrinter().printAuctionReceipts(device = device, receipts = receipts)
                        }
                    } else {
                        false
                    }
                } else {
                    true
                }

                // Simpan nama pembeli baru ke database saran
                val newBuyers = itemsToSave.map { item ->
                    item.id?.let { itemId ->
                        editingBuyers[itemId].orEmpty().ifBlank { item.buyer.orEmpty() }
                    }.orEmpty()
                }.filter { it.isNotBlank() }
                auctionViewModel.saveNewAuctionUsers(newBuyers, "")

                auctionViewModel.clearSelectedItems()
                rawAuctionPrice = ""
                auctionPrice = ""
                snackbarHostState.showSnackbar(
                    when {
                        shouldPrint && printSuccess -> "Berhasil disimpan dan dicetak"
                        shouldPrint -> "Berhasil disimpan, tapi nota gagal dicetak"
                        else -> "Berhasil disimpan tanpa cetak"
                    }
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("ScreenAuction", "save auction failed", e)
                Toast.makeText(context, "Lelang gagal disimpan. Coba lagi.", Toast.LENGTH_LONG).show()
            } finally {
                isSubmitting = false
            }
        }
    }

    BackHandler { navBack() }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { TopAppBarCustom(title = "Lelang", onBack = { navBack() }) },
        bottomBar = {
            SelectedItemsBottomBar(
                selectedCount = selectedItems.size,
                buttonText = "Simpan & Cetak",
                isSubmitting = isSubmitting,
                submittingText = submittingText,
                enabled = isSimpanEnabled,
                onClick = {
                    if (printerDevice == null) showNoPrinterDialog = true else onSaveData(true)
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
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp)) {
            OutlinedTextField(
                value = auctionPrice,
                onValueChange = {
                    val digits = it.filter { c -> c.isDigit() }
                    rawAuctionPrice = digits
                    auctionPrice = formatCurrencyInput(digits)
                },
                label = { Text("Harga Lelang Global (Opsional)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                prefix = { Text("Rp ") },
                enabled = !isSubmitting
            )

            if (selectedItems.isEmpty()) {
                EmptyItemState()
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 100.dp, top = 8.dp),
                    modifier = Modifier.fillMaxWidth().weight(1f)
                ) {
                    items(selectedItems, key = { it.id ?: "" }) { item ->
                        ItemCardAuction(
                            item = item,
                            currentBuyer = editingBuyers[item.id].orEmpty(),
                            currentPrice = editingPrices[item.id].orEmpty(),
                            auctionUsers = auctionUsers,
                            onNameChanged = { name -> auctionViewModel.updateEditingBuyer(item.id!!, name) },
                            onPriceChanged = { price -> auctionViewModel.updateEditingPrice(item.id!!, price) },
                            onCancelPriceInput = { auctionViewModel.clearEditingForItem(item.id!!) },
                            onClickDelete = { auctionViewModel.removeSelectedItem(item) }
                        )
                    }
                }
            }
        }
    }

    if (showNoPrinterDialog) {
        NoPrinterDialog(
            onDismiss = { showNoPrinterDialog = false },
            onSaveOnly = { onSaveData(false) },
            onConnectPrinter = {
                showNoPrinterDialog = false
                bluetoothHelper.requestBluetooth(
                    onReady = { printerViewModel.showBluetoothDevice(true) },
                    onFailure = { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
                )
            }
        )
    }

    if (showBluetoothDevice) {
        PrinterListDialog(
            bluetoothHelper = bluetoothHelper,
            onPrinterSelected = { printerViewModel.setSelectedPrinter(it); printerViewModel.showBluetoothDevice(false) },
            onDismiss = { printerViewModel.showBluetoothDevice(false) }
        )
    }
}
