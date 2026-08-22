package com.johnykvsky.jktimer.model

data class TimerPreset(
    val id: Long,
    val name: String,
    val description: String = "",
    val plan: TrainingPlan
)
