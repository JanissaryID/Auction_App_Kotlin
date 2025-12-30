package com.polytron.auctionapp

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import com.polytron.auctionapp.bluetooth.BluetoothHelper
import com.polytron.auctionapp.navigation.AppNavHost
import com.polytron.auctionapp.ui.theme.AuctionAppTheme

class MainActivity : ComponentActivity() {

    private lateinit var bluetoothHelper: BluetoothHelper

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                Color.TRANSPARENT,
                Color.TRANSPARENT
            )
        )

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