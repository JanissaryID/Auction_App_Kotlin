package com.polytron.auctionapp.desktop

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Auction App"
    ) {
        AuctionDesktopApp()
    }
}

@Composable
private fun AuctionDesktopApp() {
    MaterialTheme {
        Surface {
            Text("Auction App Desktop")
        }
    }
}
