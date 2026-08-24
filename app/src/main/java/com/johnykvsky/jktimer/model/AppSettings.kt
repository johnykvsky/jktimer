package com.johnykvsky.jktimer.model

import com.johnykvsky.jktimer.ui.theme.ThemeMode

enum class HalfTimeRoundingMode {
    Down,
    Up
}

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.System,
    val language: AppLanguage = AppLanguage.System,
    val hapticFeedbackEnabled: Boolean = false,
    val prepSeconds: Int = DEFAULT_PREP_SECONDS,
    val timeIntervalSeconds: Int = DEFAULT_TIME_INTERVAL_SECONDS,
    val soundEnabled: Boolean = true,
    val soundVolume: Float = 1.0f,
    val showTotalRemainingTime: Boolean = true,
    val halfWorkoutWarningEnabled: Boolean = true,
    val halfTimeRoundingMode: HalfTimeRoundingMode = HalfTimeRoundingMode.Down,
    val countdownSoundsEnabled: Boolean = true
) {
    companion object {
        const val DEFAULT_PREP_SECONDS = 3
        val PREP_TIME_OPTIONS = listOf(0, 3, 5, 10, 15)
        const val DEFAULT_TIME_INTERVAL_SECONDS = 5
        const val MIN_TIME_INTERVAL_SECONDS = 1
        const val MAX_TIME_INTERVAL_SECONDS = 99
    }
}
