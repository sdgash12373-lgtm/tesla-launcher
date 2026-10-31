package com.sdgash.carlauncher.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object CarColors {
    val Background = Color(0xFF000000)
    val Panel = Color(0xFF17181C)
    val PanelRaised = Color(0xFF222429)
    val Dock = Color(0xFF0E0F12)
    val TextPrimary = Color(0xFFF2F2F2)
    val TextSecondary = Color(0xFF8E9096)
    val Accent = Color(0xFF3E6AE1)      // the calm blue used for highlights
    val Red = Color(0xFFE31937)
    val Green = Color(0xFF33C46B)
}

@Composable
fun CarTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = CarColors.Accent,
            background = CarColors.Background,
            surface = CarColors.Panel,
            onSurface = CarColors.TextPrimary,
            onBackground = CarColors.TextPrimary,
        ),
        content = content,
    )
}
