package com.polytron.auctionapp.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object ScanBarcode : Screen("scan_barcode/{typeScreen}") {
        fun createRoute(typeScreen: String) = "scan_barcode/$typeScreen"
    }
    object ListItems : Screen("list_items")
    object Auction : Screen("auction")
    object Payment : Screen("payment")
    object ItemListSelectAuction : Screen("screen_item_list_select_auction")
    object ItemListSelectPayment : Screen("screen_item_list_select_payment")
    object ListPayment : Screen("screen_list_payment")
    object TakeItems : Screen("take_items")
    object Transactions : Screen("transactions")
}