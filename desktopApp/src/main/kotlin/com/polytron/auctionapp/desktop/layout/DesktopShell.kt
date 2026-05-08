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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.polytron.auctionapp.desktop.components.DesktopDimens
import com.polytron.auctionapp.desktop.components.StatusBadge
import com.polytron.auctionapp.desktop.components.StatusTone
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
                    .background(MaterialTheme.colorScheme.background)
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
                        .padding(DesktopDimens.ContentPadding)
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
    Surface(
        modifier = Modifier
            .fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
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

            StatusBadge(
                text = if (isLoggedIn) userName?.takeIf { it.isNotBlank() } ?: "Login" else "Belum login",
                tone = if (isLoggedIn) StatusTone.Ready else StatusTone.Neutral
            )
            Spacer(Modifier.width(12.dp))

            OutlinedButton(
                onClick = onRefreshClick,
                contentPadding = ButtonDefaults.ButtonWithIconContentPadding
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Refresh")
            }
            Spacer(Modifier.width(8.dp))
            if (isLoggedIn) {
                Button(
                    onClick = onProfileClick,
                    contentPadding = ButtonDefaults.ButtonWithIconContentPadding
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Profil")
                }
            } else {
                Button(
                    onClick = onLoginClick,
                    contentPadding = ButtonDefaults.ButtonWithIconContentPadding
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Login,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Login")
                }
            }
        }
    }
}
