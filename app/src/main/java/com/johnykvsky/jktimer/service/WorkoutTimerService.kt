package com.johnykvsky.jktimer.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.johnykvsky.jktimer.MainActivity
import com.johnykvsky.jktimer.R

class WorkoutTimerService : Service() {

    private val notificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val fallbackTitle = getString(R.string.workout)
                val title = intent.getStringExtra(EXTRA_TITLE) ?: fallbackTitle
                val notification = buildNotification(title, getString(R.string.phase_get_ready))
                startForeground(NOTIFICATION_ID, notification)
            }
            ACTION_UPDATE -> {
                val fallbackTitle = getString(R.string.workout)
                val title = intent.getStringExtra(EXTRA_TITLE) ?: fallbackTitle
                val phaseName = intent.getStringExtra(EXTRA_PHASE) ?: fallbackTitle
                val remainingSeconds = intent.getIntExtra(EXTRA_REMAINING_SECONDS, 0)
                val nextStep = intent.getStringExtra(EXTRA_NEXT_STEP)
                val isPaused = intent.getBooleanExtra(EXTRA_IS_PAUSED, false)

                val statusPrefix = if (isPaused) getString(R.string.phase_paused) else phaseName
                val nextSuffix = if (!nextStep.isNullOrBlank()) " (${getString(R.string.next_step_prefix, nextStep)})" else ""
                val content = "$statusPrefix • ${remainingSeconds}s$nextSuffix"

                val notification = buildNotification(title, content)
                notificationManager.notify(NOTIFICATION_ID, notification)
            }
            ACTION_STOP -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.notification_channel_desc)
            setShowBadge(false)
        }
        notificationManager.createNotificationChannel(channel)
    }

    private fun buildNotification(title: String, content: String): Notification {
        val launchIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    companion object {
        const val CHANNEL_ID = "workout_timer_foreground_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START = "com.johnykvsky.jktimer.service.ACTION_START"
        const val ACTION_UPDATE = "com.johnykvsky.jktimer.service.ACTION_UPDATE"
        const val ACTION_STOP = "com.johnykvsky.jktimer.service.ACTION_STOP"

        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_PHASE = "extra_phase"
        const val EXTRA_REMAINING_SECONDS = "extra_remaining_seconds"
        const val EXTRA_NEXT_STEP = "extra_next_step"
        const val EXTRA_IS_PAUSED = "extra_is_paused"

        fun startService(context: Context, title: String) {
            val intent = Intent(context, WorkoutTimerService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_TITLE, title)
            }
            context.startForegroundService(intent)
        }

        fun updateService(
            context: Context,
            title: String,
            phase: String,
            remainingSeconds: Int,
            nextStep: String?,
            isPaused: Boolean
        ) {
            val intent = Intent(context, WorkoutTimerService::class.java).apply {
                action = ACTION_UPDATE
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_PHASE, phase)
                putExtra(EXTRA_REMAINING_SECONDS, remainingSeconds)
                putExtra(EXTRA_NEXT_STEP, nextStep)
                putExtra(EXTRA_IS_PAUSED, isPaused)
            }
            context.startService(intent)
        }

        fun stopService(context: Context) {
            val intent = Intent(context, WorkoutTimerService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}
