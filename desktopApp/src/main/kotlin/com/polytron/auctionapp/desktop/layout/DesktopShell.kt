package com.polytron.auctionapp.desktop.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.polytron.auctionapp.desktop.navigation.DesktopDestination

@Composable
fun DesktopShell(
    currentDestination: DesktopDestination,
    isLoggedIn: Boolean,
    userName: String?,
    snackbarHostState: SnackbarHostState,
    onDestinationSelected: (DesktopDestination) -> Unit,
    onLoginClick: () -> Unit,
    onProfileClick: () -> Unit,
    onRefreshClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { contentPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            SideNavigation(
                currentDestination = currentDestination,
                isLoggedIn = isLoggedIn,
                userName = userName,
                onDestinationSelected = onDestinationSelected,
                onLoginClick = onLoginClick,
                onProfileClick = onProfileClick
            )

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                ContentHeader(
                    destination = currentDestination,
                    isLoggedIn = isLoggedIn,
                    userName = userName,
                    onRefreshClick = onRefreshClick,
                    onLoginClick = onLoginClick,
                    onProfileClick = onProfileClick
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun ContentHeader(
    destination: DesktopDestination,
    isLoggedIn: Boolean,
    userName: String?,
    onRefreshClick: () -> Unit,
    onLoginClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 24.dp, vertical = 18.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = destination.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = destination.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        TextButton(onClick = onRefreshClick) {
            Text("Refresh")
        }
        Spacer(Modifier.width(8.dp))
        if (isLoggedIn) {
            Button(onClick = onProfileClick) {
                Text(userName?.takeIf { it.isNotBlank() } ?: "Profil")
            }
        } else {
            Button(onClick = onLoginClick) {
                Text("Login")
            }
        }
    }
}
