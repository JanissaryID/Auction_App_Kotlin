package com.polytron.auctionapp.bluetooth

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

class BluetoothHelper(private val activity: ComponentActivity) {

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val bluetoothManager =
            activity.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private var onBluetoothReady: (() -> Unit)? = null
    private var onBluetoothFailure: ((String) -> Unit)? = null

    private val permissionLauncher =
        activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allGranted = permissions.all { it.value == true }
            if (allGranted) {
                checkAndEnableBluetooth()
            } else {
                onBluetoothFailure?.invoke("Izin Bluetooth ditolak")
            }
        }

    private val enableBluetoothLauncher =
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                onBluetoothReady?.invoke()
                clearCallbacks()
            } else {
                onBluetoothFailure?.invoke("Bluetooth belum diaktifkan")
                clearCallbacks()
            }
        }

    fun requestBluetooth(
        onReady: () -> Unit,
        onFailure: ((String) -> Unit)? = null
    ) {
        this.onBluetoothReady = onReady
        this.onBluetoothFailure = onFailure

        if (bluetoothAdapter == null) {
            onFailure?.invoke("Perangkat ini tidak mendukung Bluetooth")
            clearCallbacks()
            return
        }

        if (!hasBluetoothPermissions()) {
            permissionLauncher.launch(requiredPermissions())
        } else {
            checkAndEnableBluetooth()
        }
    }

    private fun checkAndEnableBluetooth() {
        if (bluetoothAdapter?.isEnabled == true) {
            onBluetoothReady?.invoke()
            clearCallbacks()
        } else {
            val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            enableBluetoothLauncher.launch(enableIntent)
        }
    }

    fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter?.isEnabled == true
    }

    fun hasBluetoothPermissions(): Boolean {
        return requiredPermissions().all {
            ContextCompat.checkSelfPermission(activity, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requiredPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.ACCESS_FINE_LOCATION // diperlukan untuk scanning di < Android 12
            )
        }
    }

    fun getPairedDevices(): Set<BluetoothDevice> {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED
            ) {
                emptySet()
            } else {
                bluetoothAdapter?.bondedDevices ?: emptySet()
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
            emptySet()
        }
    }

    private fun clearCallbacks() {
        onBluetoothReady = null
        onBluetoothFailure = null
    }
}
