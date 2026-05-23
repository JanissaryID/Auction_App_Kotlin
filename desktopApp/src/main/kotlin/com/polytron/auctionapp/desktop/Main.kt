package com.polytron.auctionapp.desktop

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.polytron.auctionapp.desktop.app.AuctionDesktopApp
import com.polytron.auctionapp.desktop.di.desktopAppModule
import com.polytron.auctionapp.di.desktopPlatformModule
import com.polytron.auctionapp.di.sharedModules
import java.awt.Dimension
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin

fun main() {
    startKoin {
        modules(sharedModules + desktopPlatformModule + desktopAppModule)
    }

    application {
        val windowState = rememberWindowState(width = 1280.dp, height = 800.dp)

        Window(
            onCloseRequest = {
                stopKoin()
                exitApplication()
            },
            state = windowState,
            title = "GKJ Lelang",
            icon = painterResource("icon_app.png")
        ) {
            LaunchedEffect(Unit) {
                window.minimumSize = Dimension(1100, 700)
            }

            AuctionDesktopApp()
        }
    }
}
