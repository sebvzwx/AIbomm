package com.example.aibomm.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = IOSBlue,
    background = IOSBackground,
    surface = IOSSurface,
    onPrimary = Color.White,
    onBackground = Color(0xFF0A0A0A),
    onSurface = Color(0xFF0A0A0A)
)

private val DarkColors = darkColorScheme(
    primary = IOSBlue,
    background = Color(0xFF000000),
    surface = Color(0xFF1C1C1E),
    onPrimary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun AIBommTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
