package com.polytron.auctionapp.desktop.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object DesktopDimens {
    val SideNavWidth = 260.dp
    val ContentPadding = 24.dp
    val PanelPadding = 18.dp
    val DialogSmallWidth = 520.dp
    val DialogMediumWidth = 720.dp
    val DialogMaxHeight = 720.dp
}

@Composable
fun DesktopPanel(
    modifier: Modifier = Modifier,
    contentPadding: Dp = DesktopDimens.PanelPadding,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
    }
}

@Composable
fun MetricTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null
) {
    DesktopPanel(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        supportingText?.takeIf { it.isNotBlank() }?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

enum class StatusTone {
    Neutral,
    Ready,
    Warning,
    Paid,
    Complete,
    Error
}

@Composable
fun StatusBadge(
    text: String,
    tone: StatusTone,
    modifier: Modifier = Modifier
) {
    val colors = statusToneColors(tone)
    Surface(
        modifier = modifier.widthIn(min = 84.dp),
        shape = MaterialTheme.shapes.extraSmall,
        color = colors.container,
        border = BorderStroke(1.dp, colors.outline)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = colors.content,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun DesktopToolbar(
    modifier: Modifier = Modifier,
    leading: @Composable RowScope.() -> Unit,
    actions: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        leading()
        actions()
    }
}

private data class StatusToneColors(
    val container: Color,
    val content: Color,
    val outline: Color
)

@Composable
private fun statusToneColors(tone: StatusTone): StatusToneColors {
    return when (tone) {
        StatusTone.Neutral -> StatusToneColors(
            container = MaterialTheme.colorScheme.surfaceVariant,
            content = MaterialTheme.colorScheme.onSurfaceVariant,
            outline = MaterialTheme.colorScheme.outlineVariant
        )

        StatusTone.Ready -> StatusToneColors(
            container = MaterialTheme.colorScheme.primaryContainer,
            content = MaterialTheme.colorScheme.onPrimaryContainer,
            outline = MaterialTheme.colorScheme.primary.copy(alpha = 0.24f)
        )

        StatusTone.Warning -> StatusToneColors(
            container = MaterialTheme.colorScheme.tertiaryContainer,
            content = MaterialTheme.colorScheme.onTertiaryContainer,
            outline = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.28f)
        )

        StatusTone.Paid -> StatusToneColors(
            container = MaterialTheme.colorScheme.secondaryContainer,
            content = MaterialTheme.colorScheme.onSecondaryContainer,
            outline = MaterialTheme.colorScheme.secondary.copy(alpha = 0.28f)
        )

        StatusTone.Complete -> StatusToneColors(
            container = Color(0xFFE1F3DF),
            content = Color(0xFF275C26),
            outline = Color(0xFF9FD39B)
        )

        StatusTone.Error -> StatusToneColors(
            container = MaterialTheme.colorScheme.errorContainer,
            content = MaterialTheme.colorScheme.onErrorContainer,
            outline = MaterialTheme.colorScheme.error.copy(alpha = 0.28f)
        )
    }
}
