package com.polytron.auctionapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.polytron.auctionapp.navigation.AppNavHost
import com.polytron.auctionapp.ui.theme.AuctionAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AuctionAppTheme {
                AppNavHost()
            }
        }
    }
}