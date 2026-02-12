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
import androidx.lifecycle.ViewModelStoreOwner
import com.polytron.auctionapp.bluetooth.BluetoothHelper
import com.polytron.auctionapp.bluetooth.BluetoothPrinter
import com.polytron.auctionapp.data.remote.viewmodel.InventoryViewModel
import com.polytron.auctionapp.utils.formatRupiah
import com.polytron.auctionapp.utils.generateRandomAlphanumeric
import com.polytron.auctionapp.view.components.EmptyItemState
import com.polytron.auctionapp.view.components.ReceiptCard
import com.polytron.auctionapp.view.components.SelectedItemsBottomBar
import com.polytron.auctionapp.view.components.TopAppBarCustom
import com.polytron.auctionapp.view.components.bottomsheet.PaymentBottomSheet
import com.polytron.auctionapp.view.components.dialog.NoPrinterDialog
import com.polytron.auctionapp.view.components.dialog.PrinterListDialog
import com.polytron.auctionapp.view.components.fab.FabWithSubmenu
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenPayment(
    inventoryViewModel: InventoryViewModel = koinViewModel(
        viewModelStoreOwner = LocalContext.current as ViewModelStoreOwner
    ),
    bluetoothHelper: BluetoothHelper,
    navScanBarcode: () -> Unit,
    navListItems: () -> Unit,
    navListPayment: () -> Unit,
    navBack: () -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // --- State UI & ViewModel ---
    val selectedItems by inventoryViewModel.selectedItems.collectAsState()
    val printerDevice by inventoryViewModel.selectedPrinter.collectAsState()
    val showBluetoothDevice by inventoryViewModel.showBluetoothDevice.collectAsState()

    var isFabExpanded by remember { mutableStateOf(false) }
    var showSheet by remember { mutableStateOf(false) }
    var showNoPrinterDialog by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }

    // Menyimpan pilihan pembayaran sementara saat printer belum terhubung
    var pendingPaymentLabel by remember { mutableStateOf("") }

    // --- Fungsi Inti: Proses Pembayaran ---
    val onProcessPayment = { paymentLabel: String, shouldPrint: Boolean ->
        isSubmitting = true
        showSheet = false
        showNoPrinterDialog = false

        val orderID = "Order-${generateRandomAlphanumeric()}"

        coroutineScope.launch {
            try {
                // 1. Update status ke database server (Status 2 = Paid)
                selectedItems.forEach { item ->
                    val updatedItem = item.copy(
                        status = 2,
                        orderID = orderID,
                        typePayment = paymentLabel
                    )
                    inventoryViewModel.patchItem(item.id!!, updatedItem)
                    delay(300) // Jeda tipis untuk stabilitas request
                }

                // 2. Eksekusi Print jika printer siap dan user memilih "Cetak"
                if (shouldPrint && printerDevice != null) {
                    val printer = BluetoothPrinter()
                    printer.printBarcodeReceipt(
                        items = selectedItems,
                        payment = paymentLabel,
                        orderID = orderID,
                        device = printerDevice!!
                    )
                }

                // 3. Cleanup
                inventoryViewModel.clearSelectedItems()
                snackbarHostState.showSnackbar(
                    if (shouldPrint) "Pembayaran Berhasil & Struk Dicetak"
                    else "Pembayaran Berhasil Disimpan"
                )
            } catch (e: Exception) {
                Toast.makeText(context, "Terjadi kesalahan: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                isSubmitting = false
            }
        }
    }

    // Handle back button hardware
    BackHandler {
        navBack()
        inventoryViewModel.clearSelectedItems()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBarCustom(
                title = "Pembayaran",
                onBack = {
                    navBack()
                    inventoryViewModel.clearSelectedItems()
                },
                additionalActions = {
                    IconButton(onClick = { navListPayment() }) {
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = "Riwayat Pembayaran"
                        )
                    }
                }
            )
        },
        bottomBar = {
            SelectedItemsBottomBar(
                selectedCount = selectedItems.size,
                buttonText = "Proses Pembayaran",
                enabled = selectedItems.isNotEmpty() && !isSubmitting,
                onClick = { showSheet = true }
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
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            if (selectedItems.isEmpty()) {
                EmptyItemState()
            } else {
                // Komponen kartu yang berisi daftar item yang akan dibayar
                ReceiptCard(selectedItems = selectedItems) { item ->
                    inventoryViewModel.removeSelectedItem(item)
                }
            }
        }
    }

    // --- 1. Bottom Sheet: Pilih Metode Pembayaran ---
    if (showSheet) {
        val totalAmount = selectedItems.sumOf { item ->
            item.price?.replace(Regex("\\D"), "")?.toLongOrNull() ?: 0L
        }

        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            PaymentBottomSheet(
                total = formatRupiah(totalAmount.toString()),
                onDismiss = { showSheet = false },
                onPay = { paymentType ->
                    if (printerDevice == null) {
                        // Simpan pilihan user, tampilkan dialog printer
                        pendingPaymentLabel = paymentType.label
                        showNoPrinterDialog = true
                    } else {
                        // Printer ada, langsung proses cetak
                        onProcessPayment(paymentType.label, true)
                    }
                }
            )
        }
    }

    // --- 2. Custom No Printer Dialog (Reusable) ---
    if (showNoPrinterDialog) {
        NoPrinterDialog(
            onDismiss = { showNoPrinterDialog = false },
            onSaveOnly = {
                onProcessPayment(pendingPaymentLabel, false)
            },
            onConnectPrinter = {
                showNoPrinterDialog = false
                bluetoothHelper.requestBluetooth(
                    onReady = { inventoryViewModel.showBluetoothDevice(true) },
                    onFailure = { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
                )
            }
        )
    }

    // --- 3. Bluetooth Printer Selection Dialog ---
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