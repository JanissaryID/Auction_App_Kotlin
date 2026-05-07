package com.polytron.auctionapp.desktop.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp

enum class SortOption(val label: String) {
    NAME_ASC("Nama (A-Z)"),
    NAME_DESC("Nama (Z-A)"),
    PRICE_ASC("Harga (Terendah)"),
    PRICE_DESC("Harga (Tertinggi)"),
    CODE_ASC("Kode (A-Z)"),
    CODE_DESC("Kode (Z-A)")
}

enum class StatusFilter(val label: String, val value: Int?) {
    ALL("Semua Status", null),
    AVAILABLE("Tersedia", 1),
    PAID("Dibayar", 2),
    TAKEN("Diambil", 3)
}

@Composable
fun FilterAndSortBar(
    selectedSort: SortOption,
    onSortChange: (SortOption) -> Unit,
    selectedStatus: StatusFilter,
    onStatusChange: (StatusFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSortMenu by remember { mutableStateOf(false) }
    var showStatusMenu by remember { mutableStateOf(false) }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Sort Button
        FilterChip(
            selected = true,
            onClick = { showSortMenu = true },
            label = { Text(selectedSort.label) },
            leadingIcon = {
                Icon(
                    Icons.Default.Sort,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        )

        DropdownMenu(
            expanded = showSortMenu,
            onDismissRequest = { showSortMenu = false }
        ) {
            SortOption.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label) },
                    onClick = {
                        onSortChange(option)
                        showSortMenu = false
                    },
                    leadingIcon = {
                        if (selectedSort == option) {
                            Icon(Icons.Default.Check, null, modifier = Modifier.size(20.dp))
                        }
                    }
                )
            }
        }

        // Status Filter Button
        FilterChip(
            selected = selectedStatus != StatusFilter.ALL,
            onClick = { showStatusMenu = true },
            label = { Text(selectedStatus.label) },
            leadingIcon = {
                Icon(
                    Icons.Default.FilterList,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        )

        DropdownMenu(
            expanded = showStatusMenu,
            onDismissRequest = { showStatusMenu = false }
        ) {
            StatusFilter.entries.forEach { filter ->
                DropdownMenuItem(
                    text = { Text(filter.label) },
                    onClick = {
                        onStatusChange(filter)
                        showStatusMenu = false
                    },
                    leadingIcon = {
                        if (selectedStatus == filter) {
                            Icon(Icons.Default.Check, null, modifier = Modifier.size(20.dp))
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun AnimatedItemCard(
    visible: Boolean,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(300)) + 
                expandVertically(animationSpec = tween(300)),
        exit = fadeOut(animationSpec = tween(300)) + 
               shrinkVertically(animationSpec = tween(300))
    ) {
        content()
    }
}

@Composable
fun LoadingAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Default.Refresh,
            contentDescription = "Loading",
            modifier = Modifier
                .size(48.dp)
                .rotate(rotation),
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun ExportButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.tertiary
        )
    ) {
        Icon(
            Icons.Default.Download,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("Export PDF")
    }
}

@Composable
fun BulkActionBar(
    selectedCount: Int,
    onClearSelection: () -> Unit,
    onBulkAction: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = selectedCount > 0,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
    ) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primaryContainer,
            tonalElevation = 4.dp,
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onClearSelection) {
                        Icon(Icons.Default.Close, "Clear selection")
                    }
                    Text(
                        text = "$selectedCount item dipilih",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(
                        onClick = { onBulkAction("mark_paid") }
                    ) {
                        Icon(Icons.Default.Payment, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tandai Dibayar")
                    }

                    FilledTonalButton(
                        onClick = { onBulkAction("mark_taken") }
                    ) {
                        Icon(Icons.Default.LocalShipping, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tandai Diambil")
                    }

                    FilledTonalButton(
                        onClick = { onBulkAction("delete") },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Hapus")
                    }
                }
            }
        }
    }
}

@Composable
fun SuccessAnimation(
    message: String,
    onDismiss: () -> Unit
) {
    var visible by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(2000)
        visible = false
        kotlinx.coroutines.delay(300)
        onDismiss()
    }

    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
        exit = scaleOut() + fadeOut()
    ) {
        Surface(
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(12.dp),
            tonalElevation = 8.dp
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}
