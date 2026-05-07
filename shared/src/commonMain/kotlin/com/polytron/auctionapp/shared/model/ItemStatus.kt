package com.polytron.auctionapp.shared.model

/**
 * Status enum untuk item auction
 */
enum class ItemStatus(val value: Int, val displayName: String) {
    AVAILABLE(1, "Tersedia"),
    PAID(2, "Sudah Dibayar"),
    TAKEN(3, "Sudah Diambil");

    companion object {
        fun fromValue(value: Int?): ItemStatus? {
            return entries.find { it.value == value }
        }
    }
}

/**
 * Extension untuk convert Int ke ItemStatus
 */
fun Int?.toItemStatus(): ItemStatus? = ItemStatus.fromValue(this)

/**
 * Extension untuk SharedItem
 */
fun SharedItem.getStatus(): ItemStatus? = status.toItemStatus()

/**
 * Extension untuk check status
 */
fun SharedItem.isAvailable(): Boolean = status == ItemStatus.AVAILABLE.value
fun SharedItem.isPaid(): Boolean = status == ItemStatus.PAID.value
fun SharedItem.isTaken(): Boolean = status == ItemStatus.TAKEN.value
