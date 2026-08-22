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
}
