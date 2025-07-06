package com.polytron.auctionapp.view.components.fab

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.polytron.auctionapp.view.components.camera.cameraPermissionHandler

@Composable
fun FabWithSubmenu(
    modifier: Modifier = Modifier,
    isFabExpanded: Boolean,
    onFabToggle: () -> Unit,
    onDismissRequest: () -> Unit,
    navScanBarcode: () -> Unit,
    navListItems: () -> Unit,
) {
    val context = LocalContext.current

    val requestCameraPermission = cameraPermissionHandler(
        onGranted = {
            onDismissRequest()
            navScanBarcode()
        },
        onDenied = {
            Toast.makeText(context, "Izin kamera ditolak.", Toast.LENGTH_SHORT).show()
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
            context.startActivity(intent)
        }
    )

    Box(modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
        if (isFabExpanded) {
            Box(
                modifier = Modifier.fillMaxSize().clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onDismissRequest() }
            )
        }
        AnimatedVisibility(
            visible = isFabExpanded,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.End,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 72.dp)
            ) {
                FabWithLabel("Scan Barcode", Icons.Default.CameraAlt) {
                    requestCameraPermission()
                }
                FabWithLabel("Daftar Item", Icons.AutoMirrored.Filled.List) {
                    onDismissRequest()
                    navListItems()
                }
            }
        }
        FloatingActionButton(
            onClick = onFabToggle,
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(if (isFabExpanded) Icons.Default.Close else Icons.Default.Add, null)
        }
    }
}
