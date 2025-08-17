package com.example.todolist.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.colorResource
import androidx.core.view.WindowCompat

// Default color schemes
private val LightColors = lightColorScheme(
    primary = Color(0xFF1976D2),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD3E3FD),
    onPrimaryContainer = Color(0xFF001C38),
    secondary = Color(0xFF565E71),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFDAE2F9),
    onSecondaryContainer = Color(0xFF131B2C),
    tertiary = Color(0xFF705575),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFAD8FD),
    onTertiaryContainer = Color(0xFF28132E),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFFDFCFF),
    onBackground = Color(0xFF1A1C1E),
    surface = Color(0xFFFDFCFF),
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFE1E2EC),
    onSurfaceVariant = Color(0xFF44474F),
    outline = Color(0xFF74777F),
    outlineVariant = Color(0xFFC4C7CF),
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFF2F3033),
    inverseOnSurface = Color(0xFFF1F0F4),
    inversePrimary = Color(0xFFA4C8FF),
    surfaceDim = Color(0xFFDDD9E0),
    surfaceBright = Color(0xFFFDFCFF),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF7F2FA),
    surfaceContainer = Color(0xFFF2F3FA),
    surfaceContainerHigh = Color(0xFFEBE6EE),
    surfaceContainerHighest = Color(0xFFE5E1E9)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFA4C8FF),
    onPrimary = Color(0xFF003062),
    primaryContainer = Color(0xFF004788),
    onPrimaryContainer = Color(0xFFD3E3FD),
    secondary = Color(0xFFBEC6DC),
    onSecondary = Color(0xFF283041),
    secondaryContainer = Color(0xFF3F4759),
    onSecondaryContainer = Color(0xFFDAE2F9),
    tertiary = Color(0xFFDEBCE0),
    onTertiary = Color(0xFF3F2844),
    tertiaryContainer = Color(0xFF573E5C),
    onTertiaryContainer = Color(0xFFFAD8FD),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF111316),
    onBackground = Color(0xFFE2E2E6),
    surface = Color(0xFF111316),
    onSurface = Color(0xFFE2E2E6),
    surfaceVariant = Color(0xFF44474F),
    onSurfaceVariant = Color(0xFFC4C7CF),
    outline = Color(0xFF8E9099),
    outlineVariant = Color(0xFF44474F),
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFFE2E2E6),
    inverseOnSurface = Color(0xFF2E3036),
    inversePrimary = Color(0xFF1976D2),
    surfaceDim = Color(0xFF111316),
    surfaceBright = Color(0xFF37393C),
    surfaceContainerLowest = Color(0xFF0C0E11),
    surfaceContainerLow = Color(0xFF191C1E),
    surfaceContainer = Color(0xFF1D2022),
    surfaceContainerHigh = Color(0xFF282A2D),
    surfaceContainerHighest = Color(0xFF323438)
)

/**
 * Create a light color scheme with custom accent color
 */
fun createLightColorScheme(accentColor: Color) = LightColors.copy(
    primary = accentColor,
    primaryContainer = Color(
        red = (accentColor.red + (1f - accentColor.red) * 0.7f).coerceIn(0f, 1f),
        green = (accentColor.green + (1f - accentColor.green) * 0.7f).coerceIn(0f, 1f),
        blue = (accentColor.blue + (1f - accentColor.blue) * 0.7f).coerceIn(0f, 1f),
        alpha = 1f
    ),
    onPrimaryContainer = Color(
        red = (accentColor.red * 0.2f).coerceIn(0f, 1f),
        green = (accentColor.green * 0.2f).coerceIn(0f, 1f),
        blue = (accentColor.blue * 0.2f).coerceIn(0f, 1f),
        alpha = 1f
    ),
    inversePrimary = accentColor
)

/**
 * Create a dark color scheme with custom accent color
 */
fun createDarkColorScheme(accentColor: Color) = DarkColors.copy(
    primary = accentColor,
    primaryContainer = Color(
        red = (accentColor.red * 0.3f).coerceIn(0f, 1f),
        green = (accentColor.green * 0.3f).coerceIn(0f, 1f),
        blue = (accentColor.blue * 0.3f).coerceIn(0f, 1f),
        alpha = 1f
    ),
    onPrimaryContainer = Color(
        red = (accentColor.red + (1f - accentColor.red) * 0.7f).coerceIn(0f, 1f),
        green = (accentColor.green + (1f - accentColor.green) * 0.7f).coerceIn(0f, 1f),
        blue = (accentColor.blue + (1f - accentColor.blue) * 0.7f).coerceIn(0f, 1f),
        alpha = 1f
    ),
    inversePrimary = accentColor
)

@Composable
fun MyToDoAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set to false to use custom accent colors
    accentColor: Color = Color(0xFF0733F5), // Default blue
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> createDarkColorScheme(accentColor)
        else -> createLightColorScheme(accentColor)
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography
    ) {
        ProvideDynamicColors(
            accentColor = accentColor,
            content = content
        )
    }
}