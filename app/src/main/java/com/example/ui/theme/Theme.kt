package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

@Composable
fun KiwiBuildingTheme(
    faceId: Int = 1,
    themeMode: String = "SYSTEM",
    accentColorHex: String = "#1E3A8A",
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        "DARK" -> true
        "LIGHT" -> false
        else -> isSystemDark
    }

    val customAccent = try {
        Color(android.graphics.Color.parseColor(accentColorHex))
    } catch (e: Exception) {
        null
    }

    val colorScheme = getFaceColorScheme(faceId, isDark, customAccent)

    // RTL for Persian language
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
