package com.johnykvsky.jktimer.timer

import com.johnykvsky.jktimer.model.StepType
import com.johnykvsky.jktimer.model.TimerConfig
import com.johnykvsky.jktimer.model.TimerStep
import com.johnykvsky.jktimer.model.TrainingPlan

object TimerSequence {
    fun build(plan: TrainingPlan, prepSeconds: Int = 3): List<TimerTick> {
        return when (plan) {
            is TrainingPlan.Simple -> buildSimple(plan.config, prepSeconds)
            is TrainingPlan.Advanced -> buildAdvanced(plan.steps, prepSeconds)
        }
    }

    fun build(config: TimerConfig, prepSeconds: Int = 3): List<TimerTick> =
        buildSimple(config, prepSeconds)

    private fun buildSimple(config: TimerConfig, prepSeconds: Int): List<TimerTick> {
        require(config.isValid()) { "TimerConfig must have positive workout/rest/repeats (1..9999)." }
        require(prepSeconds >= 0) { "prepSeconds cannot be negative." }

        val ticks = mutableListOf<TimerTick>()
        val workoutTotal = config.totalDurationSeconds()

        if (prepSeconds > 0) {
            val nextLabel = "Workout (${config.workoutSeconds}s)"
            for (second in prepSeconds downTo 1) {
                val events = if (second in 1..3) {
                    listOf(TimerSoundEvent.Countdown)
                } else {
                    emptyList()
                }
                ticks += TimerTick(
                    phase = TimerPhase.Starting,
                    remainingSeconds = second,
                    phaseDurationSeconds = prepSeconds,
                    completedWorkouts = 0,
                    totalWorkouts = config.repeats,
                    totalRemainingSeconds = second + workoutTotal,
                    workoutColorIndex = 0,
                    nextStepLabel = nextLabel,
                    soundEvents = events
                )
            }
        }

        repeat(config.repeats) { index ->
            val completedBefore = index
            val colorIndex = if (config.restSeconds > 0) 0 else index % 2
            val remainingRoundsAfterThis = config.repeats - index - 1
            val futureWorkSeconds = remainingRoundsAfterThis * config.workoutSeconds
            val futureRestSeconds = remainingRoundsAfterThis * config.restSeconds + (if (index < config.repeats - 1) config.restSeconds else 0)
            val nextLabelFromWorkout = if (index < config.repeats - 1) "Rest (${config.restSeconds}s)" else "Training Finish"

            for (remaining in config.workoutSeconds downTo 1) {
                val events = buildList {
                    if (remaining == 10) add(TimerSoundEvent.TenSecondWarning)
                    if (remaining in 1..3) add(TimerSoundEvent.Countdown)
                }
                ticks += TimerTick(
                    phase = TimerPhase.Workout,
                    remainingSeconds = remaining,
                    phaseDurationSeconds = config.workoutSeconds,
                    completedWorkouts = completedBefore,
                    totalWorkouts = config.repeats,
                    totalRemainingSeconds = remaining + futureWorkSeconds + futureRestSeconds,
                    workoutColorIndex = colorIndex,
                    nextStepLabel = nextLabelFromWorkout,
                    soundEvents = events
                )
            }

            val completedAfter = index + 1
            val futureRoundsAfterWorkout = config.repeats - completedAfter
            val futureTimeAfterWorkout = (futureRoundsAfterWorkout * config.workoutSeconds) +
                ((futureRoundsAfterWorkout - 1).coerceAtLeast(0) * config.restSeconds)

            ticks += TimerTick(
                phase = TimerPhase.Workout,
                remainingSeconds = 0,
                phaseDurationSeconds = config.workoutSeconds,
                completedWorkouts = completedAfter,
                totalWorkouts = config.repeats,
                totalRemainingSeconds = futureTimeAfterWorkout + (if (completedAfter < config.repeats) config.restSeconds else 0),
                workoutColorIndex = colorIndex,
                nextStepLabel = nextLabelFromWorkout,
                soundEvents = if (completedAfter == config.repeats) {
                    listOf(TimerSoundEvent.TrainingFinish)
                } else {
                    listOf(TimerSoundEvent.WorkoutEnd)
                }
            )

            if (completedAfter < config.repeats && config.restSeconds > 0) {
                val futureWorkAfterRest = (config.repeats - completedAfter) * config.workoutSeconds
                val futureRestAfterRest = ((config.repeats - completedAfter - 1).coerceAtLeast(0)) * config.restSeconds
                val nextLabelFromRest = "Workout (${config.workoutSeconds}s)"
                for (remaining in config.restSeconds downTo 1) {
                    val events = if (remaining in 1..3) {
                        listOf(TimerSoundEvent.Countdown)
                    } else {
                        emptyList()
                    }
                    ticks += TimerTick(
                        phase = TimerPhase.Rest,
                        remainingSeconds = remaining,
                        phaseDurationSeconds = config.restSeconds,
                        completedWorkouts = completedAfter,
                        totalWorkouts = config.repeats,
                        totalRemainingSeconds = remaining + futureWorkAfterRest + futureRestAfterRest,
                        workoutColorIndex = 0,
                        nextStepLabel = nextLabelFromRest,
                        soundEvents = events
                    )
                }
            }
        }

        ticks += TimerTick(
            phase = TimerPhase.Complete,
            remainingSeconds = 0,
            phaseDurationSeconds = 0,
            completedWorkouts = config.repeats,
            totalWorkouts = config.repeats,
            totalRemainingSeconds = 0,
            workoutColorIndex = 0
        )

        return ticks
    }

    private fun buildAdvanced(steps: List<TimerStep>, prepSeconds: Int): List<TimerTick> {
        require(steps.isNotEmpty() && steps.all { it.isValid() }) {
            "Advanced plan must contain at least one valid step."
        }
        require(prepSeconds >= 0) { "prepSeconds cannot be negative." }

        val ticks = mutableListOf<TimerTick>()
        val totalPlanDuration = steps.sumOf { it.durationSeconds }
        val totalWorkouts = steps.count { it.type == StepType.Workout }

        if (prepSeconds > 0) {
            val first = steps.first()
            val nextLabel = "${first.type.name} (${first.durationSeconds}s)${if (first.name.isNotBlank()) " • " + first.name else ""}"
            for (second in prepSeconds downTo 1) {
                val events = if (second in 1..3) {
                    listOf(TimerSoundEvent.Countdown)
                } else {
                    emptyList()
                }
                ticks += TimerTick(
                    phase = TimerPhase.Starting,
                    remainingSeconds = second,
                    phaseDurationSeconds = prepSeconds,
                    completedWorkouts = 0,
                    totalWorkouts = totalWorkouts,
                    totalRemainingSeconds = second + totalPlanDuration,
                    workoutColorIndex = 0,
                    nextStepLabel = nextLabel,
                    soundEvents = events
                )
            }
        }

        var completedWorkouts = 0
        var currentWorkoutColorIndex = 0

        for (stepIndex in steps.indices) {
            val step = steps[stepIndex]
            val isLastStep = stepIndex == steps.lastIndex
            val futureDurationAfterThisStep = steps.drop(stepIndex + 1).sumOf { it.durationSeconds }
            val nextStep = if (stepIndex + 1 < steps.size) steps[stepIndex + 1] else null
            val nextStepLabel = if (nextStep != null) {
                "${nextStep.type.name} (${nextStep.durationSeconds}s)${if (nextStep.name.isNotBlank()) " • " + nextStep.name else ""}"
            } else {
                "Training Finish"
            }

            when (step.type) {
                StepType.Workout -> {
                    val colorIndex = currentWorkoutColorIndex % 2

                    for (remaining in step.durationSeconds downTo 1) {
                        val events = buildList {
                            if (remaining == 10) add(TimerSoundEvent.TenSecondWarning)
                            if (remaining in 1..3) add(TimerSoundEvent.Countdown)
                        }
                        ticks += TimerTick(
                            phase = TimerPhase.Workout,
                            remainingSeconds = remaining,
                            phaseDurationSeconds = step.durationSeconds,
                            completedWorkouts = completedWorkouts,
                            totalWorkouts = totalWorkouts,
                            totalRemainingSeconds = remaining + futureDurationAfterThisStep,
                            workoutColorIndex = colorIndex,
                            stepLabel = step.name,
                            nextStepLabel = nextStepLabel,
                            soundEvents = events
                        )
                    }

                    completedWorkouts++
                    currentWorkoutColorIndex++

                    ticks += TimerTick(
                        phase = TimerPhase.Workout,
                        remainingSeconds = 0,
                        phaseDurationSeconds = step.durationSeconds,
                        completedWorkouts = completedWorkouts,
                        totalWorkouts = totalWorkouts,
                        totalRemainingSeconds = futureDurationAfterThisStep,
                        workoutColorIndex = colorIndex,
                        stepLabel = step.name,
                        nextStepLabel = nextStepLabel,
                        soundEvents = if (isLastStep) {
                            listOf(TimerSoundEvent.TrainingFinish)
                        } else {
                            listOf(TimerSoundEvent.WorkoutEnd)
                        }
                    )
                }

                StepType.Rest -> {
                    currentWorkoutColorIndex = 0

                    for (remaining in step.durationSeconds downTo 1) {
                        val events = if (remaining in 1..3) {
                            listOf(TimerSoundEvent.Countdown)
                        } else {
                            emptyList()
                        }
                        ticks += TimerTick(
                            phase = TimerPhase.Rest,
                            remainingSeconds = remaining,
                            phaseDurationSeconds = step.durationSeconds,
                            completedWorkouts = completedWorkouts,
                            totalWorkouts = totalWorkouts,
                            totalRemainingSeconds = remaining + futureDurationAfterThisStep,
                            workoutColorIndex = 0,
                            stepLabel = step.name,
                            nextStepLabel = nextStepLabel,
                            soundEvents = events
                        )
                    }

                    if (isLastStep) {
                        ticks += TimerTick(
                            phase = TimerPhase.Rest,
                            remainingSeconds = 0,
                            phaseDurationSeconds = step.durationSeconds,
                            completedWorkouts = completedWorkouts,
                            totalWorkouts = totalWorkouts,
                            totalRemainingSeconds = 0,
                            workoutColorIndex = 0,
                            stepLabel = step.name,
                            nextStepLabel = nextStepLabel,
                            soundEvents = listOf(TimerSoundEvent.TrainingFinish)
                        )
                    }
                }
            }
        }

        ticks += TimerTick(
            phase = TimerPhase.Complete,
            remainingSeconds = 0,
            phaseDurationSeconds = 0,
            completedWorkouts = totalWorkouts,
            totalWorkouts = totalWorkouts,
            totalRemainingSeconds = 0,
            workoutColorIndex = 0
        )

        return ticks
    }
}
