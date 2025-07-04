package com.polytron.auctionapp.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object ScanBarcode : Screen("scan_barcode")
    object ListItems : Screen("list_items")
    object Auction : Screen("auction")
    object Payment : Screen("payment")
    object ScreenItemListSelectAuction : Screen("screen_item_list_select_auction")
    object ScreenItemListSelectPayment : Screen("screen_item_list_select_payment")
    object ScreenListPayment : Screen("screen_list_payment")
//    object Detail : Screen("detail/{itemId}") {
//        fun createRoute(itemId: String) = "detail/$itemId"
//    }
}