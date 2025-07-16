package com.polytron.auctionapp.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class RealtimeEvent(
    @SerialName("action") val action: String? = null,
    @SerialName("record") val record: JsonObject? = null,
)