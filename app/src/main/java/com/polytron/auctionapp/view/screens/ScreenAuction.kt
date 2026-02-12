package com.polytron.auctionapp.view.screens

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelStoreOwner
import com.polytron.auctionapp.bluetooth.BluetoothHelper
import com.polytron.auctionapp.bluetooth.BluetoothPrinter
import com.polytron.auctionapp.data.remote.viewmodel.InventoryViewModel
import com.polytron.auctionapp.utils.formatCurrencyInput
import com.polytron.auctionapp.utils.formatRupiah
import com.polytron.auctionapp.view.components.EmptyItemState
import com.polytron.auctionapp.view.components.NoPrinterDialog
import com.polytron.auctionapp.view.components.PrinterListDialog
import com.polytron.auctionapp.view.components.SelectedItemsBottomBar
import com.polytron.auctionapp.view.components.TopAppBarCustom
import com.polytron.auctionapp.view.components.fab.FabWithSubmenu
import com.polytron.auctionapp.view.components.itemcard.ItemCardAuction
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenAuction(
    inventoryViewModel: InventoryViewModel = koinViewModel(
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

    // State dari ViewModel
    val selectedItems by inventoryViewModel.selectedItems.collectAsState()
    val editingBuyers by inventoryViewModel.editingBuyers.collectAsState()
    val editingPrices by inventoryViewModel.editingPrices.collectAsState()
    val printerDevice by inventoryViewModel.selectedPrinter.collectAsState()
    val showBluetoothDevice by inventoryViewModel.showBluetoothDevice.collectAsState()

    // State UI Lokal
    var rawAuctionPrice by remember { mutableStateOf("") }
    var auctionPrice by remember { mutableStateOf(formatCurrencyInput(rawAuctionPrice)) }
    var isSubmitting by remember { mutableStateOf(false) }
    var isFabExpanded by remember { mutableStateOf(false) }

    // State untuk Dialog Printer Belum Terhubung
    var showNoPrinterDialog by remember { mutableStateOf(false) }

    // Logic Validasi
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

    // Fungsi Internal untuk Simpan Data (bisa dipanggil dengan atau tanpa print)
    val onSaveData = { shouldPrint: Boolean ->
        isSubmitting = true
        showNoPrinterDialog = false
        coroutineScope.launch {
            try {
                val printer = BluetoothPrinter()
                selectedItems.forEach { item ->
                    val finalPrice = editingPrices[item.id].orEmpty().ifBlank { rawAuctionPrice }
                    val finalBuyer = editingBuyers[item.id].orEmpty().ifBlank { item.buyer.orEmpty() }

                    val updatedItem = item.copy(
                        buyer = finalBuyer,
                        price = finalPrice,
                        status = 1
                    )

                    // 1. Update ke Server
                    inventoryViewModel.patchItem(item.id!!, updatedItem)

                    // 2. Cetak jika diperintahkan dan printer tersedia
                    if (shouldPrint && printerDevice != null) {
                        printer.printBarcodeAuction(
                            device = printerDevice!!,
                            price = formatRupiah(finalPrice),
                            name = finalBuyer,
                            itemName = item.nameItem ?: "",
                            code = item.codeItem ?: "",
                        )
                        delay(400)
                        printer.printBarcodeAuctionItems(
                            device = printerDevice!!,
                            price = formatRupiah(finalPrice),
                            name = finalBuyer,
                            itemName = item.nameItem ?: "",
                        )
                        delay(400)
                    }
                }

                inventoryViewModel.clearSelectedItems()
                rawAuctionPrice = ""
                auctionPrice = ""
                snackbarHostState.showSnackbar(if (shouldPrint) "Berhasil disimpan dan dicetak" else "Berhasil disimpan tanpa cetak")
            } catch (e: Exception) {
                Log.e("Auction Err", "ScreenAuction: $e", )
                Toast.makeText(context, "Gagal: ${e.message}", Toast.LENGTH_LONG).show()
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
                enabled = isSimpanEnabled,
                onClick = {
                    if (printerDevice == null) {
                        // Jika printer belum ada, tampilkan dialog pilihan
                        showNoPrinterDialog = true
                    } else {
                        // Jika printer sudah ada, langsung jalankan simpan & cetak
                        onSaveData(true)
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
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 16.dp)) {
            OutlinedTextField(
                value = auctionPrice,
                onValueChange = {
                    val digits = it.filter { c -> c.isDigit() }
                    rawAuctionPrice = digits
                    auctionPrice = formatCurrencyInput(digits)
                },
                label = { Text("Harga Lelang Global (Opsional)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                prefix = { Text("Rp ") },
                enabled = !isSubmitting
            )

            if (selectedItems.isEmpty()) {
                EmptyItemState()
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 100.dp, top = 8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(selectedItems, key = { it.id ?: "" }) { item ->
                        ItemCardAuction(
                            item = item,
                            currentBuyer = editingBuyers[item.id].orEmpty(),
                            currentPrice = editingPrices[item.id].orEmpty(),
                            onNameChanged = { name -> inventoryViewModel.updateEditingBuyer(item.id!!, name) },
                            onPriceChanged = { price -> inventoryViewModel.updateEditingPrice(item.id!!, price) },
                            onCancelPriceInput = { inventoryViewModel.clearEditingForItem(item.id!!) },
                            onClickDelete = { inventoryViewModel.removeSelectedItem(item) }
                        )
                    }
                }
            }
        }
    }

    // --- DIALOGS ---

    // 1. Dialog Pilihan Jika Printer Belum Terhubung
    if (showNoPrinterDialog) {
        NoPrinterDialog(
            onDismiss = { showNoPrinterDialog = false },
            onSaveOnly = { onSaveData(false) },
            onConnectPrinter = {
                showNoPrinterDialog = false
                bluetoothHelper.requestBluetooth(
                    onReady = { inventoryViewModel.showBluetoothDevice(true) },
                    onFailure = { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
                )
            }
        )
    }

    // 2. Dialog List Printer (Bluetooth)
    if (showBluetoothDevice) {
        PrinterListDialog(
            bluetoothHelper = bluetoothHelper,
            onPrinterSelected = {
                inventoryViewModel.setSelectedPrinter(it)
                inventoryViewModel.showBluetoothDevice(false)
            },
            onDismiss = { inventoryViewModel.showBluetoothDevice(false) }
        )
    }
}