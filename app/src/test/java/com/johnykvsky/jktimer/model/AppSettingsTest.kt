package com.johnykvsky.jktimer.model

import com.johnykvsky.jktimer.ui.theme.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppSettingsTest {
    @Test
    fun defaultSettingsAreExpected() {
        val settings = AppSettings()
        assertEquals(ThemeMode.System, settings.themeMode)
        assertEquals(AppLanguage.System, settings.language)
        assertFalse(settings.hapticFeedbackEnabled)
        assertEquals(3, settings.prepSeconds)
        assertEquals(5, settings.timeIntervalSeconds)
        assertTrue(settings.soundEnabled)
        assertEquals(1.0f, settings.soundVolume, 0.001f)
        assertTrue(settings.showTotalRemainingTime)
    }

    @Test
    fun appLanguagesHaveExpectedCodes() {
        assertEquals("", AppLanguage.System.code)
        assertEquals("en", AppLanguage.English.code)
        assertEquals("pl", AppLanguage.Polish.code)
    }

    @Test
    fun prepTimeOptionsContainZeroAndPositiveValues() {
        assertTrue(AppSettings.PREP_TIME_OPTIONS.contains(0))
        assertTrue(AppSettings.PREP_TIME_OPTIONS.contains(3))
        assertTrue(AppSettings.PREP_TIME_OPTIONS.contains(5))
        assertTrue(AppSettings.PREP_TIME_OPTIONS.contains(10))
    }

    @Test
    fun timeIntervalConstantsAreExpected() {
        assertEquals(5, AppSettings.DEFAULT_TIME_INTERVAL_SECONDS)
        assertEquals(1, AppSettings.MIN_TIME_INTERVAL_SECONDS)
        assertEquals(99, AppSettings.MAX_TIME_INTERVAL_SECONDS)
    }
}
