package com.polytron.auctionapp

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.polytron.auctionapp.bluetooth.BluetoothHelper
import com.polytron.auctionapp.data.session.SessionManager
import com.polytron.auctionapp.navigation.AppNavHost
import com.polytron.auctionapp.ui.theme.AuctionAppTheme
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private lateinit var bluetoothHelper: BluetoothHelper
    private val sessionManager: SessionManager by inject()

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

        // Observe global session expired events — show Toast and let AuthViewModel handle navigation
        lifecycleScope.launch {
            sessionManager.sessionExpiredEvent.collect { message ->
                Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
            }
        }

        setContent {
            AuctionAppTheme(darkTheme = false) {
                AppNavHost(bluetoothHelper = bluetoothHelper)
            }
        }
    }
}