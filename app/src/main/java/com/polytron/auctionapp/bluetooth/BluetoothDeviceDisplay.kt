package com.polytron.auctionapp.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice

@SuppressLint("MissingPermission")
fun BluetoothDevice.displayNameWithAddressSuffix(): String {
    val deviceName = name?.takeIf { it.isNotBlank() } ?: "Unknown Device"
    val addressSuffix = address
        ?.takeIf { it.isNotBlank() }
        ?.split(":")
        ?.takeLast(2)
        ?.joinToString(":")
        ?.takeIf { it.isNotBlank() }

    return if (addressSuffix == null) {
        deviceName
    } else {
        "$deviceName ($addressSuffix)"
    }
}
