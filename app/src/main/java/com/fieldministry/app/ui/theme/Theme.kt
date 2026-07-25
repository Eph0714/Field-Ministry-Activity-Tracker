package com.fieldministry.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val BannerBlue = Color(0xFF1565C0)

private val AppColorScheme = lightColorScheme(
    primary = BannerBlue,
    onPrimary = Color.White,
    background = Color.White,
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black,
)

@Composable
fun FieldMinistryTheme(content: @Composable () -> Unit) {
    // Fixed light scheme regardless of system dark-mode setting, matching this app family's convention.
    MaterialTheme(
        colorScheme = AppColorScheme,
        content = content,
    )
}
