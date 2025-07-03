package com.polytron.auctionapp.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class Item(
	@SerialName("admin") val admin: String? = null,
	@SerialName("_id") val id: String? = null,
	@SerialName("maxPrice") val maxPrice: String? = null,
	@SerialName("orderID") val orderID: String? = null,
	@SerialName("codeItem") val codeItem: String? = null,
	@SerialName("nameItem") val nameItem: String? = null,
	@SerialName("basePrice") val basePrice: String? = null,
	@SerialName("price") val price: String? = null,
	@SerialName("buyer") val buyer: String? = null,
	@SerialName("status") val status: Int? = null
): Parcelable
