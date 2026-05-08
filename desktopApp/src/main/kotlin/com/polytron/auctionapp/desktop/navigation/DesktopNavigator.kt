package com.polytron.auctionapp.desktop.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

class DesktopNavigator(
    initialDestination: DesktopDestination = DesktopDestination.Dashboard
) {
    var destination by mutableStateOf(initialDestination)
        private set

    var dialog by mutableStateOf<DesktopDialog?>(null)
        private set

    fun navigate(destination: DesktopDestination) {
        this.destination = destination
    }

    fun showDialog(dialog: DesktopDialog) {
        this.dialog = dialog
    }

    fun closeDialog() {
        dialog = null
    }
}

@Composable
fun rememberDesktopNavigator(): DesktopNavigator {
    return remember { DesktopNavigator() }
}
