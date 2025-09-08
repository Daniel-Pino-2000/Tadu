package com.myapp.tadu.settings

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.myapp.tadu.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extension property to create DataStore
val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * Repository for managing app settings using DataStore
 */
class SettingsRepository(private val dataStore: DataStore<Preferences>) {

    companion object {
        private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        private val ACCENT_COLOR_KEY = intPreferencesKey("accent_color")
        private val NOTIFICATIONS_ENABLED_KEY = booleanPreferencesKey("notifications_enabled")
        private val CLEAR_HISTORY_ENABLED_KEY = booleanPreferencesKey("clear_history_enabled")
    }

    /**
     * Flow of theme mode preference
     */
    val themeMode: Flow<ThemeMode> = dataStore.data.map { preferences ->
        val themeModeString = preferences[THEME_MODE_KEY] ?: ThemeMode.SYSTEM.name
        try {
            ThemeMode.valueOf(themeModeString)
        } catch (e: IllegalArgumentException) {
            ThemeMode.SYSTEM
        }
    }

    /**
     * Flow of accent color preference
     */
    val accentColor: Flow<Color> = dataStore.data.map { preferences ->
        val colorInt = preferences[ACCENT_COLOR_KEY] ?: Color(0xFF0733F5).toArgb()
        Color(colorInt)
    }

    /**
     * Flow of notifications enabled preference
     */
    val notificationsEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[NOTIFICATIONS_ENABLED_KEY] ?: true
    }

    /**
     * Flow of clear history enabled preference
     */
    val clearHistoryEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[CLEAR_HISTORY_ENABLED_KEY] ?: false
    }

    /**
     * Set theme mode preference
     */
    suspend fun setThemeMode(themeMode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = themeMode.name
        }
    }

    /**
     * Set accent color preference
     */
    suspend fun setAccentColor(color: Color) {
        dataStore.edit { preferences ->
            preferences[ACCENT_COLOR_KEY] = color.toArgb()
        }
    }

    /**
     * Set notifications enabled preference
     */
    suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED_KEY] = enabled
        }
    }

    /**
     * Set clear history enabled preference
     */
    suspend fun setClearHistoryEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[CLEAR_HISTORY_ENABLED_KEY] = enabled
        }
    }
}

/**
 * Extension function to create SettingsRepository from Context
 */
fun Context.createSettingsRepository(): SettingsRepository {
    return SettingsRepository(this.settingsDataStore)
}