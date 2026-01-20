package com.battle4play.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF0D47A1),
    secondary = Color(0xFF5C6BC0),
    tertiary = Color(0xFFFFA000)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF90CAF9),
    secondary = Color(0xFF9FA8DA),
    tertiary = Color(0xFFFFD54F)
)

@Composable
fun Battle4PlayTheme(content: @Composable () -> Unit) {
    val colors = if (androidx.compose.foundation.isSystemInDarkTheme()) {
        DarkColors
    } else {
        LightColors
    }

    MaterialTheme(
        colorScheme = colors,
        typography = androidx.compose.material3.Typography(),
        content = content
    )
}
