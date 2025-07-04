package com.polytron.auctionapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.polytron.auctionapp.view.screens.ScreenAuction
import com.polytron.auctionapp.view.screens.ScreenHome
import com.polytron.auctionapp.view.screens.ScreenItemList
import com.polytron.auctionapp.view.screens.ScreenItemListSelectAuction
import com.polytron.auctionapp.view.screens.ScreenItemListSelectPayment
import com.polytron.auctionapp.view.screens.ScreenListPayment
import com.polytron.auctionapp.view.screens.ScreenPayment
import com.polytron.auctionapp.view.screens.ScreenScanBarcode

@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            ScreenHome(onNavigate = { navController.navigate(it) })
        }
        composable(Screen.Auction.route) {
            ScreenAuction(
                navScanBarcode = { navController.navigate(Screen.ScanBarcode.route) },
                navListItems = { navController.navigate(Screen.ScreenItemListSelectAuction.route) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.ScanBarcode.route) {
            ScreenScanBarcode()
        }
        composable(Screen.ListItems.route) {
            ScreenItemList(
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.ScreenItemListSelectAuction.route) {
            ScreenItemListSelectAuction(
                navBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Payment.route) {
            ScreenPayment(
                navScanBarcode = { navController.navigate(Screen.ScanBarcode.route) },
                navListItems = { navController.navigate(Screen.ScreenItemListSelectPayment.route) },
                navListPayment = { navController.navigate(Screen.ScreenListPayment.route) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.ScreenItemListSelectPayment.route) {
            ScreenItemListSelectPayment(
                navBack = { navController.popBackStack() }
            )
        }
        composable(Screen.ScreenListPayment.route) {
            ScreenListPayment(
                navBack = { navController.popBackStack() }
            )
        }
    }
}