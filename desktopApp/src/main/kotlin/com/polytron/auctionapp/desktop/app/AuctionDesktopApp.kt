package com.polytron.auctionapp.desktop.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.polytron.auctionapp.desktop.dialogs.DesktopDialogHost
import com.polytron.auctionapp.desktop.layout.DesktopShell
import com.polytron.auctionapp.desktop.navigation.DesktopDestination
import com.polytron.auctionapp.desktop.navigation.DesktopDialog
import com.polytron.auctionapp.desktop.navigation.rememberDesktopNavigator
import com.polytron.auctionapp.desktop.screens.AuctionScreen
import com.polytron.auctionapp.desktop.screens.DashboardScreen
import com.polytron.auctionapp.desktop.screens.ItemsScreen
import com.polytron.auctionapp.desktop.screens.PaymentScreen
import com.polytron.auctionapp.desktop.screens.PickupScreen
import com.polytron.auctionapp.desktop.screens.TransactionsScreen
import com.polytron.auctionapp.desktop.theme.DesktopTheme
import com.polytron.auctionapp.desktop.utils.DesktopThermalPrinter
import com.polytron.auctionapp.domain.model.ItemResponse
import com.polytron.auctionapp.presentation.auction.AuctionViewModel
import com.polytron.auctionapp.presentation.auth.AuthViewModel
import com.polytron.auctionapp.presentation.items.ItemsViewModel
import com.polytron.auctionapp.presentation.payment.PaymentViewModel
import com.polytron.auctionapp.presentation.pickup.PickupViewModel
import com.polytron.auctionapp.desktop.utils.exportItemsToExcelDesktop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.parameter.parametersOf
import org.koin.mp.KoinPlatform

@Composable
fun AuctionDesktopApp() {
    DesktopTheme {
        val appScope = rememberCoroutineScope()
        val koin = remember { KoinPlatform.getKoin() }
        val authViewModel = remember(appScope) {
            koin.get<AuthViewModel> { parametersOf(appScope) }
        }
        val itemsViewModel = remember(appScope) {
            koin.get<ItemsViewModel> { parametersOf(appScope) }
        }
        val auctionViewModel = remember {
            koin.get<AuctionViewModel>()
        }
        val paymentViewModel = remember {
            koin.get<PaymentViewModel>()
        }
        val pickupViewModel = remember {
            koin.get<PickupViewModel>()
        }

        val navigator = rememberDesktopNavigator()
        val snackbarHostState = remember { SnackbarHostState() }

        val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
        val userName by authViewModel.userName.collectAsState()
        val email by authViewModel.email.collectAsState()
        val idUser by authViewModel.idUser.collectAsState()
        val avatarFileName by authViewModel.avatarFileName.collectAsState()
        val items by itemsViewModel.items.collectAsState()
        val isLoadingItems by itemsViewModel.isLoading.collectAsState()
        val selectedAuctionItems by auctionViewModel.selectedItems.collectAsState()
        val editingBuyers by auctionViewModel.editingBuyers.collectAsState()
        val editingPrices by auctionViewModel.editingPrices.collectAsState()
        val selectedPaymentItems by paymentViewModel.selectedItems.collectAsState()
        val selectedPickupItems by pickupViewModel.selectedItems.collectAsState()
        val initialPrinterConfig = remember {
            val names = DesktopThermalPrinter.availablePrinterNames()
            val defaultName = DesktopThermalPrinter.defaultPrinterName()
                ?.takeIf { it in names }
                ?: names.firstOrNull()
            names to defaultName
        }
        var printerNames by remember { mutableStateOf(initialPrinterConfig.first) }
        var selectedPrinterName by remember { mutableStateOf(initialPrinterConfig.second) }

        fun refreshPrinters() {
            val names = DesktopThermalPrinter.availablePrinterNames()
            printerNames = names
            selectedPrinterName = selectedPrinterName
                ?.takeIf { it in names }
                ?: DesktopThermalPrinter.defaultPrinterName()?.takeIf { it in names }
                ?: names.firstOrNull()
        }

        fun printItemLabels(itemsToPrint: List<ItemResponse>) {
            val printerName = selectedPrinterName
            if (printerName == null) {
                appScope.launch { snackbarHostState.showSnackbar("Pilih printer thermal di Dashboard dulu.") }
                return
            }
            if (itemsToPrint.isEmpty()) {
                appScope.launch { snackbarHostState.showSnackbar("Tidak ada barang yang dipilih untuk dicetak.") }
                return
            }

            appScope.launch {
                val result = runCatching {
                    withContext(Dispatchers.IO) {
                        DesktopThermalPrinter.printItemLabels(printerName, itemsToPrint)
                    }
                }
                result.onSuccess {
                    snackbarHostState.showSnackbar("${itemsToPrint.size} label barang dikirim ke printer.")
                }.onFailure { error ->
                    snackbarHostState.showSnackbar("Gagal cetak: ${error.message ?: "printer tidak merespons"}")
                }
            }
        }

        suspend fun printPaymentReceipt(
            itemsToPrint: List<ItemResponse>,
            orderId: String,
            paymentMethod: String
        ): Result<Unit> {
            val printerName = selectedPrinterName
                ?: return Result.failure(IllegalStateException("Pilih printer thermal di Dashboard dulu."))

            return runCatching {
                withContext(Dispatchers.IO) {
                    DesktopThermalPrinter.printPaymentReceipt(
                        printerName = printerName,
                        items = itemsToPrint,
                        orderId = orderId,
                        paymentMethod = paymentMethod
                    )
                }
            }
        }

        fun printAuctionReceipts() {
            val printerName = selectedPrinterName
            if (printerName == null) {
                appScope.launch { snackbarHostState.showSnackbar("Pilih printer thermal di Dashboard dulu.") }
                return
            }

            val receipts = selectedAuctionItems.mapNotNull { item ->
                val itemId = item.id ?: return@mapNotNull null
                val buyer = editingBuyers[itemId].orEmpty().ifBlank { item.buyer.orEmpty() }
                val auctionPrice = editingPrices[itemId].orEmpty().ifBlank { item.price.orEmpty() }
                if (buyer.isBlank() || auctionPrice.isBlank()) return@mapNotNull null

                DesktopThermalPrinter.AuctionReceiptItem(
                    buyerName = buyer,
                    basePrice = item.basePrice,
                    auctionPrice = auctionPrice,
                    itemName = item.nameItem.orEmpty(),
                    itemCode = item.codeItem.orEmpty()
                )
            }

            if (receipts.isEmpty()) {
                appScope.launch { snackbarHostState.showSnackbar("Lengkapi nama pemenang dan harga lelang dulu.") }
                return
            }

            appScope.launch {
                val result = runCatching {
                    withContext(Dispatchers.IO) {
                        DesktopThermalPrinter.printAuctionReceipts(printerName, receipts)
                    }
                }
                result.onSuccess {
                    snackbarHostState.showSnackbar("Nota lelang Android-style dicetak 2 slip untuk ${receipts.size} barang.")
                }.onFailure { error ->
                    snackbarHostState.showSnackbar("Gagal cetak nota: ${error.message ?: "printer tidak merespons"}")
                }
            }
        }

        LaunchedEffect(authViewModel) {
            authViewModel.toastEvent.collect { message ->
                snackbarHostState.showSnackbar(message)
            }
        }

        Box(modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
            DesktopShell(
                currentDestination = navigator.destination,
                isLoggedIn = isLoggedIn,
                userName = userName,
                email = email,
                idUser = idUser,
                avatarFileName = avatarFileName,
                snackbarHostState = snackbarHostState,
                onDestinationSelected = navigator::navigate,
                onLoginClick = { navigator.showDialog(DesktopDialog.Login) },
                onProfileClick = { navigator.showDialog(DesktopDialog.Profile) }
            ) {
                when (navigator.destination) {
                    DesktopDestination.Dashboard -> DashboardScreen(
                        items = items,
                        isLoggedIn = isLoggedIn,
                        userName = userName,
                        printerNames = printerNames,
                        selectedPrinterName = selectedPrinterName,
                        onPrinterSelected = { selectedPrinterName = it },
                        onRefreshPrinters = {
                            refreshPrinters()
                            appScope.launch { snackbarHostState.showSnackbar("Daftar printer diperbarui.") }
                        },
                        onTestPrinter = {
                            val printerName = selectedPrinterName
                            if (printerName == null) {
                                appScope.launch { snackbarHostState.showSnackbar("Pilih printer thermal dulu.") }
                            } else {
                                appScope.launch {
                                    val result = runCatching {
                                        withContext(Dispatchers.IO) {
                                            DesktopThermalPrinter.printTest(printerName)
                                        }
                                    }
                                    result.onSuccess {
                                        snackbarHostState.showSnackbar("Tes cetak dikirim ke $printerName.")
                                    }.onFailure { error ->
                                        snackbarHostState.showSnackbar("Gagal tes cetak: ${error.message ?: "printer tidak merespons"}")
                                    }
                                }
                            }
                        }
                    )

                    DesktopDestination.Items -> ItemsScreen(
                        items = items,
                        isLoading = isLoadingItems,
                        onRefresh = itemsViewModel::fetchItems,
                        onAddItem = { navigator.showDialog(DesktopDialog.AddItem) },
                        onEditItem = { id -> navigator.showDialog(DesktopDialog.EditItem(id)) },
                        onDeleteItem = { id -> navigator.showDialog(DesktopDialog.ConfirmDelete(listOf(id))) },
                        onItemDetail = { id -> navigator.showDialog(DesktopDialog.ItemDetail(id)) },
                        onPrintItem = { item -> printItemLabels(listOf(item)) },
                        onBulkDelete = { ids -> navigator.showDialog(DesktopDialog.ConfirmDelete(ids)) },
                        onBulkPrint = ::printItemLabels
                    )

                    DesktopDestination.Auction -> AuctionScreen(
                        items = items,
                        selectedItems = selectedAuctionItems,
                        editingBuyers = editingBuyers,
                        editingPrices = editingPrices,
                        onAddItem = { navigator.showDialog(DesktopDialog.SelectAuctionItems) },
                        onBarcodeEntry = { navigator.showDialog(DesktopDialog.BarcodeEntry) },
                        onRemoveItem = auctionViewModel::removeSelectedItem,
                        onClearAll = auctionViewModel::clearSelectedItems,
                        onBuyerChange = auctionViewModel::updateEditingBuyer,
                        onPriceChange = auctionViewModel::updateEditingPrice,
                        onApplyPriceToAll = auctionViewModel::updateAllEditingPrices,
                        onPrintReceipts = ::printAuctionReceipts,
                        onSubmit = {
                            appScope.launch {
                                selectedAuctionItems.forEach { item ->
                                    val itemId = item.id ?: ""
                                    val buyer = editingBuyers[itemId] ?: ""
                                    val price = editingPrices[itemId] ?: ""
                                    itemsViewModel.patchItem(
                                        id = itemId,
                                        item = item.copy(
                                            status = 1,
                                            buyer = buyer,
                                            price = price
                                        )
                                    )
                                }
                                auctionViewModel.clearSelectedItems()
                                snackbarHostState.showSnackbar("Lelang berhasil disimpan")
                            }
                        }
                    )

                    DesktopDestination.Payment -> PaymentScreen(
                        items = items,
                        selectedItems = selectedPaymentItems,
                        onAddItem = { navigator.showDialog(DesktopDialog.SelectPaymentItems) },
                        onBarcodeEntry = { navigator.showDialog(DesktopDialog.BarcodeEntry) },
                        onRemoveItem = paymentViewModel::removeSelectedItem,
                        onClearAll = paymentViewModel::clearSelectedItems,
                        onPayClick = { navigator.showDialog(DesktopDialog.PaymentMethod) }
                    )
                    DesktopDestination.Pickup -> PickupScreen(
                        items = items,
                        selectedItems = selectedPickupItems,
                        onAddItem = { navigator.showDialog(DesktopDialog.SelectPickupItems) },
                        onBarcodeEntry = { navigator.showDialog(DesktopDialog.BarcodeEntry) },
                        onRemoveItem = pickupViewModel::removeSelectedItem,
                        onClearAll = pickupViewModel::clearSelectedItems,
                        onPickupClick = {
                            appScope.launch {
                                selectedPickupItems.forEach { item ->
                                    itemsViewModel.patchItem(
                                        id = item.id!!,
                                        item = item.copy(status = 3)
                                    )
                                }
                                pickupViewModel.clearSelectedItems()
                                snackbarHostState.showSnackbar("Pengambilan berhasil dikonfirmasi")
                            }
                        }
                    )
                    DesktopDestination.Transactions -> TransactionsScreen(
                        items = items,
                        isLoading = isLoadingItems,
                        onRefresh = itemsViewModel::fetchItems,
                        onExport = {
                            appScope.launch {
                                val transactionItems = items.filter { !it.orderID.isNullOrBlank() }
                                if (transactionItems.isEmpty()) {
                                    snackbarHostState.showSnackbar("Tidak ada data transaksi untuk di-export.")
                                    return@launch
                                }
                                val result = exportItemsToExcelDesktop(transactionItems)
                                result.onSuccess { message ->
                                    snackbarHostState.showSnackbar(message)
                                }.onFailure { error ->
                                    val msg = error.message ?: "Gagal export"
                                    if (msg != "Export dibatalkan") {
                                        snackbarHostState.showSnackbar("Gagal export: $msg")
                                    }
                                }
                            }
                        },
                        onTransactionDetail = { orderId -> navigator.showDialog(DesktopDialog.TransactionDetail(orderId)) }
                    )
                }
            }

            DesktopDialogHost(
                dialog = navigator.dialog,
                authViewModel = authViewModel,
                itemsViewModel = itemsViewModel,
                auctionViewModel = auctionViewModel,
                paymentViewModel = paymentViewModel,
                pickupViewModel = pickupViewModel,
                canPrintPaymentReceipt = selectedPrinterName != null,
                onPrintPaymentReceipt = ::printPaymentReceipt,
                onPaymentMessage = { message ->
                    appScope.launch { snackbarHostState.showSnackbar(message) }
                },
                onDismiss = navigator::closeDialog
            )
        }
    }
}
