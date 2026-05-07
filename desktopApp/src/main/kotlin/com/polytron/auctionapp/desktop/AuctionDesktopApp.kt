package com.polytron.auctionapp.desktop

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.polytron.auctionapp.desktop.auth.AuthManager
import com.polytron.auctionapp.desktop.auth.AuthState
import com.polytron.auctionapp.desktop.components.NavigationSidebar
import com.polytron.auctionapp.desktop.navigation.NavDestination
import com.polytron.auctionapp.desktop.navigation.NavigationState
import com.polytron.auctionapp.desktop.screens.*
import com.polytron.auctionapp.shared.viewmodel.ItemsSharedViewModel
import org.koin.compose.koinInject

@Composable
fun AuctionDesktopApp() {
    val navigationState = remember { NavigationState() }
    val sharedVm: ItemsSharedViewModel = koinInject()
    val authManager: AuthManager = koinInject()
    
    val authState by authManager.authState.collectAsState()

    // Restore session on startup
    LaunchedEffect(Unit) {
        authManager.restoreSession()
    }

    // Load data when authenticated
    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            sharedVm.refreshItems()
        }
    }

    MaterialTheme(
        colorScheme = lightColorScheme()
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {
            when (authState) {
                is AuthState.Unauthenticated, is AuthState.Error, is AuthState.Loading -> {
                    // Show login screen (it will handle the loading indicator internally if needed)
                    LoginScreen(
                        onLoginSuccess = {
                            // Login successful
                        }
                    )
                }
                is AuthState.Authenticated -> {
                    // Show main app
                    Row(modifier = Modifier.fillMaxSize()) {
                        // Sidebar Navigation
                        NavigationSidebar(
                            currentDestination = navigationState.currentDestination,
                            onNavigate = { destination ->
                                navigationState.navigateTo(destination)
                            },
                            onLogout = {
                                authManager.logout()
                                navigationState.navigateTo(NavDestination.ITEM_LIST)
                            },
                            userName = (authState as? AuthState.Authenticated)?.user?.name
                        )

                        // Content Area
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            when (navigationState.currentDestination) {
                                NavDestination.ITEM_LIST -> ItemListScreen()
                                NavDestination.AUCTION -> AuctionScreen()
                                NavDestination.PAYMENT -> PaymentScreen()
                                NavDestination.TAKE_ITEMS -> TakeItemsScreen()
                                NavDestination.TRANSACTIONS -> TransactionsScreen()
                            }
                        }
                    }
                }
            }
        }
    }
}
