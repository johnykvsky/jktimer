package com.johnykvsky.jktimer.vibration

interface TimerVibrationPlayer {
    fun countdownTick()
    fun halfWorkoutWarning()
    fun workoutEnd()
    fun trainingFinish()
    fun cancel()
}
