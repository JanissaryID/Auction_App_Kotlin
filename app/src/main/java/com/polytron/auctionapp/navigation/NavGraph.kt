package com.polytron.auctionapp.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.polytron.auctionapp.bluetooth.BluetoothHelper
import com.polytron.auctionapp.domain.model.TypeScreenBarcode
import com.polytron.auctionapp.ui.viewmodel.AuthViewModel
import com.polytron.auctionapp.view.components.dialog.LoginDialog
import com.polytron.auctionapp.view.screens.ScreenAuction
import com.polytron.auctionapp.view.screens.ScreenHome
import com.polytron.auctionapp.view.screens.ScreenItemList
import com.polytron.auctionapp.view.screens.ScreenItemListSelectAuction
import com.polytron.auctionapp.view.screens.ScreenItemListSelectPayment
import com.polytron.auctionapp.view.screens.ScreenListPayment
import com.polytron.auctionapp.view.screens.ScreenPayment
import com.polytron.auctionapp.view.screens.ScreenScanBarcode
import com.polytron.auctionapp.view.screens.ScreenTakeItems
import com.polytron.auctionapp.view.screens.ScreenTransactions
import org.koin.compose.viewmodel.koinViewModel

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    bluetoothHelper: BluetoothHelper,
    authViewModel: AuthViewModel = koinViewModel(
        viewModelStoreOwner = LocalContext.current as ViewModelStoreOwner
    )
) {
    var showLoginDialog by remember { mutableStateOf(false) }

    LaunchedEffect(authViewModel) {
        authViewModel.loginRequiredEvent.collect {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Home.route)
                launchSingleTop = true
            }
            showLoginDialog = true
        }
    }

    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            ScreenHome(
                onNavigate = { navController.navigate(it) },
                bluetoothHelper = bluetoothHelper
            )
        }
        composable(Screen.Auction.route) {
            ScreenAuction(
                navScanBarcode = { navController.navigate(
                    Screen.ScanBarcode.createRoute(typeScreen = TypeScreenBarcode.Auction.name)
                ) },
                navListItems = { navController.navigate(Screen.ItemListSelectAuction.route) },
                navBack = { navController.popBackStack() },
                bluetoothHelper = bluetoothHelper
            )
        }
        composable(
            Screen.ScanBarcode.route,
            arguments = listOf(navArgument("typeScreen") { type = NavType.StringType })
        ) { entry ->
            val typeScreen = entry.arguments?.getString("typeScreen")
            ScreenScanBarcode(
                navBack = { navController.popBackStack() },
                typeScreen = typeScreen
            )
        }
        composable(Screen.ListItems.route) {
            ScreenItemList(
                navBack = { navController.popBackStack() },
                bluetoothHelper = bluetoothHelper
            )
        }
        composable(Screen.ItemListSelectAuction.route,) {
            ScreenItemListSelectAuction(
                navBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Payment.route) {
            ScreenPayment(
                navScanBarcode = { navController.navigate(
                    Screen.ScanBarcode.createRoute(typeScreen = TypeScreenBarcode.Payment.name)
                ) },
                navListItems = { navController.navigate(Screen.ItemListSelectPayment.route) },
                navListPayment = { navController.navigate(Screen.ListPayment.route) },
                navBack = { navController.popBackStack() },
                bluetoothHelper = bluetoothHelper
            )
        }
        composable(Screen.ItemListSelectPayment.route) {
            ScreenItemListSelectPayment(
                navBack = { navController.popBackStack() }
            )
        }
        composable(Screen.ListPayment.route) {
            ScreenListPayment(
                navBack = { navController.popBackStack() },
                bluetoothHelper = bluetoothHelper
            )
        }
        composable(Screen.TakeItems.route) {
            ScreenTakeItems(
                navBack = { navController.popBackStack() },
            )
        }
        composable(Screen.Transactions.route) {
            ScreenTransactions(
                navBack = { navController.popBackStack() }
            )
        }
    }

    if (showLoginDialog) {
        LoginDialog(onDismiss = { showLoginDialog = false })
    }
}
