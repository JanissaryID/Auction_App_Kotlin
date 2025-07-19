package com.polytron.auctionapp

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import com.polytron.auctionapp.navigation.AppNavHost
import com.polytron.auctionapp.ui.theme.AuctionAppTheme
import com.polytron.auctionapp.bluetooth.BluetoothHelper

class MainActivity : ComponentActivity() {

    private lateinit var bluetoothHelper: BluetoothHelper

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        bluetoothHelper = BluetoothHelper(this)

        setContent {
            AuctionAppTheme(
                darkTheme = false
            ) {
                AppNavHost(bluetoothHelper = bluetoothHelper)
            }
        }
    }
}