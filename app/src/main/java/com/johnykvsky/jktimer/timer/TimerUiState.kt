package com.johnykvsky.jktimer.timer

data class TimerUiState(
    val phase: TimerPhase = TimerPhase.Idle,
    val remainingSeconds: Int = 0,
    val phaseDurationSeconds: Int = 0,
    val completedWorkouts: Int = 0,
    val totalWorkouts: Int = 0,
    val totalRemainingSeconds: Int = 0,
    val totalWorkoutDurationSeconds: Int = 0,
    val workoutColorIndex: Int = 0,
    val stepLabel: String = "",
    val nextStepLabel: String = "",
    val isActive: Boolean = false,
    val isPaused: Boolean = false
)
