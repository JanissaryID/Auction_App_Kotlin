package com.polytron.auctionapp.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object ScanBarcode : Screen("scan_barcode")
    object ListItems : Screen("list_items")
    object Auction : Screen("auction")
    object ScreenItemListSelect : Screen("screen_item_list_select")
//    object Detail : Screen("detail/{itemId}") {
//        fun createRoute(itemId: String) = "detail/$itemId"
//    }
}