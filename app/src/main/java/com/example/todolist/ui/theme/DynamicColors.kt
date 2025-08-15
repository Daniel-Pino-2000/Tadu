package com.example.todolist.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

// Create composition locals for dynamic colors
val LocalAccentColor = compositionLocalOf { Color(0xFF1976D2) }
val LocalDynamicColors = compositionLocalOf { DynamicColors() }

data class DynamicColors(
    var niceColor: Color = Color(0xFF1976D2), // Make it var
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

/**
 * Create dynamic colors based on accent color
 */
fun createDynamicColors(accentColor: Color): DynamicColors {
    return DynamicColors(
        niceColor = accentColor,
        blueToday = accentColor,
        // Keep other colors as they are, or modify them based on accent color if needed
        lightGray = Color(0xFFF2F3FA),
        redYesterday = Color(0xFFCF0C0C),
        greenTomorrow = Color(0xFF08A625),
        orange = Color(0xFFF56642),
        priorityBlue = Color(0xFFA3ADF8),
        priorityRed = Color(0xFFDFB3B3),
        priorityOrange = Color(0xFFDFBCA8),
        searchBarGray = Color(0xFF8494AD),
        dropMenuIconGray = Color(0xFF696766)
    )
}

@Composable
fun ProvideDynamicColors(
    accentColor: Color,
    content: @Composable () -> Unit
) {
    val dynamicColors = createDynamicColors(accentColor)

    CompositionLocalProvider(
        LocalAccentColor provides accentColor,
        LocalDynamicColors provides dynamicColors,
        content = content
    )
}