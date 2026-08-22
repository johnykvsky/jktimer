package com.johnykvsky.jktimer.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import com.johnykvsky.jktimer.config.AppConfig
import java.util.concurrent.ConcurrentHashMap

class AndroidTimerSoundPlayer(context: Context) : TimerSoundPlayer {
    private val appContext = context.applicationContext
    private val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    private val audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_ALARM)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

    private val soundPool = SoundPool.Builder()
        .setMaxStreams(3)
        .setAudioAttributes(audioAttributes)
        .build()

    private val loadedSounds = ConcurrentHashMap<Int, Boolean>()

    private val countdownSoundId = soundPool.load(appContext, AppConfig.Sound.countdownSound, 1)
    private val tenSecondWarningSoundId = soundPool.load(appContext, AppConfig.Sound.tenSecondWarningSound, 1)
    private val workoutEndSoundId = soundPool.load(appContext, AppConfig.Sound.workoutEndSound, 1)
    private val trainingFinishSoundId = soundPool.load(appContext, AppConfig.Sound.trainingFinishSound, 1)

    init {
        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                loadedSounds[sampleId] = true
            }
        }
    }

    private var currentVolume = AppConfig.Sound.volume

    override fun setVolume(volume: Float) {
        currentVolume = volume.coerceIn(0f, 1f)
    }

    override fun countdownBeep() {
        play(countdownSoundId)
    }

    override fun tenSecondWarningBeep() {
        play(tenSecondWarningSoundId)
    }

    override fun workoutEndBeep() {
        play(workoutEndSoundId)
    }

    override fun trainingFinishBeep() {
        play(trainingFinishSoundId)
    }

    override fun release() {
        soundPool.release()
        loadedSounds.clear()
    }

    private fun play(soundId: Int) {
        if (loadedSounds[soundId] == true) {
            requestAudioFocus()
            soundPool.play(
                soundId,
                currentVolume,
                currentVolume,
                1,
                0,
                1.0f
            )
        }
    }

    private fun requestAudioFocus() {
        val am = audioManager ?: return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                    .setAudioAttributes(audioAttributes)
                    .build()
                am.requestAudioFocus(focusRequest)
            } else {
                @Suppress("DEPRECATION")
                am.requestAudioFocus(
                    null,
                    AudioManager.STREAM_ALARM,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
                )
            }
        } catch (_: Exception) {
        }
    }
}
