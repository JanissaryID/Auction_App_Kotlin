package com.polytron.auctionapp.desktop.app

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.polytron.auctionapp.presentation.auction.AuctionViewModel
import com.polytron.auctionapp.presentation.auth.AuthViewModel
import com.polytron.auctionapp.presentation.items.ItemsViewModel
import com.polytron.auctionapp.presentation.payment.PaymentViewModel
import com.polytron.auctionapp.presentation.pickup.PickupViewModel
import com.polytron.auctionapp.utils.generateRandomAlphanumeric
import kotlinx.coroutines.launch
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
        val items by itemsViewModel.items.collectAsState()
        val isLoadingItems by itemsViewModel.isLoading.collectAsState()
        val selectedAuctionItems by auctionViewModel.selectedItems.collectAsState()
        val editingBuyers by auctionViewModel.editingBuyers.collectAsState()
        val editingPrices by auctionViewModel.editingPrices.collectAsState()
        val selectedPaymentItems by paymentViewModel.selectedItems.collectAsState()
        val selectedPickupItems by pickupViewModel.selectedItems.collectAsState()

        LaunchedEffect(authViewModel) {
            authViewModel.toastEvent.collect { message ->
                snackbarHostState.showSnackbar(message)
            }
        }

        DesktopShell(
            currentDestination = navigator.destination,
            isLoggedIn = isLoggedIn,
            userName = userName,
            snackbarHostState = snackbarHostState,
            onDestinationSelected = navigator::navigate,
            onLoginClick = { navigator.showDialog(DesktopDialog.Login) },
            onProfileClick = { navigator.showDialog(DesktopDialog.Profile) },
            onRefreshClick = itemsViewModel::fetchItems
        ) {
            when (navigator.destination) {
                DesktopDestination.Dashboard -> DashboardScreen(
                    items = items,
                    isLoggedIn = isLoggedIn,
                    userName = userName
                )

                DesktopDestination.Items -> ItemsScreen(
                    items = items,
                    isLoading = isLoadingItems,
                    onRefresh = itemsViewModel::fetchItems,
                    onAddItem = { navigator.showDialog(DesktopDialog.AddItem) },
                    onEditItem = { id -> navigator.showDialog(DesktopDialog.EditItem(id)) },
                    onDeleteItem = { id -> navigator.showDialog(DesktopDialog.ConfirmDelete(listOf(id))) },
                    onItemDetail = { id -> navigator.showDialog(DesktopDialog.ItemDetail(id)) },
                    onBulkDelete = { ids -> navigator.showDialog(DesktopDialog.ConfirmDelete(ids)) }
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
            onDismiss = navigator::closeDialog
        )
    }
}
