package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = ImmersivePrimary,
    secondary = ImmersiveSecondary,
    tertiary = AccentEmerald,
    background = ImmersiveBackground,
    surface = ImmersiveSurface,
    onPrimary = ImmersiveOnPrimary,
    onSecondary = ImmersiveTextPrimary,
    onBackground = ImmersiveTextPrimary,
    onSurface = ImmersiveTextPrimary,
    surfaceVariant = ImmersiveBorder,
    error = AccentRed,
    onError = ImmersiveTextPrimary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to stunning dark theme for lottery application
    dynamicColor: Boolean = false, // Keep brand consistency
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
