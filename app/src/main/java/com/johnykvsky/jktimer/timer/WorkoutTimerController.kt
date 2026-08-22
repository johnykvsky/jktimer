package com.johnykvsky.jktimer.timer

import android.os.SystemClock
import com.johnykvsky.jktimer.model.TimerConfig
import com.johnykvsky.jktimer.model.TrainingPlan
import com.johnykvsky.jktimer.sound.TimerSoundPlayer
import com.johnykvsky.jktimer.vibration.TimerVibrationPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WorkoutTimerController(
    private val soundPlayer: TimerSoundPlayer,
    private val vibrationPlayer: TimerVibrationPlayer
) {
    private val _state = MutableStateFlow(TimerUiState())
    val state: StateFlow<TimerUiState> = _state.asStateFlow()

    private var job: Job? = null

    @Volatile
    private var isPaused = false

    fun start(
        plan: TrainingPlan,
        prepSeconds: Int,
        soundEnabled: Boolean,
        hapticEnabled: Boolean,
        scope: CoroutineScope
    ) {
        if (!plan.isValid()) return

        stop()
        isPaused = false
        job = scope.launch {
            val ticks = TimerSequence.build(plan, prepSeconds)
            if (ticks.isEmpty()) return@launch

            val totalDuration = plan.totalDurationSeconds()
            val firstTick = ticks.first()
            _state.value = TimerUiState(
                phase = firstTick.phase,
                remainingSeconds = firstTick.remainingSeconds,
                phaseDurationSeconds = firstTick.phaseDurationSeconds,
                completedWorkouts = firstTick.completedWorkouts,
                totalWorkouts = firstTick.totalWorkouts,
                totalRemainingSeconds = firstTick.totalRemainingSeconds,
                totalWorkoutDurationSeconds = totalDuration,
                workoutColorIndex = firstTick.workoutColorIndex,
                stepLabel = firstTick.stepLabel,
                nextStepLabel = firstTick.nextStepLabel,
                isActive = true,
                isPaused = false
            )

            for (tick in ticks) {
                waitIfPaused()
                _state.value = TimerUiState(
                    phase = tick.phase,
                    remainingSeconds = tick.remainingSeconds,
                    phaseDurationSeconds = tick.phaseDurationSeconds,
                    completedWorkouts = tick.completedWorkouts,
                    totalWorkouts = tick.totalWorkouts,
                    totalRemainingSeconds = tick.totalRemainingSeconds,
                    totalWorkoutDurationSeconds = totalDuration,
                    workoutColorIndex = tick.workoutColorIndex,
                    stepLabel = tick.stepLabel,
                    nextStepLabel = tick.nextStepLabel,
                    isActive = tick.phase != TimerPhase.Complete,
                    isPaused = isPaused
                )
                playEvents(tick.soundEvents, soundEnabled, hapticEnabled)

                if (tick.phase != TimerPhase.Complete && tick.remainingSeconds > 0) {
                    delayAccurateOneSecond()
                }
            }
        }
    }

    fun start(
        config: TimerConfig,
        prepSeconds: Int,
        soundEnabled: Boolean,
        hapticEnabled: Boolean,
        scope: CoroutineScope
    ) {
        start(TrainingPlan.Simple(config), prepSeconds, soundEnabled, hapticEnabled, scope)
    }

    fun pause() {
        if (_state.value.isActive && !isPaused) {
            isPaused = true
            _state.value = _state.value.copy(isPaused = true)
        }
    }

    fun resume() {
        if (_state.value.isActive && isPaused) {
            isPaused = false
            _state.value = _state.value.copy(isPaused = false)
        }
    }

    fun stop() {
        job?.cancel()
        job = null
        isPaused = false
        vibrationPlayer.cancel()
        _state.value = TimerUiState()
    }

    private suspend fun waitIfPaused() {
        while (isPaused) {
            delay(50)
        }
    }

    /**
     * Accurately delays 1000ms using SystemClock.elapsedRealtime() to prevent cumulative drift,
     * while correctly suspending and resuming when the timer is paused.
     */
    private suspend fun delayAccurateOneSecond() {
        var remainingMs = 1000L
        while (remainingMs > 0L) {
            waitIfPaused()
            val startMs = SystemClock.elapsedRealtime()
            val chunk = remainingMs.coerceAtMost(50L)
            delay(chunk)
            if (!isPaused) {
                val actualElapsed = SystemClock.elapsedRealtime() - startMs
                remainingMs -= actualElapsed
            }
        }
    }

    private fun playEvents(
        events: List<TimerSoundEvent>,
        soundEnabled: Boolean,
        hapticEnabled: Boolean
    ) {
        events.forEach { event ->
            if (soundEnabled) {
                when (event) {
                    TimerSoundEvent.Countdown -> soundPlayer.countdownBeep()
                    TimerSoundEvent.TenSecondWarning -> soundPlayer.tenSecondWarningBeep()
                    TimerSoundEvent.WorkoutEnd -> soundPlayer.workoutEndBeep()
                    TimerSoundEvent.TrainingFinish -> soundPlayer.trainingFinishBeep()
                }
            }
            if (hapticEnabled) {
                when (event) {
                    TimerSoundEvent.Countdown -> vibrationPlayer.countdownTick()
                    TimerSoundEvent.TenSecondWarning -> vibrationPlayer.tenSecondWarning()
                    TimerSoundEvent.WorkoutEnd -> vibrationPlayer.workoutEnd()
                    TimerSoundEvent.TrainingFinish -> vibrationPlayer.trainingFinish()
                }
            }
        }
    }
}
