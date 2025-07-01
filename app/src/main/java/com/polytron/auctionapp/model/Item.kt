package com.polytron.auctionapp.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Item(
	@SerialName("admin") val admin: String? = null,
	@SerialName("_id") val id: String? = null,
	@SerialName("maxPrice") val maxPrice: String? = null,
	@SerialName("time") val time: String? = null,
	@SerialName("codeItem") val codeItem: String? = null,
	@SerialName("nameItem") val nameItem: String? = null,
	@SerialName("basePrice") val basePrice: String? = null,
	@SerialName("buyer") val buyer: String? = null,
	@SerialName("status") val status: Boolean? = null
)
