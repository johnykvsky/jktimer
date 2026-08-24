package com.johnykvsky.jktimer.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.johnykvsky.jktimer.model.AppSettings
import com.johnykvsky.jktimer.model.TimerPreset
import com.johnykvsky.jktimer.model.TrainingPlan
import com.johnykvsky.jktimer.sound.AndroidTimerSoundPlayer
import com.johnykvsky.jktimer.sound.TimerSoundPlayer
import com.johnykvsky.jktimer.service.WorkoutTimerService
import com.johnykvsky.jktimer.storage.AppSettingsRepository
import com.johnykvsky.jktimer.storage.TimerPresetRepository
import com.johnykvsky.jktimer.timer.TimerUiState
import com.johnykvsky.jktimer.timer.WorkoutTimerController
import com.johnykvsky.jktimer.vibration.AndroidTimerVibrationPlayer
import com.johnykvsky.jktimer.vibration.TimerVibrationPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AppScreen {
    data object Home : AppScreen
    data class EditPreset(val preset: TimerPreset?) : AppScreen
    data object TemporaryTimer : AppScreen
    data object Settings : AppScreen
    data object RunTimer : AppScreen
}

class TimerViewModel @JvmOverloads constructor(
    application: Application,
    private val presetRepository: TimerPresetRepository = TimerPresetRepository(application),
    private val settingsRepository: AppSettingsRepository = AppSettingsRepository(application),
    private val soundPlayer: TimerSoundPlayer = AndroidTimerSoundPlayer(application),
    private val vibrationPlayer: TimerVibrationPlayer = AndroidTimerVibrationPlayer(application)
) : AndroidViewModel(application) {

    val timerController = WorkoutTimerController(soundPlayer, vibrationPlayer)
    val timerState: StateFlow<TimerUiState> = timerController.state

    private val _presets = MutableStateFlow(presetRepository.loadPresets())
    val presets: StateFlow<List<TimerPreset>> = _presets.asStateFlow()

    private val _settings = MutableStateFlow(settingsRepository.load())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private val _currentScreen = MutableStateFlow<AppScreen>(AppScreen.Home)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _activeTitle = MutableStateFlow("Temporary timer")
    val activeTitle: StateFlow<String> = _activeTitle.asStateFlow()

    init {
        soundPlayer.setVolume(_settings.value.soundVolume)
        viewModelScope.launch {
            timerState.collect { state ->
                if (state.isActive) {
                    WorkoutTimerService.updateService(
                        getApplication(),
                        _activeTitle.value,
                        state.phase.name,
                        state.remainingSeconds,
                        state.nextStepLabel.takeIf { it.isNotBlank() },
                        state.isPaused
                    )
                } else {
                    WorkoutTimerService.stopService(getApplication())
                }
            }
        }
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun navigateBack(): Boolean {
        return when (val screen = _currentScreen.value) {
            AppScreen.Home -> false
            is AppScreen.EditPreset,
            AppScreen.TemporaryTimer,
            AppScreen.Settings -> {
                _currentScreen.value = AppScreen.Home
                true
            }
            AppScreen.RunTimer -> {
                if (!timerState.value.isActive) {
                    timerController.stop()
                    _currentScreen.value = AppScreen.Home
                    true
                } else {
                    false
                }
            }
        }
    }

    fun startTimer(plan: TrainingPlan, title: String) {
        _activeTitle.value = title
        val currentSettings = _settings.value
        soundPlayer.setVolume(currentSettings.soundVolume)
        timerController.start(
            plan = plan,
            prepSeconds = currentSettings.prepSeconds,
            soundEnabled = currentSettings.soundEnabled,
            hapticEnabled = currentSettings.hapticFeedbackEnabled,
            halfWorkoutWarningEnabled = currentSettings.halfWorkoutWarningEnabled,
            halfTimeRoundingMode = currentSettings.halfTimeRoundingMode,
            countdownSoundsEnabled = currentSettings.countdownSoundsEnabled,
            scope = viewModelScope
        )
        _currentScreen.value = AppScreen.RunTimer
    }

    fun pauseTimer() {
        timerController.pause()
    }

    fun resumeTimer() {
        timerController.resume()
    }

    fun stopTimerAndGoHome() {
        timerController.stop()
        _currentScreen.value = AppScreen.Home
    }

    fun savePreset(existingId: Long?, name: String, description: String, plan: TrainingPlan) {
        val updated = presetRepository.savePreset(existingId, name, description, plan)
        _presets.value = updated
        _currentScreen.value = AppScreen.Home
    }

    fun deletePreset(id: Long) {
        val updated = presetRepository.deletePreset(id)
        _presets.value = updated
    }

    fun toggleSound() {
        val current = _settings.value
        updateSettings(current.copy(soundEnabled = !current.soundEnabled))
    }

    fun updateSettings(newSettings: AppSettings) {
        _settings.value = newSettings
        soundPlayer.setVolume(newSettings.soundVolume)
        timerController.updateSoundEnabled(newSettings.soundEnabled)
        timerController.updateHapticEnabled(newSettings.hapticFeedbackEnabled)
        settingsRepository.save(newSettings)
    }

    fun testSound() {
        soundPlayer.setVolume(_settings.value.soundVolume)
        soundPlayer.countdownBeep()
    }

    fun testVibration() {
        vibrationPlayer.workoutEnd()
    }

    override fun onCleared() {
        super.onCleared()
        timerController.stop()
        soundPlayer.release()
        vibrationPlayer.cancel()
    }
}
