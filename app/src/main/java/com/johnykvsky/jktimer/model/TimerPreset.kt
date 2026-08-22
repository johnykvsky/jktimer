package com.johnykvsky.jktimer.model

data class TimerPreset(
    val id: Long,
    val name: String,
    val description: String = "",
    val plan: TrainingPlan
) {
    // Backwards-compatibility constructor for Simple configs
    constructor(
        id: Long,
        name: String,
        description: String = "",
        config: TimerConfig
    ) : this(id, name, description, TrainingPlan.Simple(config))

    val config: TimerConfig?
        get() = (plan as? TrainingPlan.Simple)?.config
}
