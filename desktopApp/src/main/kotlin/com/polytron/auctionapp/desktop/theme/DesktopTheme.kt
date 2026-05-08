package com.polytron.auctionapp.desktop.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DesktopColorScheme = lightColorScheme(
    primary = Color(0xFF1E5B4F),
    onPrimary = Color.White,
    secondary = Color(0xFF315F8A),
    onSecondary = Color.White,
    tertiary = Color(0xFF76552A),
    background = Color(0xFFF7F8FA),
    surface = Color.White,
    surfaceVariant = Color(0xFFE8ECEF),
    outline = Color(0xFFCBD3D9),
    outlineVariant = Color(0xFFE1E6EA),
    error = Color(0xFFB3261E)
)

@Composable
fun DesktopTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DesktopColorScheme,
        typography = MaterialTheme.typography,
        content = content
    )
}
