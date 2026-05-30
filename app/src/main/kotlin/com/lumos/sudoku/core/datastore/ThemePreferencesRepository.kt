package com.lumos.sudoku.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_prefs")

class ThemePreferencesRepository(private val context: Context) {

    private val isDarkThemeKey = booleanPreferencesKey("is_dark_theme")

    val isDarkThemeFlow: Flow<Boolean?> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { preferences -> preferences[isDarkThemeKey] }

    suspend fun saveThemePreference(isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[isDarkThemeKey] = isDark
        }
    }
}
