package com.polytron.auctionapp.view.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SelectedItemsBottomBar(
    selectedCount: Int,
    buttonText: String,
    isSubmitting: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    if (selectedCount > 0) {
        BottomAppBar(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "$selectedCount barang dipilih",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = onClick,
                enabled = enabled && !isSubmitting,
                modifier = Modifier.defaultMinSize(minHeight = 48.dp)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(buttonText)
                }
            }
        }
    }
}
