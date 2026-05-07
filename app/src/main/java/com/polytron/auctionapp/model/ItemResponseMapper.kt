package com.polytron.auctionapp.model

import com.polytron.auctionapp.shared.model.SharedItem

fun ItemResponse.toSharedItem(): SharedItem {
    return SharedItem(
        id = id ?: codeItem ?: nameItem ?: orderID ?: "unknown-id",
        nameItem = nameItem,
        codeItem = codeItem,
        buyer = buyer,
        price = price,
        orderId = orderID,
        status = status
    )
}

fun SharedItem.toItemResponse(): ItemResponse {
    return ItemResponse(
        orderID = orderId,
        nameItem = nameItem,
        buyer = buyer,
        price = price,
        codeItem = codeItem,
        status = status
    )
}
