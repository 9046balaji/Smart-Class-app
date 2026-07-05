package com.vfstr.smartclass.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Default is Dark only as per Rule 23. Light toggle can be used inside Student Portal.
val DarkColorScheme = darkColorScheme(
    primary = AccentCyan,
    onPrimary = BackgroundDark,
    primaryContainer = AccentViolet,
    onPrimaryContainer = TextPrimary,
    secondary = AccentViolet,
    onSecondary = TextPrimary,
    background = BackgroundDark,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = Color(0x1F2937FF),
    onSurfaceVariant = TextSecondary,
    error = DangerRed,
    onError = TextPrimary,
    outline = BorderColor
)

val LightColorScheme = lightColorScheme(
    primary = VignanBlue,
    onPrimary = Color.White,
    primaryContainer = AccentCyan,
    onPrimaryContainer = Color.Black,
    secondary = AccentViolet,
    onSecondary = Color.White,
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF64748B),
    error = DangerRed,
    onError = Color.White,
    outline = Color(0xFFE2E8F0)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default and only theme represents elegant dark
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

