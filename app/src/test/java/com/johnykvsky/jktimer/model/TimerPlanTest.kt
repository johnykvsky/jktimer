package com.johnykvsky.jktimer.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TimerPlanTest {

    @Test
    fun simplePlanDurationAndSummariesAreAccurate() {
        val config = TimerConfig(workoutSeconds = 40, restSeconds = 20, repeats = 3)
        val plan = TrainingPlan.Simple(config)

        assertTrue(plan.isValid())
        // (3 * 40) + (2 * 20) = 120 + 40 = 160s = 2m 40s
        assertEquals(160, plan.totalDurationSeconds())
        assertEquals("2m 40s", plan.formattedDuration())
        assertEquals(3, plan.totalWorkoutIntervals())
        assertEquals("40s workout  •  20s rest  •  3 sets", plan.summaryText())
    }

    @Test
    fun advancedPlanDurationAndSummariesAreAccurate() {
        val steps = listOf(
            TimerStep(type = StepType.Workout, durationSeconds = 15, name = "Warmup"),
            TimerStep(type = StepType.Workout, durationSeconds = 20, name = "Pushups"),
            TimerStep(type = StepType.Rest, durationSeconds = 15, name = "Rest"),
            TimerStep(type = StepType.Workout, durationSeconds = 60, name = "Plank"),
            TimerStep(type = StepType.Rest, durationSeconds = 30, name = "Rest")
        )
        val plan = TrainingPlan.Advanced(steps)

        assertTrue(plan.isValid())
        // 15 + 20 + 15 + 60 + 30 = 140s = 2m 20s
        assertEquals(140, plan.totalDurationSeconds())
        assertEquals("2m 20s", plan.formattedDuration())
        assertEquals(3, plan.totalWorkoutIntervals())
        assertEquals("5 steps  •  3 work, 2 rest", plan.summaryText())
    }

    @Test
    fun emptyAdvancedPlanIsInvalid() {
        val plan = TrainingPlan.Advanced(emptyList())
        assertFalse(plan.isValid())
    }

    @Test
    fun stepWithZeroDurationIsInvalid() {
        val step = TimerStep(type = StepType.Workout, durationSeconds = 0)
        assertFalse(step.isValid())
    }

    @Test
    fun maxNinetyNineStepsIsValidAndHundredIsInvalid() {
        val ninetyNineSteps = List(99) { TimerStep(type = StepType.Workout, durationSeconds = 10) }
        assertTrue(TrainingPlan.Advanced(ninetyNineSteps).isValid())

        val hundredSteps = List(100) { TimerStep(type = StepType.Workout, durationSeconds = 10) }
        assertFalse(TrainingPlan.Advanced(hundredSteps).isValid())
    }
}
