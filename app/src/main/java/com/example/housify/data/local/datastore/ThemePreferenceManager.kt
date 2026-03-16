package com.example.housify.data.local.datastore

import com.example.housify.ui.theme.AppTheme
import kotlinx.coroutines.flow.map
import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "theme_settings")

object ThemeKeys {
    val THEME = stringPreferencesKey("app_theme")
}


@Singleton
class ThemePreferenceManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    val themeFlow = context.dataStore.data.map { prefs ->
        when (prefs[ThemeKeys.THEME]) {
            "LIGHT" -> AppTheme.LIGHT
            "DARK" -> AppTheme.DARK
            else -> AppTheme.SYSTEM
        }
    }

    suspend fun saveTheme(theme: AppTheme) {
        context.dataStore.edit { prefs ->
            prefs[ThemeKeys.THEME] = theme.name
        }
    }
}