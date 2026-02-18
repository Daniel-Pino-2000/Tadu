package com.myapp.tadu.settings

import android.content.Context
import androidx.compose.ui.graphics.Color
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

val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val dataStore: DataStore<Preferences>) {

    companion object {
        private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        // Store palette INDEX (0-7) instead of a raw color int
        private val ACCENT_COLOR_INDEX_KEY = intPreferencesKey("accent_color_index")
        private val NOTIFICATIONS_ENABLED_KEY = booleanPreferencesKey("notifications_enabled")
    }

    val themeMode: Flow<ThemeMode> = dataStore.data.map { preferences ->
        val themeModeString = preferences[THEME_MODE_KEY] ?: ThemeMode.SYSTEM.name
        try {
            ThemeMode.valueOf(themeModeString)
        } catch (e: IllegalArgumentException) {
            ThemeMode.SYSTEM
        }
    }

    /** Emits the stored palette index (default 0). */
    val accentColorIndex: Flow<Int> = dataStore.data.map { preferences ->
        preferences[ACCENT_COLOR_INDEX_KEY] ?: 0
    }

    val notificationsEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[NOTIFICATIONS_ENABLED_KEY] ?: true
    }

    suspend fun setThemeMode(themeMode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = themeMode.name
        }
    }

    /** Save only the palette index, never a raw color. */
    suspend fun setAccentColorIndex(index: Int) {
        dataStore.edit { preferences ->
            preferences[ACCENT_COLOR_INDEX_KEY] = index
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED_KEY] = enabled
        }
    }
}

fun Context.createSettingsRepository(): SettingsRepository {
    return SettingsRepository(this.settingsDataStore)
}