package com.johnykvsky.jktimer.timer

import com.johnykvsky.jktimer.model.TimerConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TimerSequenceTest {
    @Test
    fun finalIntervalEndsWithWorkoutAndNoRest() {
        val ticks = TimerSequence.build(
            TimerConfig(workoutSeconds = 5, restSeconds = 3, repeats = 2)
        )

        val finalWorkTick = ticks[ticks.lastIndex - 1]

        assertEquals(TimerPhase.Workout, finalWorkTick.phase)
        assertEquals(0, finalWorkTick.remainingSeconds)
        assertEquals(2, finalWorkTick.completedWorkouts)
        assertEquals(TimerPhase.Complete, ticks.last().phase)
        assertFalse(ticks.takeLast(2).any { it.phase == TimerPhase.Rest })
    }

    @Test
    fun workoutTenSecondWarningHappensOnlyAtTen() {
        val ticks = TimerSequence.build(
            TimerConfig(workoutSeconds = 12, restSeconds = 1, repeats = 1)
        )

        val warningSeconds = ticks
            .filter { TimerSoundEvent.TenSecondWarning in it.soundEvents }
            .map { it.remainingSeconds }

        assertEquals(listOf(10), warningSeconds)
    }

    @Test
    fun workoutFinalCountdownUsesCountdownSound() {
        val ticks = TimerSequence.build(
            TimerConfig(workoutSeconds = 12, restSeconds = 1, repeats = 1)
        )

        val countdownSeconds = ticks
            .filter { it.phase == TimerPhase.Workout }
            .filter { TimerSoundEvent.Countdown in it.soundEvents }
            .map { it.remainingSeconds }

        assertEquals(listOf(3, 2, 1), countdownSeconds)
    }

    @Test
    fun nonFinalWorkoutEndEmitsWorkoutEndEvent() {
        val ticks = TimerSequence.build(
            TimerConfig(workoutSeconds = 1, restSeconds = 1, repeats = 2)
        )

        val firstWorkoutEndTick = ticks.first {
            it.phase == TimerPhase.Workout &&
                it.remainingSeconds == 0 &&
                it.completedWorkouts == 1
        }

        assertEquals(listOf(TimerSoundEvent.WorkoutEnd), firstWorkoutEndTick.soundEvents)
    }

    @Test
    fun finalWorkoutEndEmitsTrainingFinishEvent() {
        val ticks = TimerSequence.build(
            TimerConfig(workoutSeconds = 1, restSeconds = 1, repeats = 1)
        )

        val finalWorkTick = ticks[ticks.lastIndex - 1]

        assertEquals(listOf(TimerSoundEvent.TrainingFinish), finalWorkTick.soundEvents)
    }

    @Test
    fun restCountdownSoundsAtThreeTwoAndOne() {
        val ticks = TimerSequence.build(
            TimerConfig(workoutSeconds = 1, restSeconds = 5, repeats = 2)
        )

        val restCountdownSeconds = ticks
            .filter { it.phase == TimerPhase.Rest }
            .filter { TimerSoundEvent.Countdown in it.soundEvents }
            .map { it.remainingSeconds }

        assertEquals(listOf(3, 2, 1), restCountdownSeconds)
    }

    @Test
    fun restEndDoesNotEmitWorkoutEndEvent() {
        val ticks = TimerSequence.build(
            TimerConfig(workoutSeconds = 1, restSeconds = 5, repeats = 2)
        )

        val restTicks = ticks.filter { it.phase == TimerPhase.Rest }

        assertTrue(restTicks.all { TimerSoundEvent.WorkoutEnd !in it.soundEvents })
        assertTrue(restTicks.all { TimerSoundEvent.TrainingFinish !in it.soundEvents })
    }

    @Test
    fun startupCountdownSoundsAtThreeTwoAndOneByDefault() {
        val ticks = TimerSequence.build(
            TimerConfig(workoutSeconds = 1, restSeconds = 1, repeats = 1)
        )

        val startupTicks = ticks.filter { it.phase == TimerPhase.Starting }

        assertEquals(listOf(3, 2, 1), startupTicks.map { it.remainingSeconds })
        assertTrue(startupTicks.all { TimerSoundEvent.Countdown in it.soundEvents })
    }

    @Test
    fun startupWithZeroPrepSecondsHasNoStartupTicks() {
        val ticks = TimerSequence.build(
            TimerConfig(workoutSeconds = 1, restSeconds = 1, repeats = 1),
            prepSeconds = 0
        )

        assertFalse(ticks.any { it.phase == TimerPhase.Starting })
        assertEquals(TimerPhase.Workout, ticks.first().phase)
    }

    @Test
    fun startupWithCustomPrepSecondsCountsDownCorrectly() {
        val ticks = TimerSequence.build(
            TimerConfig(workoutSeconds = 1, restSeconds = 1, repeats = 1),
            prepSeconds = 5
        )

        val startupTicks = ticks.filter { it.phase == TimerPhase.Starting }

        assertEquals(listOf(5, 4, 3, 2, 1), startupTicks.map { it.remainingSeconds })
        // Only final 3, 2, 1 should emit countdown sound event
        assertEquals(
            listOf(3, 2, 1),
            startupTicks.filter { TimerSoundEvent.Countdown in it.soundEvents }.map { it.remainingSeconds }
        )
    }

    @Test
    fun advancedPlanConsecutiveWorkoutsAlternateColorsAndEmitCorrectSounds() {
        val steps = listOf(
            com.johnykvsky.jktimer.model.TimerStep(type = com.johnykvsky.jktimer.model.StepType.Workout, durationSeconds = 15, name = "Sprint"),
            com.johnykvsky.jktimer.model.TimerStep(type = com.johnykvsky.jktimer.model.StepType.Workout, durationSeconds = 20, name = "Pushups"),
            com.johnykvsky.jktimer.model.TimerStep(type = com.johnykvsky.jktimer.model.StepType.Rest, durationSeconds = 15, name = "Rest"),
            com.johnykvsky.jktimer.model.TimerStep(type = com.johnykvsky.jktimer.model.StepType.Workout, durationSeconds = 60, name = "Plank"),
            com.johnykvsky.jktimer.model.TimerStep(type = com.johnykvsky.jktimer.model.StepType.Workout, durationSeconds = 30, name = "Burpees")
        )
        val plan = com.johnykvsky.jktimer.model.TrainingPlan.Advanced(steps)

        val ticks = TimerSequence.build(plan, prepSeconds = 0)

        // Step 1 (Sprint): First workout in streak -> Color 0 (Orange)
        val step1Ticks = ticks.filter { it.phase == TimerPhase.Workout && it.stepLabel == "Sprint" }
        assertTrue(step1Ticks.all { it.workoutColorIndex == 0 })

        // Step 2 (Pushups): Follows Sprint immediately without rest -> Color 1 (Blue)
        val step2Ticks = ticks.filter { it.phase == TimerPhase.Workout && it.stepLabel == "Pushups" }
        assertTrue(step2Ticks.all { it.workoutColorIndex == 1 })

        // Step 3 (Rest): Rest phase
        val step3Ticks = ticks.filter { it.phase == TimerPhase.Rest }
        assertTrue(step3Ticks.isNotEmpty())

        // Step 4 (Plank): Follows Rest -> Always resets to Color 0 (Orange)!
        val step4Ticks = ticks.filter { it.phase == TimerPhase.Workout && it.stepLabel == "Plank" }
        assertTrue(step4Ticks.all { it.workoutColorIndex == 0 })

        // Step 5 (Burpees): Follows Plank immediately without rest -> Color 1 (Blue)
        val step5Ticks = ticks.filter { it.phase == TimerPhase.Workout && it.stepLabel == "Burpees" }
        assertTrue(step5Ticks.all { it.workoutColorIndex == 1 })
    }

    @Test
    fun simplePlanWithRestHasAllOrangeWorkouts() {
        val ticks = TimerSequence.build(
            TimerConfig(workoutSeconds = 10, restSeconds = 5, repeats = 3),
            prepSeconds = 0
        )

        val workTicks = ticks.filter { it.phase == TimerPhase.Workout }
        // All workouts should be color 0 (Orange) because rest separates them
        assertTrue(workTicks.all { it.workoutColorIndex == 0 })
    }

    @Test
    fun consecutiveWorkoutsWithoutRestAlternateColors() {
        val steps = listOf(
            com.johnykvsky.jktimer.model.TimerStep(type = com.johnykvsky.jktimer.model.StepType.Workout, durationSeconds = 10, name = "W1"),
            com.johnykvsky.jktimer.model.TimerStep(type = com.johnykvsky.jktimer.model.StepType.Workout, durationSeconds = 10, name = "W2"),
            com.johnykvsky.jktimer.model.TimerStep(type = com.johnykvsky.jktimer.model.StepType.Workout, durationSeconds = 10, name = "W3")
        )
        val plan = com.johnykvsky.jktimer.model.TrainingPlan.Advanced(steps)
        val ticks = TimerSequence.build(plan, prepSeconds = 0)

        val round0Ticks = ticks.filter { it.phase == TimerPhase.Workout && it.stepLabel == "W1" }
        val round1Ticks = ticks.filter { it.phase == TimerPhase.Workout && it.stepLabel == "W2" }
        val round2Ticks = ticks.filter { it.phase == TimerPhase.Workout && it.stepLabel == "W3" }

        assertTrue(round0Ticks.all { it.workoutColorIndex == 0 })
        assertTrue(round1Ticks.all { it.workoutColorIndex == 1 })
        assertTrue(round2Ticks.all { it.workoutColorIndex == 0 })
    }

    @Test
    fun advancedPlanTotalDurationCalculatedCorrectly() {
        val steps = listOf(
            com.johnykvsky.jktimer.model.TimerStep(type = com.johnykvsky.jktimer.model.StepType.Workout, durationSeconds = 10),
            com.johnykvsky.jktimer.model.TimerStep(type = com.johnykvsky.jktimer.model.StepType.Rest, durationSeconds = 5),
            com.johnykvsky.jktimer.model.TimerStep(type = com.johnykvsky.jktimer.model.StepType.Workout, durationSeconds = 20)
        )
        val plan = com.johnykvsky.jktimer.model.TrainingPlan.Advanced(steps)

        val ticks = TimerSequence.build(plan, prepSeconds = 3)
        // First prep tick total remaining = 3s prep + 35s workout = 38s
        assertEquals(38, ticks.first().totalRemainingSeconds)

        val firstWorkTick = ticks.first { it.phase == TimerPhase.Workout }
        // 10s work + 5s rest + 20s work = 35s
        assertEquals(35, firstWorkTick.totalRemainingSeconds)
    }

    @Test
    fun nextStepLabelsAreAccurateForSimpleAndAdvancedPlans() {
        val simpleTicks = TimerSequence.build(
            TimerConfig(workoutSeconds = 30, restSeconds = 15, repeats = 2),
            prepSeconds = 3
        )
        // Starting phase next step is Workout (30s)
        assertEquals("Workout (30s)", simpleTicks.first { it.phase == TimerPhase.Starting }.nextStepLabel)
        // First workout phase next step is Rest (15s)
        assertEquals("Rest (15s)", simpleTicks.first { it.phase == TimerPhase.Workout && it.remainingSeconds > 0 && it.completedWorkouts == 0 }.nextStepLabel)
        // First rest phase next step is Workout (30s)
        assertEquals("Workout (30s)", simpleTicks.first { it.phase == TimerPhase.Rest && it.remainingSeconds > 0 }.nextStepLabel)
        // Last workout phase next step is Training Finish
        assertEquals("Training Finish", simpleTicks.first { it.phase == TimerPhase.Workout && it.remainingSeconds > 0 && it.completedWorkouts == 1 }.nextStepLabel)

        val advancedSteps = listOf(
            com.johnykvsky.jktimer.model.TimerStep(type = com.johnykvsky.jktimer.model.StepType.Workout, durationSeconds = 10, name = "Pushups"),
            com.johnykvsky.jktimer.model.TimerStep(type = com.johnykvsky.jktimer.model.StepType.Rest, durationSeconds = 5, name = "Rest"),
            com.johnykvsky.jktimer.model.TimerStep(type = com.johnykvsky.jktimer.model.StepType.Workout, durationSeconds = 20, name = "Plank")
        )
        val advTicks = TimerSequence.build(com.johnykvsky.jktimer.model.TrainingPlan.Advanced(advancedSteps), prepSeconds = 3)
        assertEquals("Workout (10s) • Pushups", advTicks.first { it.phase == TimerPhase.Starting }.nextStepLabel)
        assertEquals("Rest (5s)", advTicks.first { it.phase == TimerPhase.Workout && it.stepLabel == "Pushups" }.nextStepLabel)
        assertEquals("Workout (20s) • Plank", advTicks.first { it.phase == TimerPhase.Rest }.nextStepLabel)
        assertEquals("Training Finish", advTicks.first { it.phase == TimerPhase.Workout && it.stepLabel == "Plank" }.nextStepLabel)
    }

    @Test
    fun simplePlanTotalRemainingSecondsDecrementsSmoothlyWithoutGapsOrJumps() {
        // 40s workout, 20s rest, 3 repeats -> (3 * 40) + (2 * 20) = 160s (2m 40s)
        val config = TimerConfig(workoutSeconds = 40, restSeconds = 20, repeats = 3)
        val prepSeconds = 3
        val ticks = TimerSequence.build(config, prepSeconds = prepSeconds)

        // Starting prep ticks: 163, 162, 161
        val startingTicks = ticks.filter { it.phase == TimerPhase.Starting }
        assertEquals(listOf(163, 162, 161), startingTicks.map { it.totalRemainingSeconds })

        // First workout tick (remaining = 40s) should start at 160s (2m 40s), NOT 180s (3m 00s)!
        val firstWorkoutTick = ticks.first { it.phase == TimerPhase.Workout }
        assertEquals(40, firstWorkoutTick.remainingSeconds)
        assertEquals(160, firstWorkoutTick.totalRemainingSeconds)

        // Filter all 1-second interval ticks (excluding 0-second boundary transition ticks and Complete)
        val activeSecondTicks = ticks.filter { it.remainingSeconds > 0 && it.phase != TimerPhase.Complete }
        assertEquals(163, activeSecondTicks.size) // 3s prep + 160s workout/rest = 163 seconds total

        for (i in 0 until activeSecondTicks.size) {
            val expectedRemaining = 163 - i
            assertEquals(
                "Mismatch at second index $i (phase: ${activeSecondTicks[i].phase}, remaining: ${activeSecondTicks[i].remainingSeconds})",
                expectedRemaining,
                activeSecondTicks[i].totalRemainingSeconds
            )
        }

        // Final Complete tick
        assertEquals(0, ticks.last().totalRemainingSeconds)
        assertEquals(TimerPhase.Complete, ticks.last().phase)
    }

    @Test
    fun advancedPlanTotalRemainingSecondsDecrementsSmoothlyWithoutGapsOrJumps() {
        val steps = listOf(
            com.johnykvsky.jktimer.model.TimerStep(type = com.johnykvsky.jktimer.model.StepType.Workout, durationSeconds = 15, name = "Warmup"),
            com.johnykvsky.jktimer.model.TimerStep(type = com.johnykvsky.jktimer.model.StepType.Workout, durationSeconds = 20, name = "Pushups"),
            com.johnykvsky.jktimer.model.TimerStep(type = com.johnykvsky.jktimer.model.StepType.Rest, durationSeconds = 15, name = "Rest"),
            com.johnykvsky.jktimer.model.TimerStep(type = com.johnykvsky.jktimer.model.StepType.Workout, durationSeconds = 60, name = "Plank"),
            com.johnykvsky.jktimer.model.TimerStep(type = com.johnykvsky.jktimer.model.StepType.Rest, durationSeconds = 30, name = "Rest")
        )
        // Total = 15 + 20 + 15 + 60 + 30 = 140s
        val plan = com.johnykvsky.jktimer.model.TrainingPlan.Advanced(steps)
        val prepSeconds = 5
        val ticks = TimerSequence.build(plan, prepSeconds = prepSeconds)

        // Starting prep ticks: 145, 144, 143, 142, 141
        val startingTicks = ticks.filter { it.phase == TimerPhase.Starting }
        assertEquals(listOf(145, 144, 143, 142, 141), startingTicks.map { it.totalRemainingSeconds })

        // First step tick
        val firstStepTick = ticks.first { it.phase == TimerPhase.Workout }
        assertEquals(15, firstStepTick.remainingSeconds)
        assertEquals(140, firstStepTick.totalRemainingSeconds)

        // Filter all 1-second interval ticks
        val activeSecondTicks = ticks.filter { it.remainingSeconds > 0 && it.phase != TimerPhase.Complete }
        assertEquals(145, activeSecondTicks.size) // 5s prep + 140s plan = 145 seconds total

        for (i in 0 until activeSecondTicks.size) {
            val expectedRemaining = 145 - i
            assertEquals(
                "Mismatch at second index $i (phase: ${activeSecondTicks[i].phase}, remaining: ${activeSecondTicks[i].remainingSeconds})",
                expectedRemaining,
                activeSecondTicks[i].totalRemainingSeconds
            )
        }

        // Final Complete tick
        assertEquals(0, ticks.last().totalRemainingSeconds)
        assertEquals(TimerPhase.Complete, ticks.last().phase)
    }
}
