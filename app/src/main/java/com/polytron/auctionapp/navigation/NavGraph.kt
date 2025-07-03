package com.polytron.auctionapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.polytron.auctionapp.view.screens.ScreenAuction
import com.polytron.auctionapp.view.screens.ScreenItemList
import com.polytron.auctionapp.view.screens.ScreenItemListSelect
import com.polytron.auctionapp.view.screens.ScreenScanBarcode
import com.polytron.auctionapp.viewmodel.ItemsViewModel
import org.koin.compose.koinInject

@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Screen.Auction.route) {
        composable(Screen.Auction.route) {
            ScreenAuction(
                navScanBarcode = { navController.navigate(Screen.ScanBarcode.route) },
                navListItems = { navController.navigate(Screen.ScreenItemListSelect.route) }
            )
        }
        composable(Screen.ScanBarcode.route) {
            ScreenScanBarcode()
        }
        composable(Screen.ListItems.route) {
            ScreenItemList()
        }
        composable(Screen.ScreenItemListSelect.route) {
            ScreenItemListSelect(
                navBack = { navController.popBackStack() }
            )
        }
    }
}