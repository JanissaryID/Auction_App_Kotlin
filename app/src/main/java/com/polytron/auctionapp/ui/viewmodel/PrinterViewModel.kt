package com.polytron.auctionapp.ui.viewmodel

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages Bluetooth printer connection state.
 * Shared across all screens that need printer access.
 */
class PrinterViewModel : ViewModel() {

    private val _isBluetoothConnected = MutableStateFlow(false)
    val isBluetoothConnected: StateFlow<Boolean> = _isBluetoothConnected.asStateFlow()

    private val _showBluetoothDevice = MutableStateFlow(false)
    val showBluetoothDevice: StateFlow<Boolean> = _showBluetoothDevice.asStateFlow()

    private val _selectedPrinter = MutableStateFlow<BluetoothDevice?>(null)
    val selectedPrinter: StateFlow<BluetoothDevice?> = _selectedPrinter.asStateFlow()

    fun setSelectedPrinter(device: BluetoothDevice) {
        _selectedPrinter.value = device
        _isBluetoothConnected.value = true
    }

    fun showBluetoothDevice(show: Boolean) {
        _showBluetoothDevice.value = show
    }
}
