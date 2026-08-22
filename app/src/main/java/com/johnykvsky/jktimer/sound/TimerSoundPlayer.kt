package com.johnykvsky.jktimer.sound

interface TimerSoundPlayer {
    fun setVolume(volume: Float)
    fun countdownBeep()
    fun tenSecondWarningBeep()
    fun workoutEndBeep()
    fun trainingFinishBeep()
    fun release()
}
