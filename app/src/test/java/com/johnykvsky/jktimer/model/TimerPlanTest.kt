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

    @Test
    fun advancedPlanShareableSummaryGeneratesFormattedNumberedListWithTotalTime() {
        val steps = listOf(
            TimerStep(type = StepType.Workout, durationSeconds = 30, name = "Pushups"),
            TimerStep(type = StepType.Rest, durationSeconds = 15, name = ""),
            TimerStep(type = StepType.Workout, durationSeconds = 45, name = "Plank")
        )
        val plan = TrainingPlan.Advanced(steps)
        val summary = plan.generateShareableSummary(
            title = "Upper Body Routine",
            totalTimeLabel = "Total time",
            workoutLabel = "Workout",
            restLabel = "Rest",
            setsLabel = "Sets"
        )

        val expected = """
            Upper Body Routine
            Total time: 1m 30s
            1. Workout - Pushups (30s)
            2. Rest (15s)
            3. Workout - Plank (45s)
        """.trimIndent()

        assertEquals(expected, summary)
    }

    @Test
    fun simplePlanShareableSummaryGeneratesFormattedSummaryWithTotalTime() {
        val config = TimerConfig(workoutSeconds = 20, restSeconds = 10, repeats = 8)
        val plan = TrainingPlan.Simple(config)
        val summary = plan.generateShareableSummary(
            title = "Full Body Tabata",
            totalTimeLabel = "Total time",
            workoutLabel = "Workout",
            restLabel = "Rest",
            setsLabel = "Sets"
        )

        val expected = """
            Full Body Tabata
            Total time: 3m 50s
            Workout: 20s
            Rest: 10s
            Sets: 8
        """.trimIndent()

        assertEquals(expected, summary)
    }

    @Test
    fun advancedPlanShareableSummaryWithCustomLabels() {
        val steps = listOf(
            TimerStep(type = StepType.Workout, durationSeconds = 20, name = "Pompki"),
            TimerStep(type = StepType.Rest, durationSeconds = 10, name = "")
        )
        val plan = TrainingPlan.Advanced(steps)
        val summary = plan.generateShareableSummary(
            title = "Mój Trening",
            totalTimeLabel = "Całkowity czas",
            workoutLabel = "Ćwiczenie",
            restLabel = "Odpoczynek",
            setsLabel = "Serie"
        )

        val expected = """
            Mój Trening
            Całkowity czas: 30s
            1. Ćwiczenie - Pompki (20s)
            2. Odpoczynek (10s)
        """.trimIndent()

        assertEquals(expected, summary)
    }
}
