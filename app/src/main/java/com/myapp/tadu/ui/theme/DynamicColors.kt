// Updated theme.kt file with proper theme handling

package com.myapp.tadu.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

// Create composition locals for dynamic colors
val LocalAccentColor = compositionLocalOf { Color(0xFF0733F5) }
val LocalDynamicColors = compositionLocalOf { DynamicColors() }

data class DynamicColors(
    var niceColor: Color = Color(0xFF0733F5),
    val lightGray: Color = Color(0xFFF2F3FA),
    val blueToday: Color = Color(0xFF1976D2),
    val redYesterday: Color = Color(0xFFCF0C0C),
    val greenTomorrow: Color = Color(0xFF08A625),
    val orange: Color = Color(0xFFF56642),
    val priorityBlue: Color = Color(0xFFA3ADF8),
    val priorityRed: Color = Color(0xFFDFB3B3),
    val priorityOrange: Color = Color(0xFFDFBCA8),
    val searchBarGray: Color = Color(0xFF8494AD),
    val dropMenuIconGray: Color = Color(0xFF696766)
) {
    var niceColorState by mutableStateOf(niceColor)
}

fun getCommonAccentColors(isDarkTheme: Boolean): List<Color> {
    return if (isDarkTheme) {
        // Dark theme colors
        listOf(
            Color(0xFF4285F4), // Your nice blue (brightened for dark theme)
            Color(0xFFBB86FC), // Light Purple
            Color(0xFF03DAC6), // Teal / Cyan
            Color(0xFF4CAF50), // Green
            Color(0xFFFFB300), // Amber / Orange
            Color(0xFFCE93D8), // Lavender Purple
            Color(0xFF80CBC4), // Muted Teal
            Color(0xFFFF5252), // Red
        )
    } else {
        // Light theme colors
        listOf(
            Color(0xFF0733F5), // Your nice blue (original)
            Color(0xFF7B1FA2), // Purple
            Color(0xFF009688), // Teal
            Color(0xFF388E3C), // Green
            Color(0xFFF57C00), // Orange
            Color(0xFF5E35B1), // Deep Purple
            Color(0xFF00838F), // Cyan-ish Teal
            Color(0xFFD32F2F), // Red
        )
    }
}

private fun getThemeAdjustedAccentColor(color: Color, isDarkTheme: Boolean): Color {
    return if (isDarkTheme) {
        Color(
            red = (color.red + (1f - color.red) * 0.3f).coerceIn(0f, 1f),
            green = (color.green + (1f - color.green) * 0.3f).coerceIn(0f, 1f),
            blue = (color.blue + (1f - color.blue) * 0.3f).coerceIn(0f, 1f),
            alpha = color.alpha
        )
    } else {
        color
    }
}

// Updated to accept isDarkTheme parameter instead of using isSystemInDarkTheme()
@Composable
fun createDynamicColors(accentColor: Color, isDarkTheme: Boolean): DynamicColors {
    val adjustedAccentColor = getThemeAdjustedAccentColor(accentColor, isDarkTheme)

    return DynamicColors(
        niceColor = adjustedAccentColor,
        lightGray = if (isDarkTheme) Color(0xFF2A2D32) else Color(0xFFF2F3FA),
        blueToday = if (isDarkTheme) Color(0xFF4285F4) else Color(0xFF0733F5),
        redYesterday = if (isDarkTheme) Color(0xFFEF5350) else Color(0xFFCF0C0C),
        greenTomorrow = if (isDarkTheme) Color(0xFF66BB6A) else Color(0xFF08A625),
        orange = if (isDarkTheme) Color(0xFFFF8A65) else Color(0xFFF56642),
        priorityBlue = if (isDarkTheme) Color(0xFF90CAF9) else Color(0xFFA3ADF8),
        priorityRed = if (isDarkTheme) Color(0xFFFFCDD2) else Color(0xFFDFB3B3),
        priorityOrange = if (isDarkTheme) Color(0xFFFFE0B2) else Color(0xFFDFBCA8),
        searchBarGray = if (isDarkTheme) Color(0xFFB0BEC5) else Color(0xFF8494AD),
        dropMenuIconGray = if (isDarkTheme) Color(0xFF9E9E9E) else Color(0xFF696766)
    )
}

@Composable
fun ProvideDynamicColors(
    accentColor: Color,
    isDarkTheme: Boolean, // Add this parameter
    content: @Composable () -> Unit
) {
    val dynamicColors = createDynamicColors(accentColor, isDarkTheme)

    CompositionLocalProvider(
        LocalAccentColor provides accentColor,
        LocalDynamicColors provides dynamicColors,
        content = content
    )
}