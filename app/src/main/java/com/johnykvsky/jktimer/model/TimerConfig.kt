package com.johnykvsky.jktimer.model

data class TimerConfig(
    val workoutSeconds: Int,
    val restSeconds: Int,
    val repeats: Int
) {
    fun isValid(): Boolean =
        workoutSeconds in 1..MAX_WORKOUT_SECONDS &&
            restSeconds in 1..MAX_REST_SECONDS &&
            repeats in 1..MAX_REPEATS

    fun totalDurationSeconds(): Int {
        if (!isValid()) return 0
        return (repeats * workoutSeconds) + ((repeats - 1).coerceAtLeast(0) * restSeconds)
    }

    fun formattedDuration(): String = formatDuration(totalDurationSeconds())

    companion object {
        const val MAX_WORKOUT_SECONDS = 9_999
        const val MAX_REST_SECONDS = 9_999
        const val MAX_REPEATS = 999

        fun formatDuration(totalSeconds: Int): String {
            val mins = totalSeconds / 60
            val secs = totalSeconds % 60
            return if (mins > 0) {
                if (secs > 0) "${mins}m ${secs}s" else "${mins}m"
            } else {
                "${secs}s"
            }
        }
    }
}
