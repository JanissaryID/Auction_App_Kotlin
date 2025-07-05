package com.polytron.auctionapp.view.components.camera

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

@Composable
fun CameraPermissionHandler(
    onGranted: () -> Unit,
    onDenied: () -> Unit
): () -> Unit {
    val context = LocalContext.current
    val permission = Manifest.permission.CAMERA
    val permissionState = remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onGranted()
        } else {
            onDenied()
        }
    }

    return {
        when {
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED -> {
                onGranted()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(context as Activity, permission) -> {
                // User menolak tapi belum "Don't ask again"
                launcher.launch(permission)
            }
            else -> {
                // Pertama kali atau sudah "Don't ask again"
                launcher.launch(permission)
            }
        }
    }
}
