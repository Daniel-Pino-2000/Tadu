package com.example.todoapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.data.SettingsRepository
import com.example.todoapp.ui.settings.ThemeMode
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

    fun updateNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch { repo.setNotificationsEnabled(enabled) }
    }

    fun updateClearHistoryEnabled(enabled: Boolean) {
        viewModelScope.launch { repo.setClearHistoryEnabled(enabled) }
    }
}
