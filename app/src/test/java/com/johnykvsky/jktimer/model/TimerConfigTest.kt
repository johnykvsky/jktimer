package com.johnykvsky.jktimer.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TimerConfigTest {
    @Test
    fun acceptsConfiguredBoundaries() {
        assertTrue(
            TimerConfig(
                workoutSeconds = 1,
                restSeconds = 1,
                repeats = 1
            ).isValid()
        )
        assertTrue(
            TimerConfig(
                workoutSeconds = TimerConfig.MAX_WORKOUT_SECONDS,
                restSeconds = TimerConfig.MAX_REST_SECONDS,
                repeats = TimerConfig.MAX_REPEATS
            ).isValid()
        )
    }

    @Test
    fun rejectsValuesOutsideConfiguredBoundaries() {
        assertFalse(TimerConfig(0, 1, 1).isValid())
        assertFalse(TimerConfig(1, 0, 1).isValid()) // 0 rest is rejected
        assertFalse(TimerConfig(1, -1, 1).isValid())
        assertFalse(TimerConfig(TimerConfig.MAX_WORKOUT_SECONDS + 1, 1, 1).isValid())
        assertFalse(TimerConfig(1, TimerConfig.MAX_REST_SECONDS + 1, 1).isValid())
        assertFalse(TimerConfig(1, 1, TimerConfig.MAX_REPEATS + 1).isValid())
    }
}
