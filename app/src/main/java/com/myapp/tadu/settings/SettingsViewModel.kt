package com.myapp.tadu.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myapp.tadu.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color

data class SettingsState(
    val isLoading: Boolean = true,
    val settingsLoaded: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val accentColor: Color = Color(0xFF0733F5),
    val notificationsEnabled: Boolean = true,
    val clearHistoryEnabled: Boolean = false
)

class SettingsViewModel(private val repo: SettingsRepository) : ViewModel() {

    // Combined settings state that indicates when all settings are loaded
    val settingsState: StateFlow<SettingsState> = combine(
        repo.themeMode,
        repo.accentColor,
        repo.notificationsEnabled
    ) { themeMode, accentColor, notificationsEnabled ->
        SettingsState(
            isLoading = true, // Will be managed in MainActivity
            settingsLoaded = true,
            themeMode = themeMode,
            accentColor = accentColor,
            notificationsEnabled = notificationsEnabled
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = SettingsState(isLoading = true, settingsLoaded = false)
    )

    // Individual flows for backward compatibility if needed elsewhere
    val themeMode = repo.themeMode.stateIn(viewModelScope, SharingStarted.Eagerly, ThemeMode.SYSTEM)
    val accentColor = repo.accentColor.stateIn(viewModelScope, SharingStarted.Eagerly, Color(0xFF0733F5))
    val notificationsEnabled = repo.notificationsEnabled.stateIn(viewModelScope, SharingStarted.Eagerly, true)



    fun updateThemeMode(mode: ThemeMode) {
        viewModelScope.launch { repo.setThemeMode(mode) }
    }

    fun updateAccentColor(color: Color) {
        viewModelScope.launch { repo.setAccentColor(color) }
    }

    // Add this method to handle notification changes and update scheduled reminders
    fun updateNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            try {
                repo.setNotificationsEnabled(enabled)

                // If notifications are being disabled, you might want to cancel all scheduled reminders
                // You'll need to inject your reminder scheduler or repository to do this
                // if (!enabled) {
                //     reminderScheduler.cancelAllReminders()
                // }

                Log.d("SettingsViewModel", "Notifications enabled updated to: $enabled")
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "Error updating notifications setting", e)
            }
        }
    }
}