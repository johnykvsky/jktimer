package com.johnykvsky.jktimer.sound

interface TimerSoundPlayer {
    fun setVolume(volume: Float)
    fun countdownBeep()
    fun halfWorkoutWarningBeep()
    fun workoutEndBeep()
    fun trainingFinishBeep()
    fun release()
}
