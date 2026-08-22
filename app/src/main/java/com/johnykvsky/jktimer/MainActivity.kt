package com.johnykvsky.jktimer

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import com.johnykvsky.jktimer.model.AppLanguage
import com.johnykvsky.jktimer.ui.TimerApp
import com.johnykvsky.jktimer.ui.TimerViewModel
import com.johnykvsky.jktimer.ui.theme.JktimerTheme
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val viewModel: TimerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        volumeControlStream = AudioManager.STREAM_MUSIC
        enableEdgeToEdge()

        setContent {
            val settings by viewModel.settings.collectAsState()
            val timerState by viewModel.timerState.collectAsState()
            val view = LocalView.current
            val baseContext = LocalContext.current

            val keepScreenOn = timerState.isActive && !timerState.isPaused
            DisposableEffect(keepScreenOn) {
                view.keepScreenOn = keepScreenOn
                onDispose {
                    view.keepScreenOn = false
                }
            }

            val localizedContext = remember(settings.language, baseContext) {
                createLocalizedContext(baseContext, settings.language)
            }
            val localizedConfiguration = remember(localizedContext) {
                localizedContext.resources.configuration
            }

            CompositionLocalProvider(
                LocalContext provides localizedContext,
                LocalConfiguration provides localizedConfiguration
            ) {
                JktimerTheme(themeMode = settings.themeMode) {
                    TimerApp(viewModel = viewModel)
                }
            }
        }
    }

    private fun createLocalizedContext(context: Context, language: AppLanguage): Context {
        val targetLocale = when (language) {
            AppLanguage.System -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Resources.getSystem().configuration.locales[0]
                } else {
                    @Suppress("DEPRECATION")
                    Resources.getSystem().configuration.locale
                }
            }
            AppLanguage.English -> Locale.ENGLISH
            AppLanguage.Polish -> Locale.forLanguageTag("pl")
        }
        val config = Configuration(context.resources.configuration).apply {
            setLocale(targetLocale)
            setLayoutDirection(targetLocale)
        }
        return context.createConfigurationContext(config)
    }
}
