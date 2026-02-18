package com.myapp.tadu.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myapp.tadu.ThemeMode
import com.myapp.tadu.ui.theme.getCommonAccentColors
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color

data class SettingsState(
    val isLoading: Boolean = true,
    val settingsLoaded: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val accentColorIndex: Int = 0,
    val notificationsEnabled: Boolean = true,
)

class SettingsViewModel(private val repo: SettingsRepository) : ViewModel() {

    val settingsState: StateFlow<SettingsState> = combine(
        repo.themeMode,
        repo.accentColorIndex,
        repo.notificationsEnabled
    ) { themeMode, accentColorIndex, notificationsEnabled ->
        SettingsState(
            isLoading = false,
            settingsLoaded = true,
            themeMode = themeMode,
            accentColorIndex = accentColorIndex,
            notificationsEnabled = notificationsEnabled
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = SettingsState(isLoading = true, settingsLoaded = false)
    )

    val themeMode = repo.themeMode.stateIn(viewModelScope, SharingStarted.Eagerly, ThemeMode.SYSTEM)
    val accentColorIndex = repo.accentColorIndex.stateIn(viewModelScope, SharingStarted.Eagerly, 0)
    val notificationsEnabled = repo.notificationsEnabled.stateIn(viewModelScope, SharingStarted.Eagerly, true)

    // Keep accentColor flow for backward compatibility with the rest of the codebase.
    // It always returns the LIGHT theme color — the correct themed color is derived
    // in MainActivity using accentColorIndex + isDarkTheme. Files like TaskReminderCard
    // that read this flow will get the base color; MainActivity overrides with the
    // properly themed version at the top level via MyToDoAppTheme.
    val accentColor: StateFlow<Color> = repo.accentColorIndex.map { index ->
        val palette = getCommonAccentColors(isDarkTheme = false)
        palette[index.coerceIn(0, palette.lastIndex)]
    }.stateIn(viewModelScope, SharingStarted.Eagerly, Color(0xFF0733F5))

    fun updateThemeMode(mode: ThemeMode) {
        viewModelScope.launch { repo.setThemeMode(mode) }
    }

    fun updateAccentColorIndex(index: Int) {
        viewModelScope.launch { repo.setAccentColorIndex(index) }
    }

    // Keep updateAccentColor for backward compatibility - finds the closest index
    fun updateAccentColor(color: Color) {
        viewModelScope.launch {
            val lightColors = getCommonAccentColors(isDarkTheme = false)
            val darkColors = getCommonAccentColors(isDarkTheme = true)
            // Try to find in either palette
            val index = (lightColors.indexOfFirst { it == color }).takeIf { it >= 0 }
                ?: (darkColors.indexOfFirst { it == color }).takeIf { it >= 0 }
                ?: 0
            repo.setAccentColorIndex(index)
        }
    }

    fun updateNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            try {
                repo.setNotificationsEnabled(enabled)
                Log.d("SettingsViewModel", "Notifications enabled updated to: $enabled")
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "Error updating notifications setting", e)
            }
        }
    }
}