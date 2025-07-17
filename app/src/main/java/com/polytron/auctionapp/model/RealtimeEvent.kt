package com.polytron.auctionapp.model

import android.os.Build
import androidx.annotation.RequiresApi
import io.github.agrevster.pocketbaseKotlin.models.Record
import kotlinx.serialization.Serializable

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Serializable
data class ItemRecord(
    val name: String,
    val quantity: Int,
) : Record()