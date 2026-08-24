package com.johnykvsky.jktimer.storage

import android.content.Context
import androidx.core.content.edit
import com.johnykvsky.jktimer.model.AppLanguage
import com.johnykvsky.jktimer.model.AppSettings
import com.johnykvsky.jktimer.ui.theme.ThemeMode

class AppSettingsRepository(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val legacyThemePreferences = context.getSharedPreferences("theme_preferences", Context.MODE_PRIVATE)

    fun load(): AppSettings {
        val themeModeStr = preferences.getString(KEY_THEME_MODE, null)
            ?: legacyThemePreferences.getString("theme_mode", null)

        val themeMode = themeModeStr
            ?.let { value -> ThemeMode.entries.firstOrNull { it.name == value } }
            ?: ThemeMode.System

        val languageStr = preferences.getString(KEY_LANGUAGE, null)
        val language = languageStr
            ?.let { value -> AppLanguage.entries.firstOrNull { it.name == value } }
            ?: AppLanguage.System

        val hapticEnabled = preferences.getBoolean(KEY_HAPTIC_FEEDBACK, false)
        val prepSeconds = preferences.getInt(KEY_PREP_SECONDS, AppSettings.DEFAULT_PREP_SECONDS)
        val timeIntervalSeconds = preferences.getInt(KEY_TIME_INTERVAL_SECONDS, AppSettings.DEFAULT_TIME_INTERVAL_SECONDS)
            .coerceIn(AppSettings.MIN_TIME_INTERVAL_SECONDS, AppSettings.MAX_TIME_INTERVAL_SECONDS)
        val soundEnabled = preferences.getBoolean(KEY_SOUND_ENABLED, true)
        val soundVolume = preferences.getFloat(KEY_SOUND_VOLUME, 1.0f)
        val showTotalRemainingTime = preferences.getBoolean(KEY_SHOW_TOTAL_REMAINING_TIME, true)
        val halfWorkoutWarningEnabled = preferences.getBoolean(KEY_HALF_WORKOUT_WARNING, true)
        val halfTimeRoundingModeStr = preferences.getString(KEY_HALF_TIME_ROUNDING, null)
        val halfTimeRoundingMode = halfTimeRoundingModeStr
            ?.let { value -> com.johnykvsky.jktimer.model.HalfTimeRoundingMode.entries.firstOrNull { it.name == value } }
            ?: com.johnykvsky.jktimer.model.HalfTimeRoundingMode.Down
        val countdownSoundsEnabled = preferences.getBoolean(KEY_COUNTDOWN_SOUNDS, true)

        return AppSettings(
            themeMode = themeMode,
            language = language,
            hapticFeedbackEnabled = hapticEnabled,
            prepSeconds = prepSeconds,
            timeIntervalSeconds = timeIntervalSeconds,
            soundEnabled = soundEnabled,
            soundVolume = soundVolume,
            showTotalRemainingTime = showTotalRemainingTime,
            halfWorkoutWarningEnabled = halfWorkoutWarningEnabled,
            halfTimeRoundingMode = halfTimeRoundingMode,
            countdownSoundsEnabled = countdownSoundsEnabled
        )
    }

    fun save(settings: AppSettings) {
        preferences.edit {
            putString(KEY_THEME_MODE, settings.themeMode.name)
            putString(KEY_LANGUAGE, settings.language.name)
            putBoolean(KEY_HAPTIC_FEEDBACK, settings.hapticFeedbackEnabled)
            putInt(KEY_PREP_SECONDS, settings.prepSeconds)
            putInt(KEY_TIME_INTERVAL_SECONDS, settings.timeIntervalSeconds)
            putBoolean(KEY_SOUND_ENABLED, settings.soundEnabled)
            putFloat(KEY_SOUND_VOLUME, settings.soundVolume)
            putBoolean(KEY_SHOW_TOTAL_REMAINING_TIME, settings.showTotalRemainingTime)
            putBoolean(KEY_HALF_WORKOUT_WARNING, settings.halfWorkoutWarningEnabled)
            putString(KEY_HALF_TIME_ROUNDING, settings.halfTimeRoundingMode.name)
            putBoolean(KEY_COUNTDOWN_SOUNDS, settings.countdownSoundsEnabled)
        }
    }

    private companion object {
        const val PREFERENCES_NAME = "app_settings"
        const val KEY_THEME_MODE = "theme_mode"
        const val KEY_LANGUAGE = "language"
        const val KEY_HAPTIC_FEEDBACK = "haptic_feedback"
        const val KEY_PREP_SECONDS = "prep_seconds"
        const val KEY_TIME_INTERVAL_SECONDS = "time_interval_seconds"
        const val KEY_SOUND_ENABLED = "sound_enabled"
        const val KEY_SOUND_VOLUME = "sound_volume"
        const val KEY_SHOW_TOTAL_REMAINING_TIME = "show_total_remaining_time"
        const val KEY_HALF_WORKOUT_WARNING = "half_workout_warning"
        const val KEY_HALF_TIME_ROUNDING = "half_time_rounding"
        const val KEY_COUNTDOWN_SOUNDS = "countdown_sounds"
    }
}
