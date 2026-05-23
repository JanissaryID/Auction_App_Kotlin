package com.polytron.auctionapp.view.components.fab

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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

    if (isSelectionMode) {
        ExtendedFloatingActionButton(
            onClick = {
                if (!isDeleting) {
                    coroutineScope.launch { onDelete() }
                }
            },
            icon = {
                if (isDeleting) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Hapus Item",
                        tint = Color.White
                    )
                }
            },
            text = { Text(if (isDeleting) "Menghapus..." else "Hapus") },
            containerColor = if (isDeleting) Color.Gray else Color.Red,
            contentColor = Color.White,
            modifier = Modifier.alpha(if (isDeleting) 0.6f else 1f)
        )
    } else {
        FloatingActionButton(
            onClick = {
                onAddClick()
            },
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Tambah Item"
            )
        }
    }
}
