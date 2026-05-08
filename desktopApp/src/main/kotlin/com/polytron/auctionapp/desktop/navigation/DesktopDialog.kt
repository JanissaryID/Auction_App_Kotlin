package com.polytron.auctionapp.desktop.navigation

sealed interface DesktopDialog {
    data object Login : DesktopDialog
    data object Profile : DesktopDialog
    data object AddItem : DesktopDialog
    data class EditItem(val itemId: String) : DesktopDialog
    data class ItemDetail(val itemId: String) : DesktopDialog
    data object SelectAuctionItems : DesktopDialog
    data object SelectPaymentItems : DesktopDialog
    data object SelectPickupItems : DesktopDialog
    data object BarcodeEntry : DesktopDialog
    data object PaymentMethod : DesktopDialog
    data class TransactionDetail(val orderId: String) : DesktopDialog
    data class ConfirmDelete(val itemIds: List<String>) : DesktopDialog
    data class ConfirmTakeItems(val orderId: String) : DesktopDialog
}
