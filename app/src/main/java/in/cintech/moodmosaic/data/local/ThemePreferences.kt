package `in`.cintech.moodmosaic.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


// ✅ Theme modes
enum class ThemeMode {
    SYSTEM,  // Follow system
    LIGHT,   // Force light
    DARK     // Force dark
}

class ThemePreferences(private val context: Context) {

    companion object {
        private val THEME_MODE = intPreferencesKey("theme_mode")
    }

    // ✅ Returns ThemeMode (default is SYSTEM)
    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { preferences ->
        when (preferences[THEME_MODE]) {
            1 -> ThemeMode.LIGHT
            2 -> ThemeMode.DARK
            else -> ThemeMode.SYSTEM  // Default to system
        }
    }

    // ✅ Cycle through: SYSTEM → LIGHT → DARK → SYSTEM
    suspend fun cycleTheme() {
        context.dataStore.edit { preferences ->
            val current = preferences[THEME_MODE] ?: 0
            preferences[THEME_MODE] = (current + 1) % 3
        }
    }

    // ✅ Set specific theme
    suspend fun setTheme(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE] = when (mode) {
                ThemeMode.SYSTEM -> 0
                ThemeMode.LIGHT -> 1
                ThemeMode.DARK -> 2
            }
        }
    }

    // ✅ Get current theme mode synchronously (for widget)
    suspend fun getCurrentThemeMode(): ThemeMode {
        var mode = ThemeMode.SYSTEM
        context.dataStore.data.collect { preferences ->
            mode = when (preferences[THEME_MODE]) {
                1 -> ThemeMode.LIGHT
                2 -> ThemeMode.DARK
                else -> ThemeMode.SYSTEM
            }
        }
        return mode
    }
}