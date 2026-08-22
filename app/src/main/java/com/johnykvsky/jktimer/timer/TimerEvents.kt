package com.johnykvsky.jktimer.timer

sealed interface TimerSoundEvent {
    data object Countdown : TimerSoundEvent
    data object TenSecondWarning : TimerSoundEvent
    data object WorkoutEnd : TimerSoundEvent
    data object TrainingFinish : TimerSoundEvent
}

data class TimerTick(
    val phase: TimerPhase,
    val remainingSeconds: Int,
    val phaseDurationSeconds: Int,
    val completedWorkouts: Int,
    val totalWorkouts: Int,
    val totalRemainingSeconds: Int = 0,
    val workoutColorIndex: Int = 0,
    val stepLabel: String = "",
    val nextStepLabel: String = "",
    val soundEvents: List<TimerSoundEvent> = emptyList()
)
