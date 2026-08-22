package com.johnykvsky.jktimer.vibration

interface TimerVibrationPlayer {
    fun countdownTick()
    fun tenSecondWarning()
    fun workoutEnd()
    fun trainingFinish()
    fun cancel()
}
