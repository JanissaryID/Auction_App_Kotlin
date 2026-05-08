package com.polytron.auctionapp.desktop.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.polytron.auctionapp.desktop.navigation.DesktopDestination

@Composable
fun SideNavigation(
    currentDestination: DesktopDestination,
    isLoggedIn: Boolean,
    userName: String?,
    onDestinationSelected: (DesktopDestination) -> Unit,
    onLoginClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(260.dp)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surface)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Auction App",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Desktop",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(Modifier.height(16.dp))

        DesktopDestination.entries.forEach { destination ->
            SideNavigationItem(
                destination = destination,
                selected = destination == currentDestination,
                onClick = { onDestinationSelected(destination) }
            )
        }

        Spacer(Modifier.weight(1f))

        if (isLoggedIn) {
            Text(
                text = userName?.takeIf { it.isNotBlank() } ?: "Pengguna",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            OutlinedButton(
                onClick = onProfileClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Profil")
            }
        } else {
            Button(
                onClick = onLoginClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Login")
            }
        }
    }
}

@Composable
private fun SideNavigationItem(
    destination: DesktopDestination,
    selected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (selected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    } else {
        MaterialTheme.colorScheme.surface
    }
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    TextButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp)
            .clip(MaterialTheme.shapes.small)
            .background(backgroundColor)
    ) {
        Text(
            text = destination.label,
            color = contentColor,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
