package com.johnykvsky.jktimer.vibration

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class AndroidTimerVibrationPlayer(context: Context) : TimerVibrationPlayer {
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    override fun countdownTick() {
        vibrate(durationMs = 60, amplitude = VibrationEffect.DEFAULT_AMPLITUDE)
    }

    override fun tenSecondWarning() {
        vibratePattern(longArrayOf(0, 100, 100, 100))
    }

    override fun workoutEnd() {
        vibrate(durationMs = 350, amplitude = VibrationEffect.DEFAULT_AMPLITUDE)
    }

    override fun trainingFinish() {
        vibratePattern(longArrayOf(0, 200, 100, 200, 100, 400))
    }

    override fun cancel() {
        try {
            vibrator?.cancel()
        } catch (_: Exception) {
        }
    }

    private fun vibrate(durationMs: Long, amplitude: Int) {
        val v = vibrator ?: return
        if (!v.hasVibrator()) return

        try {
            v.vibrate(VibrationEffect.createOneShot(durationMs, amplitude))
        } catch (_: Exception) {
        }
    }

    private fun vibratePattern(timings: LongArray) {
        val v = vibrator ?: return
        if (!v.hasVibrator()) return

        try {
            v.vibrate(VibrationEffect.createWaveform(timings, -1))
        } catch (_: Exception) {
        }
    }
}
