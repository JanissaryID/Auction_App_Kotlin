package com.polytron.auctionapp.desktop

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.polytron.auctionapp.desktop.di.desktopModule
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin

fun main() = application {
    // Initialize Koin (only once)
    if (GlobalContext.getOrNull() == null) {
        startKoin {
            modules(desktopModule)
        }
    }
    
    Window(
        onCloseRequest = ::exitApplication,
        title = "Auction App Desktop",
        state = rememberWindowState(width = 1280.dp, height = 800.dp)
    ) {
        AuctionDesktopApp()
    }
}
