package com.johnykvsky.jktimer.storage

import android.content.Context
import com.johnykvsky.jktimer.R
import com.johnykvsky.jktimer.model.StepType
import com.johnykvsky.jktimer.model.TimerConfig
import com.johnykvsky.jktimer.model.TimerPreset
import com.johnykvsky.jktimer.model.TimerStep
import com.johnykvsky.jktimer.model.TrainingPlan
import org.json.JSONArray
import org.json.JSONObject
import java.util.Base64
import java.util.UUID

class TimerPresetRepository(private val context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun loadPresets(): List<TimerPreset> {
        val jsonStr = preferences.getString(KEY_PRESETS_JSON, null)
        if (jsonStr != null) {
            val presets = parseJsonPresets(jsonStr).sortedBy { it.name.lowercase() }
            if (presets.isNotEmpty() || preferences.getBoolean(KEY_INITIALIZED, false)) {
                return presets
            }
        }

        val stored = preferences.getStringSet(KEY_PRESETS_LEGACY, null).orEmpty()
        val legacyPresets = stored.mapNotNull(::decodePresetLegacy).sortedBy { it.name.lowercase() }

        if (legacyPresets.isNotEmpty()) {
            persist(legacyPresets)
            return legacyPresets
        }
        if (preferences.getBoolean(KEY_INITIALIZED, false)) return emptyList()

        val defaultName = try {
            context.getString(R.string.default_training)
        } catch (_: Exception) {
            "Timer-Monday"
        }

        val defaultPreset = TimerPreset(
            id = System.currentTimeMillis(),
            name = defaultName,
            description = "",
            config = TimerConfig(workoutSeconds = 40, restSeconds = 20, repeats = 3)
        )
        persist(listOf(defaultPreset))
        return listOf(defaultPreset)
    }

    fun savePreset(
        existingId: Long?,
        name: String,
        description: String,
        plan: TrainingPlan
    ): List<TimerPreset> {
        val current = loadPresets()
        val preset = TimerPreset(
            id = existingId ?: System.currentTimeMillis(),
            name = name.trim(),
            description = description.trim(),
            plan = plan
        )
        val updated = (current.filterNot { it.id == preset.id } + preset)
            .sortedBy { it.name.lowercase() }
        persist(updated)
        return updated
    }

    fun savePreset(
        existingId: Long?,
        name: String,
        description: String,
        config: TimerConfig
    ): List<TimerPreset> = savePreset(existingId, name, description, TrainingPlan.Simple(config))

    fun deletePreset(id: Long): List<TimerPreset> {
        val updated = loadPresets().filterNot { it.id == id }
        persist(updated)
        return updated
    }

    private fun persist(presets: List<TimerPreset>) {
        val jsonArray = JSONArray()
        for (p in presets) {
            val obj = JSONObject().apply {
                put("id", p.id)
                put("name", p.name)
                put("description", p.description)
                when (val plan = p.plan) {
                    is TrainingPlan.Simple -> {
                        put("type", "simple")
                        put("workoutSeconds", plan.config.workoutSeconds)
                        put("restSeconds", plan.config.restSeconds)
                        put("repeats", plan.config.repeats)
                    }
                    is TrainingPlan.Advanced -> {
                        put("type", "advanced")
                        val stepsArray = JSONArray()
                        for (step in plan.steps) {
                            val stepObj = JSONObject().apply {
                                put("id", step.id)
                                put("type", if (step.type == StepType.Workout) "workout" else "rest")
                                put("durationSeconds", step.durationSeconds)
                                put("name", if (step.type == StepType.Workout) step.name else "")
                            }
                            stepsArray.put(stepObj)
                        }
                        put("steps", stepsArray)
                    }
                }
            }
            jsonArray.put(obj)
        }
        preferences.edit()
            .putString(KEY_PRESETS_JSON, jsonArray.toString())
            .putBoolean(KEY_INITIALIZED, true)
            .apply()
    }

    private fun parseJsonPresets(json: String): List<TimerPreset> {
        return runCatching {
            val array = JSONArray(json)
            val list = mutableListOf<TimerPreset>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val id = obj.getLong("id")
                val name = obj.getString("name")
                val description = obj.optString("description", "")
                val type = obj.optString("type", "simple")

                val plan: TrainingPlan? = if (type == "advanced" && obj.has("steps")) {
                    val stepsArray = obj.getJSONArray("steps")
                    val stepsList = mutableListOf<TimerStep>()
                    for (j in 0 until stepsArray.length()) {
                        val stepObj = stepsArray.getJSONObject(j)
                        val stepTypeStr = stepObj.optString("type", "workout")
                        val stepType = if (stepTypeStr.equals("rest", ignoreCase = true)) StepType.Rest else StepType.Workout
                        val duration = stepObj.optInt("durationSeconds", stepObj.optInt("duration", 30))
                        val stepName = if (stepType == StepType.Workout) stepObj.optString("name", "") else ""
                        val stepId = stepObj.optString("id", UUID.randomUUID().toString())
                        stepsList.add(
                            TimerStep(
                                id = stepId,
                                type = stepType,
                                durationSeconds = duration,
                                name = stepName
                            )
                        )
                    }
                    TrainingPlan.Advanced(stepsList).takeIf { it.isValid() }
                } else if (obj.has("workoutSeconds")) {
                    val config = TimerConfig(
                        workoutSeconds = obj.getInt("workoutSeconds"),
                        restSeconds = obj.getInt("restSeconds"),
                        repeats = obj.getInt("repeats")
                    )
                    if (config.isValid()) TrainingPlan.Simple(config) else null
                } else {
                    null
                }

                if (plan != null && name.isNotBlank()) {
                    list.add(
                        TimerPreset(
                            id = id,
                            name = name,
                            description = description,
                            plan = plan
                        )
                    )
                }
            }
            list
        }.getOrDefault(emptyList())
    }

    private fun decodePresetLegacy(value: String): TimerPreset? {
        val parts = value.split("|")
        if (parts.size != 5 && parts.size != 6) return null

        return runCatching {
            val name = String(Base64.getDecoder().decode(parts[1]), Charsets.UTF_8)
            val description = if (parts.size == 6) {
                String(Base64.getDecoder().decode(parts[2]), Charsets.UTF_8)
            } else {
                ""
            }
            val configStart = if (parts.size == 6) 3 else 2
            val config = TimerConfig(
                workoutSeconds = parts[configStart].toInt(),
                restSeconds = parts[configStart + 1].toInt(),
                repeats = parts[configStart + 2].toInt()
            )
            TimerPreset(
                id = parts[0].toLong(),
                name = name,
                description = description,
                plan = TrainingPlan.Simple(config)
            )
        }.getOrNull()?.takeIf { it.name.isNotBlank() && it.plan.isValid() }
    }

    private companion object {
        const val PREFERENCES_NAME = "timer_presets"
        const val KEY_PRESETS_JSON = "presets_json"
        const val KEY_PRESETS_LEGACY = "presets"
        const val KEY_INITIALIZED = "initialized"
    }
}
