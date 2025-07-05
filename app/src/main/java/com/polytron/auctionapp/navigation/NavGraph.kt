package com.polytron.auctionapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.polytron.auctionapp.model.TypeScreenBarcode
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
import com.polytron.auctionapp.viewmodel.ItemsViewModel
import org.koin.compose.koinInject

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    itemsViewModel: ItemsViewModel = koinInject()
) {
    NavHost(navController = navController, startDestination = Screen.Home.route) {

        itemsViewModel.fetchItems()

        composable(Screen.Home.route) {
            ScreenHome(onNavigate = { navController.navigate(it) })
        }
        composable(Screen.Auction.route) {
            ScreenAuction(
                navScanBarcode = { navController.navigate(
                    Screen.ScanBarcode.createRoute(typeScreen = TypeScreenBarcode.Auction.name)
                ) },
                navListItems = { navController.navigate(Screen.ItemListSelectAuction.route) },
                navBack = { navController.popBackStack() }
            )
        }
        composable(
            Screen.ScanBarcode.route,
            arguments = listOf(navArgument("typeScreen") { type = NavType.StringType })
        ) { Entry ->
            val typeScreen = Entry.arguments?.getString("typeScreen")
            ScreenScanBarcode(
                navBack = { navController.popBackStack() },
                typeScreen = typeScreen
            )
        }
        composable(Screen.ListItems.route) {
            ScreenItemList(
                navBack = { navController.popBackStack() }
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
                navBack = { navController.popBackStack() }
            )
        }
        composable(Screen.ItemListSelectPayment.route) {
            ScreenItemListSelectPayment(
                navBack = { navController.popBackStack() }
            )
        }
        composable(Screen.ListPayment.route) {
            ScreenListPayment(
                navBack = { navController.popBackStack() }
            )
        }
        composable(Screen.TakeItems.route) {
            ScreenTakeItems(
                navBack = { navController.popBackStack() },
                navScanBarcode = { navController.navigate(Screen.ScanBarcode.route) }
            )
        }
        composable(Screen.Transactions.route) {
            ScreenTransactions(
                navBack = { navController.popBackStack() }
            )
        }
    }
}