package com.johnykvsky.jktimer.model

import com.johnykvsky.jktimer.ui.theme.ThemeMode

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.System,
    val language: AppLanguage = AppLanguage.System,
    val hapticFeedbackEnabled: Boolean = false,
    val prepSeconds: Int = DEFAULT_PREP_SECONDS,
    val soundEnabled: Boolean = true,
    val soundVolume: Float = 1.0f,
    val showTotalRemainingTime: Boolean = true
) {
    companion object {
        const val DEFAULT_PREP_SECONDS = 3
        val PREP_TIME_OPTIONS = listOf(0, 3, 5, 10, 15)
    }
}
