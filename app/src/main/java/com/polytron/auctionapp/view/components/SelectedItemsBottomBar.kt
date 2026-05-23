package com.polytron.auctionapp.view.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SelectedItemsBottomBar(
    selectedCount: Int,
    buttonText: String,
    isSubmitting: Boolean = false,
    submittingText: String = "Memproses...",
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(submittingText)
                    }
                } else {
                    Text(buttonText)
                }
            }
        }
    }
}
