package com.abubakr.taskstreak.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class HealthMetrics(
    val stepsToday: Int = 6420,
    val targetSteps: Int = 10000,
    val activeMinutes: Int = 42,
    val sleepHours: Float = 7.5f,
    val restingHeartRate: Int = 64,
    val isFitnessHabitAutoCompletable: Boolean = true
)

class HealthConnectSyncBridge(context: Context) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

    private val _healthData = MutableStateFlow(HealthMetrics())
    val healthData: StateFlow<HealthMetrics> = _healthData.asStateFlow()

    fun startStepTracking() {
        stepSensor?.let { sensor ->
            sensorManager?.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stopStepTracking() {
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            val totalSteps = event.values[0].toInt()
            val current = _healthData.value
            _healthData.value = current.copy(stepsToday = totalSteps)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    /**
     * Checks if a fitness habit (e.g. "10,000 steps" or "Workout") has met health threshold
     */
    fun checkAutoCompletionEligibility(habitTitle: String): Boolean {
        val titleLower = habitTitle.lowercase()
        val currentSteps = _healthData.value.stepsToday
        return when {
            titleLower.contains("step") || titleLower.contains("walk") || titleLower.contains("مشي") -> {
                currentSteps >= 5000
            }
            titleLower.contains("exercise") || titleLower.contains("workout") || titleLower.contains("رياضة") -> {
                _healthData.value.activeMinutes >= 30
            }
            titleLower.contains("sleep") || titleLower.contains("نوم") -> {
                _healthData.value.sleepHours >= 7.0f
            }
            else -> false
        }
    }
}
