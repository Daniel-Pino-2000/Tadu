package com.example.todolist.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color

class SettingsViewModel(private val repo: SettingsRepository) : ViewModel() {

    val themeMode = repo.themeMode.stateIn(viewModelScope, SharingStarted.Lazily, ThemeMode.SYSTEM)
    val accentColor = repo.accentColor.stateIn(viewModelScope, SharingStarted.Lazily, Color(0xFF1976D2))
    val notificationsEnabled = repo.notificationsEnabled.stateIn(viewModelScope, SharingStarted.Lazily, true)
    val clearHistoryEnabled = repo.clearHistoryEnabled.stateIn(viewModelScope, SharingStarted.Lazily, false)

    fun updateThemeMode(mode: ThemeMode) {
        viewModelScope.launch { repo.setThemeMode(mode) }
    }

    fun updateAccentColor(color: Color) {
        viewModelScope.launch { repo.setAccentColor(color) }
    }

    fun updateClearHistoryEnabled(enabled: Boolean) {
        viewModelScope.launch { repo.setClearHistoryEnabled(enabled) }
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
