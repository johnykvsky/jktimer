package com.johnykvsky.jktimer.model

import java.util.UUID

enum class StepType {
    Workout,
    Rest
}

data class TimerStep(
    val id: String = UUID.randomUUID().toString(),
    val type: StepType = StepType.Workout,
    val durationSeconds: Int = 30,
    val name: String = ""
) {
    fun isValid(): Boolean = durationSeconds in 1..TimerConfig.MAX_WORKOUT_SECONDS
}

sealed interface TrainingPlan {
    data class Simple(val config: TimerConfig) : TrainingPlan
    data class Advanced(val steps: List<TimerStep>) : TrainingPlan

    fun isValid(): Boolean = when (this) {
        is Simple -> config.isValid()
        is Advanced -> steps.isNotEmpty() && steps.size <= MAX_ADVANCED_STEPS && steps.all { it.isValid() }
    }

    fun totalDurationSeconds(): Int = when (this) {
        is Simple -> config.totalDurationSeconds()
        is Advanced -> steps.sumOf { it.durationSeconds }
    }

    fun formattedDuration(): String = TimerConfig.formatDuration(totalDurationSeconds())

    fun totalWorkoutIntervals(): Int = when (this) {
        is Simple -> config.repeats
        is Advanced -> steps.count { it.type == StepType.Workout }
    }

    fun summaryText(): String = when (this) {
        is Simple -> "${config.workoutSeconds}s workout  •  ${config.restSeconds}s rest  •  ${config.repeats} sets"
        is Advanced -> {
            val workouts = steps.count { it.type == StepType.Workout }
            val rests = steps.count { it.type == StepType.Rest }
            "${steps.size} steps  •  $workouts work, $rests rest"
        }
    }

    fun generateShareableSummary(
        title: String,
        totalTimeLabel: String = "Total time",
        workoutLabel: String = "Workout",
        restLabel: String = "Rest",
        setsLabel: String = "Sets"
    ): String = when (this) {
        is Simple -> {
            val name = title.ifBlank { "Workout Timer" }
            val duration = formattedDuration()
            "$name\n$totalTimeLabel: $duration\n$workoutLabel: ${config.workoutSeconds}s\n$restLabel: ${config.restSeconds}s\n$setsLabel: ${config.repeats}"
        }
        is Advanced -> {
            val sb = StringBuilder()
            sb.append(title.ifBlank { "Advanced Training" }).append("\n")
            sb.append("$totalTimeLabel: ${formattedDuration()}\n")
            steps.forEachIndexed { index, step ->
                val typeStr = if (step.type == StepType.Workout) workoutLabel else restLabel
                val line = if (step.name.isNotBlank()) {
                    "${index + 1}. $typeStr - ${step.name} (${step.durationSeconds}s)"
                } else {
                    "${index + 1}. $typeStr (${step.durationSeconds}s)"
                }
                sb.append(line).append("\n")
            }
            sb.toString().trimEnd()
        }
    }

    companion object {
        const val MAX_ADVANCED_STEPS = 99
    }
}
