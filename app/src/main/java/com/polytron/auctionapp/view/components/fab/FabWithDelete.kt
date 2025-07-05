package com.polytron.auctionapp.view.components.fab

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun FabWithDelete(
    isSelectionMode: Boolean,
    isDeleting: Boolean,
    onDelete: suspend () -> Unit,
    onAddClick: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    FloatingActionButton(
        onClick = {
            if (isSelectionMode) {
                if (!isDeleting) {
                    coroutineScope.launch { onDelete() }
                }
            } else {
                onAddClick()
            }
        },
        containerColor = if (isSelectionMode) {
            if (isDeleting) Color.Gray else Color.Red
        } else {
            MaterialTheme.colorScheme.primary
        },
        modifier = Modifier.alpha(
            if (isSelectionMode && isDeleting) 0.6f else 1f
        )
    ) {
        if (isSelectionMode) {
            if (isDeleting) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Hapus Item",
                    tint = Color.White
                )
            }
        } else {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Tambah Item"
            )
        }
    }
}
