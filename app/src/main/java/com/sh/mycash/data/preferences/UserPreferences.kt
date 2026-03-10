package com.sh.mycash.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

enum class AppLanguage(val code: String) {
    UKRAINIAN("uk"),
    RUSSIAN("ru"),
    ENGLISH("en")
}

object PreferenceKeys {
    val LANGUAGE = stringPreferencesKey("language")
    val DARK_THEME = booleanPreferencesKey("dark_theme")
}

class UserPreferences(private val context: Context) {

    val language: Flow<AppLanguage> = context.dataStore.data.map { preferences ->
        preferences[PreferenceKeys.LANGUAGE]?.let { code ->
            AppLanguage.entries.find { it.code == code } ?: AppLanguage.UKRAINIAN
        } ?: AppLanguage.UKRAINIAN
    }

    val darkTheme: Flow<Boolean?> = context.dataStore.data.map { preferences ->
        preferences[PreferenceKeys.DARK_THEME]
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.LANGUAGE] = language.code
        }
    }

    suspend fun setDarkTheme(enabled: Boolean?) {
        context.dataStore.edit { preferences ->
            if (enabled != null) {
                preferences[PreferenceKeys.DARK_THEME] = enabled
            } else {
                preferences.remove(PreferenceKeys.DARK_THEME)
            }
        }
    }
}
