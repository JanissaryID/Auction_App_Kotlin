package com.polytron.auctionapp.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class Items(
    @SerialName("totalItems") val totalItems: Int? = null,
    @SerialName("perPage") val perPage: Int? = null,
    @SerialName("totalPages") val totalPages: Int? = null,
    @SerialName("page") val page: Int? = null,
//    @SerialName("items") val items: List<ItemResponse>? = null
): Parcelable