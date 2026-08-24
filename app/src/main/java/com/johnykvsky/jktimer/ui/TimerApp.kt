package com.johnykvsky.jktimer.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.Configuration
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.johnykvsky.jktimer.R
import com.johnykvsky.jktimer.config.AppConfig
import com.johnykvsky.jktimer.model.AppLanguage
import com.johnykvsky.jktimer.model.AppSettings
import com.johnykvsky.jktimer.model.HalfTimeRoundingMode
import com.johnykvsky.jktimer.model.StepType
import com.johnykvsky.jktimer.model.TimerConfig
import com.johnykvsky.jktimer.model.TimerPreset
import com.johnykvsky.jktimer.model.TimerStep
import com.johnykvsky.jktimer.model.TrainingPlan
import com.johnykvsky.jktimer.timer.TimerPhase
import com.johnykvsky.jktimer.timer.TimerUiState
import com.johnykvsky.jktimer.ui.theme.ThemeMode
import java.util.UUID

@Composable
fun TimerApp(viewModel: TimerViewModel) {
    val presets by viewModel.presets.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val screen by viewModel.currentScreen.collectAsState()
    val activeTitle by viewModel.activeTitle.collectAsState()
    val timerState by viewModel.timerState.collectAsState()

    when (val current = screen) {
        AppScreen.Home -> TimerHomeScreen(
            presets = presets,
            soundEnabled = settings.soundEnabled,
            onToggleSound = { viewModel.toggleSound() },
            onNewPreset = { viewModel.navigateTo(AppScreen.EditPreset(null)) },
            onTemporaryTimer = { viewModel.navigateTo(AppScreen.TemporaryTimer) },
            onStartPreset = { preset ->
                viewModel.startTimer(preset.plan, preset.name)
            },
            onEditPreset = { viewModel.navigateTo(AppScreen.EditPreset(it)) },
            onDeletePreset = { preset ->
                viewModel.deletePreset(preset.id)
            },
            onOpenSettings = { viewModel.navigateTo(AppScreen.Settings) }
        )

        AppScreen.Settings -> {
            BackHandler { viewModel.navigateBack() }
            SettingsScreen(
                settings = settings,
                onSettingsChange = { viewModel.updateSettings(it) },
                onTestSound = { viewModel.testSound() },
                onTestVibration = { viewModel.testVibration() },
                onBack = { viewModel.navigateBack() }
            )
        }

        is AppScreen.EditPreset -> {
            BackHandler { viewModel.navigateBack() }
            TimerFormScreen(
                title = if (current.preset == null) stringResource(R.string.new_training) else stringResource(R.string.edit_training),
                initialName = current.preset?.name.orEmpty(),
                initialPlan = current.preset?.plan ?: TrainingPlan.Simple(TimerConfig(60, 30, 1)),
                requireName = true,
                timeInterval = settings.timeIntervalSeconds,
                primaryAction = stringResource(R.string.save),
                onBack = { viewModel.navigateBack() },
                onDelete = if (current.preset != null) {
                    {
                        viewModel.deletePreset(current.preset.id)
                        viewModel.navigateBack()
                    }
                } else null,
                onSubmit = { name, plan ->
                    viewModel.savePreset(current.preset?.id, name.orEmpty(), "", plan)
                }
            )
        }

        AppScreen.TemporaryTimer -> {
            val tempWorkoutTitle = stringResource(R.string.temporary_training)
            BackHandler { viewModel.navigateBack() }
            TimerFormScreen(
                title = tempWorkoutTitle,
                initialName = "",
                initialPlan = TrainingPlan.Simple(TimerConfig(60, 30, 1)),
                requireName = false,
                showTypeSelector = false,
                timeInterval = settings.timeIntervalSeconds,
                primaryAction = stringResource(R.string.start),
                onBack = { viewModel.navigateBack() },
                onSubmit = { _, plan ->
                    viewModel.startTimer(plan, tempWorkoutTitle)
                }
            )
        }

        AppScreen.RunTimer -> {
            ActiveTimerScreen(
                title = activeTitle,
                state = timerState,
                soundEnabled = settings.soundEnabled,
                showTotalRemainingTime = settings.showTotalRemainingTime,
                onToggleSound = { viewModel.toggleSound() },
                onPause = { viewModel.pauseTimer() },
                onResume = { viewModel.resumeTimer() },
                onExit = { viewModel.stopTimerAndGoHome() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimerHomeScreen(
    presets: List<TimerPreset>,
    soundEnabled: Boolean,
    onToggleSound: () -> Unit,
    onNewPreset: () -> Unit,
    onTemporaryTimer: () -> Unit,
    onStartPreset: (TimerPreset) -> Unit,
    onEditPreset: (TimerPreset) -> Unit,
    onDeletePreset: (TimerPreset) -> Unit,
    onOpenSettings: () -> Unit
) {
    var presetToDelete by remember { mutableStateOf<TimerPreset?>(null) }
    var presetForSummary by remember { mutableStateOf<TimerPreset?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.home_title), fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onToggleSound) {
                        Icon(
                            if (soundEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                            contentDescription = if (soundEnabled) stringResource(R.string.mute_sound) else stringResource(R.string.unmute_sound)
                        )
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings_title))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onTemporaryTimer,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.quick_training))
                }
                OutlinedButton(
                    onClick = onNewPreset,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.add_training))
                }
            }

            Text(
                text = stringResource(R.string.saved_trainings),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            if (presets.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Timer,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = stringResource(R.string.no_saved_trainings_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = stringResource(R.string.no_saved_trainings_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Button(onClick = onNewPreset) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.create_first_training))
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(presets, key = { it.id }) { preset ->
                        PresetCard(
                            preset = preset,
                            onStart = { onStartPreset(preset) },
                            onEdit = { onEditPreset(preset) },
                            onDelete = { presetToDelete = preset },
                            onShowSummary = { presetForSummary = preset }
                        )
                    }
                }
            }
        }
    }

    presetToDelete?.let { preset ->
        AlertDialog(
            onDismissRequest = { presetToDelete = null },
            title = { Text(stringResource(R.string.delete_training_title)) },
            text = { Text(stringResource(R.string.delete_training_confirm, preset.name)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeletePreset(preset)
                        presetToDelete = null
                    }
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { presetToDelete = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    presetForSummary?.let { preset ->
        TrainingSummaryDialog(
            preset = preset,
            onDismiss = { presetForSummary = null }
        )
    }
}

@Composable
private fun PresetCard(
    preset: TimerPreset,
    onStart: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onShowSummary: () -> Unit = {}
) {
    val plan = preset.plan
    val isAdvanced = plan is TrainingPlan.Advanced

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = preset.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                AssistChip(
                    onClick = onShowSummary,
                    label = {
                        Text(
                            text = if (isAdvanced) stringResource(R.string.advanced) else stringResource(R.string.simple),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (isAdvanced) {
                            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                        } else {
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                        }
                    )
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = plan.formattedDuration(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                val summaryText = when (plan) {
                    is TrainingPlan.Simple -> stringResource(
                        R.string.summary_simple_format,
                        plan.config.workoutSeconds,
                        plan.config.restSeconds,
                        plan.config.repeats
                    )
                    is TrainingPlan.Advanced -> {
                        val workouts = plan.steps.count { it.type == StepType.Workout }
                        val rests = plan.steps.count { it.type == StepType.Rest }
                        stringResource(
                            R.string.summary_advanced_format,
                            plan.steps.size,
                            workouts,
                            rests
                        )
                    }
                }

                Text(
                    text = summaryText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (preset.description.isNotBlank()) {
                Text(
                    text = preset.description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            ) {
                Button(onClick = onStart) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.start))
                }
                OutlinedButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.edit))
                }
                OutlinedButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.delete))
                }
            }
        }
    }
}

@Composable
private fun TrainingSummaryDialog(
    preset: TimerPreset,
    onDismiss: () -> Unit
) {
    val plan = preset.plan
    val isAdvanced = plan is TrainingPlan.Advanced
    val context = LocalContext.current
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val timePrefix = stringResource(R.string.summary_time_prefix)
    val workoutLabel = stringResource(R.string.workout)
    val restLabel = stringResource(R.string.rest)
    val setsLabel = stringResource(R.string.sets_label)
    val copiedMessage = stringResource(R.string.copied_to_clipboard)
    val summaryText = remember(preset, plan, timePrefix, workoutLabel, restLabel, setsLabel) {
        plan.generateShareableSummary(
            title = preset.name,
            timePrefix = timePrefix,
            workoutLabel = workoutLabel,
            restLabel = restLabel,
            setsLabel = setsLabel
        )
    }

    fun copyToClipboard() {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clip = ClipData.newPlainText("workout_summary", summaryText)
        clipboard?.setPrimaryClip(clip)
        Toast.makeText(context, copiedMessage, Toast.LENGTH_SHORT).show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(if (isLandscape) 0.75f else 0.92f)
            .padding(vertical = if (isLandscape) 8.dp else 16.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.training_summary_title),
                        style = if (isLandscape) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = preset.name.ifBlank {
                            if (isAdvanced) stringResource(R.string.advanced_training) else stringResource(R.string.simple_training)
                        },
                        style = if (isLandscape) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = { copyToClipboard() },
                    modifier = if (isLandscape) Modifier.size(36.dp) else Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Default.ContentCopy,
                        contentDescription = stringResource(R.string.copy_to_clipboard),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = if (isLandscape) Modifier.size(20.dp) else Modifier.size(24.dp)
                    )
                }
            }
        },
        text = {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = if (isLandscape) 140.dp else 380.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = summaryText,
                    style = if (isLandscape) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(if (isLandscape) 10.dp else 14.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = if (isLandscape) Modifier.height(36.dp) else Modifier
            ) {
                Text(
                    text = stringResource(R.string.close),
                    fontSize = if (isLandscape) 13.sp else 14.sp
                )
            }
        }
    )
}

private enum class FormTrainingType {
    Simple,
    Advanced
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun TimerFormScreen(
    title: String,
    initialName: String,
    initialPlan: TrainingPlan,
    requireName: Boolean,
    showTypeSelector: Boolean = true,
    timeInterval: Int = AppSettings.DEFAULT_TIME_INTERVAL_SECONDS,
    primaryAction: String,
    onBack: () -> Unit,
    onDelete: (() -> Unit)? = null,
    onSubmit: (String?, TrainingPlan) -> Unit
) {
    var trainingType by remember {
        mutableStateOf(if (initialPlan is TrainingPlan.Advanced) FormTrainingType.Advanced else FormTrainingType.Simple)
    }

    var name by remember { mutableStateOf(initialName) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    // Simple fields
    val initialSimpleConfig = (initialPlan as? TrainingPlan.Simple)?.config ?: TimerConfig(60, 30, 1)
    var workoutSeconds by remember { mutableIntStateOf(initialSimpleConfig.workoutSeconds.coerceIn(1, TimerConfig.MAX_WORKOUT_SECONDS)) }
    var restSeconds by remember { mutableIntStateOf(initialSimpleConfig.restSeconds.coerceIn(1, TimerConfig.MAX_REST_SECONDS)) }
    var repeats by remember { mutableIntStateOf(initialSimpleConfig.repeats.coerceIn(1, TimerConfig.MAX_REPEATS)) }

    // Advanced fields: start empty when creating a new routine from scratch
    val initialSteps = (initialPlan as? TrainingPlan.Advanced)?.steps ?: emptyList()
    val advancedSteps = remember { mutableStateListOf<TimerStep>().apply { addAll(initialSteps) } }

    val currentSimpleConfig = TimerConfig(
        workoutSeconds = workoutSeconds,
        restSeconds = restSeconds,
        repeats = repeats
    )

    val currentPlan: TrainingPlan = if (trainingType == FormTrainingType.Simple) {
        TrainingPlan.Simple(currentSimpleConfig)
    } else {
        TrainingPlan.Advanced(advancedSteps.toList())
    }

    val nameValid = !requireName || name.isNotBlank()
    val formValid = nameValid && currentPlan.isValid()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    if (onDelete != null) {
                        IconButton(onClick = { showDeleteConfirmation = true }) {
                            Icon(
                                Icons.Default.DeleteOutline,
                                contentDescription = stringResource(R.string.delete),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (showTypeSelector) {
                // Segmented mode selector
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = trainingType == FormTrainingType.Simple,
                        onClick = { trainingType = FormTrainingType.Simple },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        icon = {}
                    ) {
                        Text(stringResource(R.string.simple_training), fontWeight = FontWeight.SemiBold)
                    }
                    SegmentedButton(
                        selected = trainingType == FormTrainingType.Advanced,
                        onClick = { trainingType = FormTrainingType.Advanced },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        icon = {}
                    ) {
                        Text(stringResource(R.string.advanced_training), fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Live Summary Duration Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = if (trainingType == FormTrainingType.Simple) {
                                stringResource(R.string.total_training_time)
                            } else {
                                stringResource(R.string.advanced_sequence_duration)
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = if (currentPlan.isValid()) currentPlan.formattedDuration() else "--",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            if (requireName) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it.take(AppConfig.Input.maxNameLength)
                    },
                    label = { Text(stringResource(R.string.training_name)) },
                    leadingIcon = { Icon(Icons.Default.FitnessCenter, contentDescription = null) },
                    singleLine = true,
                    isError = !nameValid,
                    supportingText = {
                        Text(
                            if (!nameValid) stringResource(R.string.name_required) else "${name.length}/${AppConfig.Input.maxNameLength}"
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (trainingType == FormTrainingType.Simple) {
                // SIMPLE TRAINING FORM: Stepper Controls (No manual text fields)
                SimpleTimerStepperCard(
                    label = stringResource(R.string.workout),
                    valueText = "${workoutSeconds}s",
                    leadingIcon = {
                        Icon(
                            Icons.Default.Timer,
                            contentDescription = null,
                            tint = AppConfig.TimerScreen.workoutColorPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    onDecrement = { workoutSeconds = (workoutSeconds - timeInterval).coerceAtLeast(1) },
                    onIncrement = { workoutSeconds = (workoutSeconds + timeInterval).coerceAtMost(TimerConfig.MAX_WORKOUT_SECONDS) },
                    canDecrement = workoutSeconds - timeInterval >= 1,
                    canIncrement = workoutSeconds + timeInterval <= TimerConfig.MAX_WORKOUT_SECONDS
                )

                SimpleTimerStepperCard(
                    label = stringResource(R.string.rest),
                    valueText = "${restSeconds}s",
                    leadingIcon = {
                        Icon(
                            Icons.Default.HourglassEmpty,
                            contentDescription = null,
                            tint = AppConfig.TimerScreen.restColor,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    onDecrement = { restSeconds = (restSeconds - timeInterval).coerceAtLeast(1) },
                    onIncrement = { restSeconds = (restSeconds + timeInterval).coerceAtMost(TimerConfig.MAX_REST_SECONDS) },
                    canDecrement = restSeconds - timeInterval >= 1,
                    canIncrement = restSeconds + timeInterval <= TimerConfig.MAX_REST_SECONDS
                )

                SimpleTimerStepperCard(
                    label = stringResource(R.string.repeats_sets),
                    valueText = "$repeats",
                    leadingIcon = {
                        Icon(
                            Icons.Default.Repeat,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    onDecrement = { repeats = (repeats - 1).coerceAtLeast(1) },
                    onIncrement = { repeats = (repeats + 1).coerceAtMost(TimerConfig.MAX_REPEATS) },
                    canDecrement = repeats > 1,
                    canIncrement = repeats < TimerConfig.MAX_REPEATS
                )
            } else {
                // ADVANCED TRAINING FORM: Single "Add item" button directly below Name/Description
                Button(
                    onClick = {
                        if (advancedSteps.size < TrainingPlan.MAX_ADVANCED_STEPS) {
                            advancedSteps.add(
                                TimerStep(
                                    id = UUID.randomUUID().toString(),
                                    type = StepType.Workout,
                                    durationSeconds = 30
                                )
                            )
                        }
                    },
                    enabled = advancedSteps.size < TrainingPlan.MAX_ADVANCED_STEPS,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.add_item))
                }

                if (advancedSteps.size >= TrainingPlan.MAX_ADVANCED_STEPS) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.max_steps_reached, TrainingPlan.MAX_ADVANCED_STEPS),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                if (advancedSteps.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Timer,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = stringResource(R.string.no_steps_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = stringResource(R.string.no_steps_desc, TrainingPlan.MAX_ADVANCED_STEPS),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.training_steps_count, advancedSteps.size, TrainingPlan.MAX_ADVANCED_STEPS),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    advancedSteps.forEachIndexed { index, step ->
                        AdvancedStepCard(
                            step = step,
                            index = index,
                            totalSteps = advancedSteps.size,
                            timeInterval = timeInterval,
                            onUpdate = { updated -> advancedSteps[index] = updated },
                            onMoveUp = {
                                if (index > 0) {
                                    val item = advancedSteps.removeAt(index)
                                    advancedSteps.add(index - 1, item)
                                }
                            },
                            onMoveDown = {
                                if (index < advancedSteps.size - 1) {
                                    val currentStep = advancedSteps.removeAt(index)
                                    advancedSteps.add(index + 1, currentStep)
                                }
                            },
                            onDelete = {
                                advancedSteps.removeAt(index)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (onDelete != null) {
                    OutlinedButton(
                        onClick = { showDeleteConfirmation = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.delete), modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
                Button(
                    onClick = { onSubmit(name.takeIf { requireName }, currentPlan) },
                    enabled = formValid,
                    modifier = Modifier.weight(if (onDelete != null) 1.5f else 1f)
                ) {
                    Text(primaryAction, fontSize = 16.sp, modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }
    }

    if (showDeleteConfirmation && onDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text(stringResource(R.string.delete_training_title)) },
            text = { Text(stringResource(R.string.delete_training_confirm, name.ifBlank { title })) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                        onDelete()
                    }
                ) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun SimpleTimerStepperCard(
    label: String,
    valueText: String,
    leadingIcon: @Composable () -> Unit,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    canDecrement: Boolean,
    canIncrement: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        leadingIcon()
                    }
                }
                Column(verticalArrangement = Arrangement.Center) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = valueText,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalIconButton(
                    onClick = onDecrement,
                    enabled = canDecrement,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Default.Remove,
                        contentDescription = stringResource(R.string.decrease),
                        modifier = Modifier.size(24.dp)
                    )
                }
                FilledTonalIconButton(
                    onClick = onIncrement,
                    enabled = canIncrement,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = stringResource(R.string.increase),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AdvancedStepCard(
    step: TimerStep,
    index: Int,
    totalSteps: Int,
    timeInterval: Int = AppSettings.DEFAULT_TIME_INTERVAL_SECONDS,
    onUpdate: (TimerStep) -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDelete: () -> Unit
) {
    val isWorkout = step.type == StepType.Workout
    val typeColor = if (isWorkout) AppConfig.TimerScreen.workoutColorPrimary else AppConfig.TimerScreen.restColor

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = typeColor.copy(alpha = 0.18f)
                    ) {
                        Text(
                            text = "#${index + 1}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = typeColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    FilterChip(
                        selected = isWorkout,
                        onClick = { onUpdate(step.copy(type = StepType.Workout)) },
                        label = { Text(stringResource(R.string.workout)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AppConfig.TimerScreen.workoutColorPrimary.copy(alpha = 0.22f),
                            selectedLabelColor = AppConfig.TimerScreen.workoutColorPrimary
                        )
                    )

                    FilterChip(
                        selected = !isWorkout,
                        onClick = { onUpdate(step.copy(type = StepType.Rest, name = "")) },
                        label = { Text(stringResource(R.string.rest)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AppConfig.TimerScreen.restColor.copy(alpha = 0.22f),
                            selectedLabelColor = AppConfig.TimerScreen.restColor
                        )
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    IconButton(onClick = onMoveUp, enabled = index > 0, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.ArrowUpward, contentDescription = stringResource(R.string.move_up), modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onMoveDown, enabled = index < totalSteps - 1, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.ArrowDownward, contentDescription = stringResource(R.string.move_down), modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = stringResource(R.string.delete), modifier = Modifier.size(18.dp))
                    }
                }
            }

            // Stepper for step duration (large number on left, big - / + buttons on right)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = if (isWorkout) stringResource(R.string.workout) else stringResource(R.string.rest),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${step.durationSeconds}s",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = typeColor
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilledTonalIconButton(
                            onClick = {
                                val newDuration = (step.durationSeconds - timeInterval).coerceAtLeast(1)
                                onUpdate(step.copy(durationSeconds = newDuration))
                            },
                            enabled = step.durationSeconds - timeInterval >= 1,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                Icons.Default.Remove,
                                contentDescription = stringResource(R.string.decrease),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        FilledTonalIconButton(
                            onClick = {
                                val newDuration = (step.durationSeconds + timeInterval).coerceAtMost(TimerConfig.MAX_WORKOUT_SECONDS)
                                onUpdate(step.copy(durationSeconds = newDuration))
                            },
                            enabled = step.durationSeconds + timeInterval <= TimerConfig.MAX_WORKOUT_SECONDS,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = stringResource(R.string.increase),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            // Optional Step title input - workouts only
            if (isWorkout) {
                OutlinedTextField(
                    value = step.name,
                    onValueChange = { onUpdate(step.copy(name = it.take(50))) },
                    label = { Text(stringResource(R.string.title_optional)) },
                    singleLine = true,
                    placeholder = {
                        Text(stringResource(R.string.eg_pushups))
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActiveTimerScreen(
    title: String,
    state: TimerUiState,
    soundEnabled: Boolean,
    showTotalRemainingTime: Boolean = true,
    onToggleSound: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onExit: () -> Unit
) {
    val completionMessages = listOf(
        stringResource(R.string.msg_great_workout),
        stringResource(R.string.msg_awesome_job),
        stringResource(R.string.msg_workout_completed),
        stringResource(R.string.msg_boom_intense),
    )
    var completionMessage by remember { mutableStateOf(completionMessages.first()) }
    var showStopConfirmation by remember { mutableStateOf(false) }

    LaunchedEffect(state.phase) {
        if (state.phase == TimerPhase.Complete) {
            completionMessage = completionMessages.random()
        }
    }

    BackHandler {
        if (state.isActive) {
            showStopConfirmation = true
        } else {
            onExit()
        }
    }

    val workoutColor = if (state.workoutColorIndex % 2 == 0) {
        AppConfig.TimerScreen.workoutColorPrimary
    } else {
        AppConfig.TimerScreen.workoutColorSecondary
    }

    val phaseColor = when (state.phase) {
        TimerPhase.Workout -> workoutColor
        TimerPhase.Rest -> AppConfig.TimerScreen.restColor
        else -> MaterialTheme.colorScheme.onSurface
    }

    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val localizedNextStep = formatNextStepLabel(state.nextStepLabel)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (state.isActive) {
                            showStopConfirmation = true
                        } else {
                            onExit()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = onToggleSound) {
                        Icon(
                            if (soundEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                            contentDescription = if (soundEnabled) stringResource(R.string.mute_sound) else stringResource(R.string.unmute_sound)
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (isLandscape) {
            // Landscape 2-column layout
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Left Column: Countdown Ring
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CountdownRing(
                        remainingSeconds = state.remainingSeconds,
                        phaseDurationSeconds = state.phaseDurationSeconds,
                        phase = state.phase,
                        workoutColorIndex = state.workoutColorIndex,
                        completionMessage = completionMessage,
                        onTogglePause = {
                            if (state.isActive && (state.phase == TimerPhase.Workout || state.phase == TimerPhase.Rest)) {
                                if (state.isPaused) onResume() else onPause()
                            }
                        },
                        modifier = Modifier
                            .fillMaxHeight(0.85f)
                            .aspectRatio(1f)
                    )

                    if (state.isActive && (state.phase == TimerPhase.Workout || state.phase == TimerPhase.Rest)) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (state.isPaused) stringResource(R.string.tap_to_resume) else stringResource(R.string.tap_to_pause),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Right Column: Details & Controls
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = phaseText(state.phase, state.isPaused),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = phaseColor,
                        textAlign = TextAlign.Center
                    )

                    if (state.stepLabel.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = state.stepLabel,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }

                    if (state.isActive && state.phase != TimerPhase.Complete && localizedNextStep.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    Icons.Default.SkipNext,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = stringResource(R.string.next_step_prefix, localizedNextStep),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }

                    if (showTotalRemainingTime && state.isActive && state.totalRemainingSeconds > 0) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh
                        ) {
                            Text(
                                text = stringResource(R.string.total_remaining_prefix, TimerConfig.formatDuration(state.totalRemainingSeconds)),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }
                    }

                    if (state.totalWorkouts in 1..30) {
                        Spacer(modifier = Modifier.height(12.dp))
                        RoundStepper(
                            totalWorkouts = state.totalWorkouts,
                            completedWorkouts = state.completedWorkouts,
                            phase = state.phase,
                            workoutColorIndex = state.workoutColorIndex
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = {
                            if (state.isActive) {
                                showStopConfirmation = true
                            } else {
                                onExit()
                            }
                        },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Text(
                            if (state.phase == TimerPhase.Complete) stringResource(R.string.finish_and_exit) else stringResource(R.string.stop_training),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        } else {
            // Portrait layout
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Phase indicator
                Text(
                    text = phaseText(state.phase, state.isPaused),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = phaseColor,
                    textAlign = TextAlign.Center
                )

                // Custom exercise label if present
                if (state.stepLabel.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = state.stepLabel,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                // Next up pill
                if (state.isActive && state.phase != TimerPhase.Complete && localizedNextStep.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.SkipNext,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = stringResource(R.string.next_step_prefix, localizedNextStep),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }

                // Total remaining time badge (toggleable in Settings)
                if (showTotalRemainingTime && state.isActive && state.totalRemainingSeconds > 0) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Text(
                            text = stringResource(R.string.total_remaining_prefix, TimerConfig.formatDuration(state.totalRemainingSeconds)),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Animated Countdown Ring with Alternating Workout Colors
                CountdownRing(
                    remainingSeconds = state.remainingSeconds,
                    phaseDurationSeconds = state.phaseDurationSeconds,
                    phase = state.phase,
                    workoutColorIndex = state.workoutColorIndex,
                    completionMessage = completionMessage,
                    onTogglePause = {
                        if (state.isActive && (state.phase == TimerPhase.Workout || state.phase == TimerPhase.Rest)) {
                            if (state.isPaused) onResume() else onPause()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.82f)
                        .aspectRatio(1f)
                )

                if (state.isActive && (state.phase == TimerPhase.Workout || state.phase == TimerPhase.Rest)) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (state.isPaused) stringResource(R.string.tap_to_resume) else stringResource(R.string.tap_to_pause),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                // Visual round stepper dots / pills
                if (state.totalWorkouts in 1..30) {
                    Spacer(modifier = Modifier.height(20.dp))
                    RoundStepper(
                        totalWorkouts = state.totalWorkouts,
                        completedWorkouts = state.completedWorkouts,
                        phase = state.phase,
                        workoutColorIndex = state.workoutColorIndex
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            if (state.isActive) {
                                showStopConfirmation = true
                            } else {
                                onExit()
                            }
                        },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Text(
                            if (state.phase == TimerPhase.Complete) stringResource(R.string.finish_and_exit) else stringResource(R.string.stop_training),
                            fontSize = 15.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }

    if (showStopConfirmation) {
        AlertDialog(
            onDismissRequest = { showStopConfirmation = false },
            title = { Text(stringResource(R.string.stop_training_dialog_title)) },
            text = { Text(stringResource(R.string.stop_training_dialog_desc)) },
            confirmButton = {
                TextButton(onClick = {
                    showStopConfirmation = false
                    onExit()
                }) {
                    Text(stringResource(R.string.stop_training))
                }
            },
            dismissButton = {
                TextButton(onClick = { showStopConfirmation = false }) {
                    Text(stringResource(R.string.continue_training))
                }
            }
        )
    }
}

@Composable
private fun formatNextStepLabel(label: String): String {
    if (label.isBlank()) return ""
    val workoutStr = stringResource(R.string.workout)
    val restStr = stringResource(R.string.rest)
    val finishStr = stringResource(R.string.training_finish)
    return when {
        label == "Training Finish" -> finishStr
        label.startsWith("Workout") -> label.replaceFirst("Workout", workoutStr)
        label.startsWith("Rest") -> label.replaceFirst("Rest", restStr)
        else -> label
    }
}

@Composable
private fun RoundStepper(
    totalWorkouts: Int,
    completedWorkouts: Int,
    phase: TimerPhase,
    workoutColorIndex: Int
) {
    val activeColor = if (workoutColorIndex % 2 == 0) {
        AppConfig.TimerScreen.workoutColorPrimary
    } else {
        AppConfig.TimerScreen.workoutColorSecondary
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (round in 1..totalWorkouts) {
            val isCompleted = round <= completedWorkouts
            val isCurrent = round == completedWorkouts + 1 && (phase == TimerPhase.Workout || phase == TimerPhase.Rest)

            val color = when {
                isCompleted -> AppConfig.TimerScreen.restColor
                isCurrent -> if (phase == TimerPhase.Workout) activeColor else AppConfig.TimerScreen.restColor
                else -> MaterialTheme.colorScheme.surfaceContainerHighest
            }

            Box(
                modifier = Modifier
                    .size(if (isCurrent) 12.dp else 9.dp)
                    .clip(CircleShape)
                    .background(color)
                    .then(
                        if (!isCompleted && !isCurrent) {
                            Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                        } else {
                            Modifier
                        }
                    )
            )
        }
    }
}

@Composable
private fun CountdownRing(
    remainingSeconds: Int,
    phaseDurationSeconds: Int,
    phase: TimerPhase,
    workoutColorIndex: Int,
    completionMessage: String,
    onTogglePause: () -> Unit,
    modifier: Modifier = Modifier
) {
    val targetProgress = if (phaseDurationSeconds > 0) {
        (remainingSeconds.toFloat() / phaseDurationSeconds.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 800, easing = LinearEasing),
        label = "CountdownRingProgress"
    )

    val activeWorkoutColor = if (workoutColorIndex % 2 == 0) {
        AppConfig.TimerScreen.workoutColorPrimary
    } else {
        AppConfig.TimerScreen.workoutColorSecondary
    }

    val showRing = phase == TimerPhase.Workout || phase == TimerPhase.Rest
    val ringColor = when (phase) {
        TimerPhase.Workout -> activeWorkoutColor
        TimerPhase.Rest -> AppConfig.TimerScreen.restColor
        TimerPhase.Complete -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.onBackground
    }
    val countdownFontSize = when {
        remainingSeconds >= 1000 -> 70.sp
        remainingSeconds >= 100 -> 88.sp
        else -> 112.sp
    }

    Box(
        modifier = modifier.clickable(
            enabled = showRing,
            onClick = onTogglePause
        ),
        contentAlignment = Alignment.Center
    ) {
        if (showRing) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = AppConfig.TimerScreen.ringStrokeWidth.toPx()
                val inset = strokeWidth / 2f
                val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                val topLeft = Offset(inset, inset)

                // Background track
                drawArc(
                    color = ringColor.copy(alpha = 0.18f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth)
                )

                // Active progress arc
                drawArc(
                    color = ringColor,
                    startAngle = -90f,
                    sweepAngle = animatedProgress * 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (phase) {
                TimerPhase.Complete -> {
                    Text(
                        text = stringResource(R.string.done_exclamation),
                        fontSize = 52.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = completionMessage,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }

                else -> {
                    Text(
                        text = remainingSeconds.toString(),
                        fontSize = countdownFontSize,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }
}

@Composable
private fun phaseText(phase: TimerPhase, isPaused: Boolean): String {
    return if (isPaused && (phase == TimerPhase.Workout || phase == TimerPhase.Rest)) {
        stringResource(R.string.phase_paused)
    } else {
        when (phase) {
            TimerPhase.Starting -> stringResource(R.string.phase_get_ready)
            TimerPhase.Workout -> stringResource(R.string.phase_workout)
            TimerPhase.Rest -> stringResource(R.string.phase_rest)
            TimerPhase.Complete -> stringResource(R.string.phase_finished)
            TimerPhase.Idle -> stringResource(R.string.phase_ready)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun SettingsScreen(
    settings: AppSettings,
    onSettingsChange: (AppSettings) -> Unit,
    onTestSound: () -> Unit,
    onTestVibration: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Language Selection
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.language_section),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(R.string.language_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppLanguage.entries.forEach { lang ->
                        val label = when (lang) {
                            AppLanguage.System -> stringResource(R.string.language_system)
                            AppLanguage.English -> stringResource(R.string.language_english)
                            AppLanguage.Polish -> stringResource(R.string.language_polish)
                        }
                        if (lang == settings.language) {
                            Button(
                                onClick = { onSettingsChange(settings.copy(language = lang)) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(label)
                            }
                        } else {
                            OutlinedButton(
                                onClick = { onSettingsChange(settings.copy(language = lang)) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(label)
                            }
                        }
                    }
                }
            }

            // Theme Mode Selection
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.appearance_section),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(R.string.appearance_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThemeMode.entries.forEach { mode ->
                        val label = when (mode) {
                            ThemeMode.System -> stringResource(R.string.theme_system)
                            ThemeMode.Light -> stringResource(R.string.theme_light)
                            ThemeMode.Dark -> stringResource(R.string.theme_dark)
                        }
                        if (mode == settings.themeMode) {
                            Button(
                                onClick = { onSettingsChange(settings.copy(themeMode = mode)) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(label)
                            }
                        } else {
                            OutlinedButton(
                                onClick = { onSettingsChange(settings.copy(themeMode = mode)) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(label)
                            }
                        }
                    }
                }
            }

            // Prep Time Selection
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.prep_time_section),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(R.string.prep_time_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppSettings.PREP_TIME_OPTIONS.forEach { seconds ->
                        val label = if (seconds == 0) stringResource(R.string.prep_none) else "${seconds}s"
                        FilterChip(
                            selected = settings.prepSeconds == seconds,
                            onClick = { onSettingsChange(settings.copy(prepSeconds = seconds)) },
                            label = { Text(label) }
                        )
                    }
                }
            }

            // Time Interval Selection
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.time_interval_section),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(R.string.time_interval_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                SimpleTimerStepperCard(
                    label = stringResource(R.string.time_interval_step_label),
                    valueText = "${settings.timeIntervalSeconds}s",
                    leadingIcon = {
                        Icon(
                            Icons.Default.Timer,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    onDecrement = {
                        val newVal = (settings.timeIntervalSeconds - 1).coerceAtLeast(AppSettings.MIN_TIME_INTERVAL_SECONDS)
                        onSettingsChange(settings.copy(timeIntervalSeconds = newVal))
                    },
                    onIncrement = {
                        val newVal = (settings.timeIntervalSeconds + 1).coerceAtMost(AppSettings.MAX_TIME_INTERVAL_SECONDS)
                        onSettingsChange(settings.copy(timeIntervalSeconds = newVal))
                    },
                    canDecrement = settings.timeIntervalSeconds > AppSettings.MIN_TIME_INTERVAL_SECONDS,
                    canIncrement = settings.timeIntervalSeconds < AppSettings.MAX_TIME_INTERVAL_SECONDS
                )
            }

            // Audio & Haptic Feedback
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.sound_vibration_section),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Sound switch, volume slider & test button
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                    Text(
                                        text = stringResource(R.string.sound_effects),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = stringResource(R.string.sound_effects_desc),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = settings.soundEnabled,
                                    onCheckedChange = { onSettingsChange(settings.copy(soundEnabled = it)) }
                                )
                            }

                            if (settings.soundEnabled) {
                                Column(
                                    modifier = Modifier.padding(top = 4.dp),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = stringResource(R.string.beep_volume),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "${(settings.soundVolume * 100).toInt()}%",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Slider(
                                        value = settings.soundVolume,
                                        onValueChange = { onSettingsChange(settings.copy(soundVolume = it)) },
                                        valueRange = 0.1f..1.0f,
                                        steps = 8
                                    )
                                }

                                OutlinedButton(
                                    onClick = onTestSound,
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(stringResource(R.string.test_sound), fontSize = 13.sp)
                                }
                            }
                        }

                        // Countdown sounds switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                Text(
                                    text = stringResource(R.string.countdown_sounds),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = stringResource(R.string.countdown_sounds_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = settings.countdownSoundsEnabled,
                                onCheckedChange = { onSettingsChange(settings.copy(countdownSoundsEnabled = it)) }
                            )
                        }

                        // Half-workout alert switch & rounding mode
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                    Text(
                                        text = stringResource(R.string.half_workout_warning),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = stringResource(R.string.half_workout_warning_desc),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = settings.halfWorkoutWarningEnabled,
                                    onCheckedChange = { onSettingsChange(settings.copy(halfWorkoutWarningEnabled = it)) }
                                )
                            }

                            if (settings.halfWorkoutWarningEnabled) {
                                Column(
                                    modifier = Modifier.padding(top = 4.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.half_time_rounding),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        HalfTimeRoundingMode.entries.forEach { mode ->
                                            val label = when (mode) {
                                                HalfTimeRoundingMode.Down -> stringResource(R.string.rounding_down)
                                                HalfTimeRoundingMode.Up -> stringResource(R.string.rounding_up)
                                            }
                                            if (mode == settings.halfTimeRoundingMode) {
                                                Button(
                                                    onClick = { onSettingsChange(settings.copy(halfTimeRoundingMode = mode)) },
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text(label)
                                                }
                                            } else {
                                                OutlinedButton(
                                                    onClick = { onSettingsChange(settings.copy(halfTimeRoundingMode = mode)) },
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text(label)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Vibration switch & test button
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                    Text(
                                        text = stringResource(R.string.haptic_feedback),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = stringResource(R.string.haptic_feedback_desc),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = settings.hapticFeedbackEnabled,
                                    onCheckedChange = { onSettingsChange(settings.copy(hapticFeedbackEnabled = it)) }
                                )
                            }
                            if (settings.hapticFeedbackEnabled) {
                                OutlinedButton(
                                    onClick = onTestVibration,
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Icon(Icons.Default.Vibration, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(stringResource(R.string.test_vibration), fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Workout Display Options
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.workout_screen_section),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                Text(
                                    text = stringResource(R.string.show_total_remaining),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = stringResource(R.string.show_total_remaining_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = settings.showTotalRemainingTime,
                                onCheckedChange = { onSettingsChange(settings.copy(showTotalRemainingTime = it)) }
                            )
                        }
                    }
                }
            }

            // About App Section
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.about_section),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = AppConfig.Metadata.appName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            AssistChip(
                                onClick = {},
                                label = {
                                    Text(
                                        stringResource(R.string.app_version, AppConfig.Metadata.versionName),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            )
                        }
                        Text(
                            text = stringResource(R.string.app_description),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = stringResource(R.string.app_author, AppConfig.Metadata.author),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ActiveTimerScreenPreview() {
    MaterialTheme {
        ActiveTimerScreen(
            title = "HIIT Advanced Routine",
            state = TimerUiState(
                phase = TimerPhase.Workout,
                remainingSeconds = 18,
                phaseDurationSeconds = 30,
                completedWorkouts = 2,
                totalWorkouts = 8,
                totalRemainingSeconds = 240,
                workoutColorIndex = 0,
                stepLabel = "Pushups",
                nextStepLabel = "Rest (15s)",
                isActive = true
            ),
            soundEnabled = true,
            showTotalRemainingTime = true,
            onToggleSound = {},
            onPause = {},
            onResume = {},
            onExit = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TimerHomeScreenPreview() {
    MaterialTheme {
        TimerHomeScreen(
            presets = listOf(
                TimerPreset(
                    id = 1L,
                    name = "Full Body Tabata",
                    description = "High intensity interval workout",
                    plan = TrainingPlan.Simple(TimerConfig(20, 10, 8))
                )
            ),
            soundEnabled = true,
            onToggleSound = {},
            onNewPreset = {},
            onTemporaryTimer = {},
            onStartPreset = {},
            onEditPreset = {},
            onDeletePreset = {},
            onOpenSettings = {}
        )
    }
}
