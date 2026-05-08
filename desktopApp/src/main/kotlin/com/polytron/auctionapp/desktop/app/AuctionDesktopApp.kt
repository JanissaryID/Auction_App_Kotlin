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

        val navigator = rememberDesktopNavigator()
        val snackbarHostState = remember { SnackbarHostState() }

        val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
        val userName by authViewModel.userName.collectAsState()
        val items by itemsViewModel.items.collectAsState()
        val isLoadingItems by itemsViewModel.isLoading.collectAsState()
        val selectedAuctionItems by auctionViewModel.selectedItems.collectAsState()

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
                    onRefresh = itemsViewModel::fetchItems
                )

                DesktopDestination.Auction -> AuctionScreen(
                    items = items,
                    selectedItemCount = selectedAuctionItems.size
                )

                DesktopDestination.Payment -> PaymentScreen(items = items)
                DesktopDestination.Pickup -> PickupScreen(items = items)
                DesktopDestination.Transactions -> TransactionsScreen(items = items)
            }
        }

        DesktopDialogHost(
            dialog = navigator.dialog,
            authViewModel = authViewModel,
            onDismiss = navigator::closeDialog
        )
    }
}
