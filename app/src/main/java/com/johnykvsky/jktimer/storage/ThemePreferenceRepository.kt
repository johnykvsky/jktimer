package com.johnykvsky.jktimer.storage

import android.content.Context
import com.johnykvsky.jktimer.ui.theme.ThemeMode

class ThemePreferenceRepository(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun load(): ThemeMode {
        return preferences.getString(THEME_MODE_KEY, null)
            ?.let { value -> ThemeMode.entries.firstOrNull { it.name == value } }
            ?: ThemeMode.System
    }

    fun save(mode: ThemeMode) {
        preferences.edit().putString(THEME_MODE_KEY, mode.name).apply()
    }

    private companion object {
        const val PREFERENCES_NAME = "theme_preferences"
        const val THEME_MODE_KEY = "theme_mode"
    }
}
