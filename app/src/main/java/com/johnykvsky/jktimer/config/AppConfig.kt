package com.johnykvsky.jktimer.config

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.johnykvsky.jktimer.R

object AppConfig {
    object Input {
        const val maxNameLength = 100
        const val maxDescriptionLength = 2_000
    }

    object TimerScreen {
        // Ring and countdown text colors for active phases.
        val workoutColorPrimary = Color(0xFFE68A45)   // Energizing warm orange
        val workoutColorSecondary = Color(0xFF2979FF) // Electric vibrant blue
        val workoutColor = workoutColorPrimary
        val restColor = Color(0xFF2E7D32)            // Calming green

        // Thickness of the countdown ring.
        val ringStrokeWidth: Dp = 18.dp
    }

    object Sound {
        // Bundled audio files. Edit these if you add different files under res/raw.
        val countdownSound = R.raw.countdown_beep
        val tenSecondWarningSound = R.raw.ten_second_warning
        val workoutEndSound = R.raw.workout_interval_end
        val trainingFinishSound = R.raw.training_end

        // SoundPool volume, from 0.0 to 1.0.
        const val volume = 1.0f
    }

    object Metadata {
        const val appName = "JK Timer"
        const val versionName = "0.0.1"
        const val author = "JK"
    }
}
