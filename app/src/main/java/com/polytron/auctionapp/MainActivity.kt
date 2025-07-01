package com.polytron.auctionapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.polytron.auctionapp.ui.theme.AuctionAppTheme
import com.polytron.auctionapp.view.screens.ItemListScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AuctionAppTheme {
                ItemListScreen()
            }
        }
    }
}