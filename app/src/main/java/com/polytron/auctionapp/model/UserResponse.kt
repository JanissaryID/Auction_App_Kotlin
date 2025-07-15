package com.polytron.auctionapp.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName

@Parcelize
@kotlinx.serialization.Serializable
data class UserResponse(
	@SerialName("record") val record: Record? = null,
	@SerialName("token") val token: String? = null
) : Parcelable

@Parcelize
@kotlinx.serialization.Serializable
data class Record(
	@SerialName("created") val created: String? = null,
	@SerialName("verified") val verified: Boolean? = null,
	@SerialName("emailVisibility") val emailVisibility: Boolean? = null,
	@SerialName("id") val id: String? = null,
	@SerialName("collectionId") val collectionId: String? = null,
	@SerialName("updated") val updated: String? = null,
	@SerialName("email") val email: String? = null,
	@SerialName("collectionName") val collectionName: String? = null
) : Parcelable

@Parcelize
@kotlinx.serialization.Serializable
data class UserRequest(
	@SerialName("identity") val identity: String? = null,
	@SerialName("password") val password: String? = null
) : Parcelable

