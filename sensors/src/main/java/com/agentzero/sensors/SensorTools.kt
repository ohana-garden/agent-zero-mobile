package com.agentzero.sensors

import com.agentzero.core.AgentTool
import com.agentzero.core.ToolResult
import com.agentzero.core.ToolSchema
import com.agentzero.core.ParameterSchema
import com.agentzero.core.ToolExample
import kotlinx.coroutines.delay
import timber.log.Timber
import kotlin.math.*

/**
 * Heart rate monitoring tool for health agents
 */
class HeartRateTool(
    private val sensorManager: SensorToolManager
) : AgentTool(
    name = "heart_rate",
    description = "Get current heart rate and heart rate variability data",
    requiredPermissions = listOf("android.permission.BODY_SENSORS")
) {
    
    override suspend fun execute(parameters: Map<String, Any>): ToolResult {
        return try {
            val timeRange = parameters["time_range"] as? String ?: "current"
            
            when (timeRange) {
                "current" -> {
                    val reading = sensorManager.getCurrentReading("heart_rate")
                    if (reading != null) {
                        ToolResult.success(mapOf(
                            "heart_rate_bpm" to reading.values["bpm"],
                            "timestamp" to reading.timestamp,
                            "accuracy" to reading.values["accuracy"]
                        ))
                    } else {
                        ToolResult.error("No heart rate data available")
                    }
                }
                "recent" -> {
                    val readings = sensorManager.getRecentData("heart_rate", 3600000) // 1 hour
                    val analysis = analyzeHeartRateData(readings)
                    ToolResult.success(analysis)
                }
                else -> ToolResult.error("Invalid time_range: $timeRange")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting heart rate data")
            ToolResult.error("Failed to get heart rate data: ${e.message}")
        }
    }
    
    private fun analyzeHeartRateData(readings: List<SensorReading>): Map<String, Any> {
        if (readings.isEmpty()) {
            return mapOf("error" to "No heart rate data available")
        }
        
        val heartRates = readings.mapNotNull { it.values["bpm"] }
        
        return mapOf(
            "current_bpm" to heartRates.lastOrNull(),
            "average_bpm" to heartRates.average(),
            "min_bpm" to heartRates.minOrNull(),
            "max_bpm" to heartRates.maxOrNull(),
            "heart_rate_variability" to calculateHRV(heartRates),
            "readings_count" to readings.size,
            "time_span_minutes" to ((readings.last().timestamp - readings.first().timestamp) / 60000)
        )
    }
    
    private fun calculateHRV(heartRates: List<Double>): Double {
        if (heartRates.size < 2) return 0.0
        
        val intervals = heartRates.zipWithNext { a, b -> abs(b - a) }
        return intervals.average()
    }
    
    override fun getSchema(): ToolSchema {
        return ToolSchema(
            name = name,
            description = description,
            parameters = listOf(
                ParameterSchema(
                    name = "time_range",
                    type = "string",
                    description = "Time range for heart rate data",
                    required = false,
                    defaultValue = "current",
                    allowedValues = listOf("current", "recent")
                )
            ),
            returnType = "object",
            examples = listOf(
                ToolExample(
                    description = "Get current heart rate",
                    parameters = mapOf("time_range" to "current"),
                    expectedResult = "Current heart rate in BPM with accuracy"
                )
            )
        )
    }
}

/**
 * Step counting and activity tracking tool
 */
class StepCounterTool(
    private val sensorManager: SensorToolManager
) : AgentTool(
    name = "step_counter",
    description = "Get step count and walking activity data",
    requiredPermissions = listOf("android.permission.ACTIVITY_RECOGNITION")
) {
    
    override suspend fun execute(parameters: Map<String, Any>): ToolResult {
        return try {
            val timeRange = parameters["time_range"] as? String ?: "today"
            
            when (timeRange) {
                "current" -> {
                    val reading = sensorManager.getCurrentReading("step_counter")
                    if (reading != null) {
                        ToolResult.success(mapOf(
                            "total_steps" to reading.values["steps"],
                            "timestamp" to reading.timestamp
                        ))
                    } else {
                        ToolResult.error("No step counter data available")
                    }
                }
                "today" -> {
                    val readings = sensorManager.getRecentData("step_counter", 86400000) // 24 hours
                    val analysis = analyzeStepData(readings)
                    ToolResult.success(analysis)
                }
                else -> ToolResult.error("Invalid time_range: $timeRange")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting step counter data")
            ToolResult.error("Failed to get step counter data: ${e.message}")
        }
    }
    
    private fun analyzeStepData(readings: List<SensorReading>): Map<String, Any> {
        if (readings.isEmpty()) {
            return mapOf("error" to "No step data available")
        }
        
        val steps = readings.mapNotNull { it.values["steps"] }
        val currentSteps = steps.lastOrNull() ?: 0.0
        val startSteps = steps.firstOrNull() ?: 0.0
        val stepsToday = currentSteps - startSteps
        
        return mapOf(
            "steps_today" to stepsToday,
            "total_steps" to currentSteps,
            "estimated_distance_km" to (stepsToday * 0.0008), // Rough estimate
            "estimated_calories" to (stepsToday * 0.04), // Rough estimate
            "active_periods" to identifyActivePeriods(readings)
        )
    }
    
    private fun identifyActivePeriods(readings: List<SensorReading>): List<Map<String, Any>> {
        // Simple activity period detection
        val periods = mutableListOf<Map<String, Any>>()
        
        for (i in 1 until readings.size) {
            val prev = readings[i-1].values["steps"] ?: 0.0
            val curr = readings[i].values["steps"] ?: 0.0
            val stepDiff = curr - prev
            
            if (stepDiff > 10) { // Active period threshold
                periods.add(mapOf(
                    "timestamp" to readings[i].timestamp,
                    "steps_in_period" to stepDiff,
                    "activity_level" to when {
                        stepDiff > 100 -> "high"
                        stepDiff > 50 -> "medium"
                        else -> "low"
                    }
                ))
            }
        }
        
        return periods
    }
    
    override fun getSchema(): ToolSchema {
        return ToolSchema(
            name = name,
            description = description,
            parameters = listOf(
                ParameterSchema(
                    name = "time_range",
                    type = "string",
                    description = "Time range for step data",
                    required = false,
                    defaultValue = "today",
                    allowedValues = listOf("current", "today")
                )
            ),
            returnType = "object"
        )
    }
}

/**
 * Accelerometer-based motion analysis tool
 */
class AccelerometerTool(
    private val sensorManager: SensorToolManager
) : AgentTool(
    name = "accelerometer",
    description = "Analyze device motion and orientation using accelerometer data"
) {
    
    override suspend fun execute(parameters: Map<String, Any>): ToolResult {
        return try {
            val duration = parameters["duration_seconds"] as? Int ?: 5
            val analysis = collectAndAnalyzeMotion(duration)
            ToolResult.success(analysis)
        } catch (e: Exception) {
            Timber.e(e, "Error analyzing accelerometer data")
            ToolResult.error("Failed to analyze motion: ${e.message}")
        }
    }
    
    private suspend fun collectAndAnalyzeMotion(durationSeconds: Int): Map<String, Any> {
        val readings = mutableListOf<SensorReading>()
        val startTime = System.currentTimeMillis()
        
        // Collect data for specified duration
        while (System.currentTimeMillis() - startTime < durationSeconds * 1000) {
            sensorManager.getCurrentReading("accelerometer")?.let { reading ->
                readings.add(reading)
            }
            delay(100) // Sample every 100ms
        }
        
        if (readings.isEmpty()) {
            return mapOf("error" to "No accelerometer data collected")
        }
        
        return analyzeMotionData(readings)
    }
    
    private fun analyzeMotionData(readings: List<SensorReading>): Map<String, Any> {
        val magnitudes = readings.map { reading ->
            val x = reading.values["x"] ?: 0.0
            val y = reading.values["y"] ?: 0.0
            val z = reading.values["z"] ?: 0.0
            sqrt(x*x + y*y + z*z)
        }
        
        val avgMagnitude = magnitudes.average()
        val variance = magnitudes.map { (it - avgMagnitude).pow(2) }.average()
        val stdDev = sqrt(variance)
        
        val motionLevel = when {
            avgMagnitude < 2.0 -> "stationary"
            avgMagnitude < 8.0 -> "light_movement"
            avgMagnitude < 15.0 -> "moderate_movement"
            else -> "vigorous_movement"
        }
        
        return mapOf(
            "motion_level" to motionLevel,
            "average_magnitude" to avgMagnitude,
            "motion_variability" to stdDev,
            "peak_magnitude" to magnitudes.maxOrNull(),
            "samples_analyzed" to readings.size,
            "dominant_activity" to classifyActivity(magnitudes)
        )
    }
    
    private fun classifyActivity(magnitudes: List<Double>): String {
        val avgMagnitude = magnitudes.average()
        val variance = magnitudes.map { (it - avgMagnitude).pow(2) }.average()
        
        return when {
            avgMagnitude < 2.0 && variance < 1.0 -> "resting"
            avgMagnitude < 5.0 && variance < 2.0 -> "walking"
            avgMagnitude < 10.0 && variance > 3.0 -> "running"
            variance > 5.0 -> "irregular_movement"
            else -> "unknown_activity"
        }
    }
    
    override fun getSchema(): ToolSchema {
        return ToolSchema(
            name = name,
            description = description,
            parameters = listOf(
                ParameterSchema(
                    name = "duration_seconds",
                    type = "number",
                    description = "Duration to collect motion data in seconds",
                    required = false,
                    defaultValue = "5"
                )
            ),
            returnType = "object"
        )
    }
}

/**
 * Activity recognition tool using multiple sensors
 */
class ActivityRecognitionTool(
    private val sensorManager: SensorToolManager
) : AgentTool(
    name = "activity_recognition",
    description = "Recognize current user activity (walking, running, sitting, etc.)"
) {
    
    override suspend fun execute(parameters: Map<String, Any>): ToolResult {
        return try {
            val confidence = parameters["min_confidence"] as? Double ?: 0.6
            val activity = recognizeCurrentActivity(confidence)
            ToolResult.success(activity)
        } catch (e: Exception) {
            Timber.e(e, "Error recognizing activity")
            ToolResult.error("Failed to recognize activity: ${e.message}")
        }
    }
    
    private suspend fun recognizeCurrentActivity(minConfidence: Double): Map<String, Any> {
        // Get recent activity data
        val activityReadings = sensorManager.getRecentData("activity", 30000) // 30 seconds
        
        if (activityReadings.isEmpty()) {
            return mapOf(
                "activity" to "unknown",
                "confidence" to 0.0,
                "reason" to "No sensor data available"
            )
        }
        
        // Analyze recent activities
        val activities = activityReadings.mapNotNull { reading ->
            val activity = reading.values["activity"]?.toString()
            val confidence = reading.values["confidence"] ?: 0.0
            if (activity != null) Pair(activity, confidence) else null
        }
        
        // Find most common activity with sufficient confidence
        val activityCounts = activities.groupBy { it.first }
            .mapValues { (_, pairs) -> 
                pairs.map { it.second }.average()
            }
        
        val bestActivity = activityCounts.maxByOrNull { it.value }
        
        return if (bestActivity != null && bestActivity.value >= minConfidence) {
            mapOf(
                "activity" to bestActivity.key,
                "confidence" to bestActivity.value,
                "duration_seconds" to (activityReadings.size * 1.0), // Rough estimate
                "alternatives" to activityCounts.filter { it.key != bestActivity.key }
            )
        } else {
            mapOf(
                "activity" to "uncertain",
                "confidence" to (bestActivity?.value ?: 0.0),
                "reason" to "Confidence below threshold"
            )
        }
    }
    
    override fun getSchema(): ToolSchema {
        return ToolSchema(
            name = name,
            description = description,
            parameters = listOf(
                ParameterSchema(
                    name = "min_confidence",
                    type = "number",
                    description = "Minimum confidence threshold for activity recognition",
                    required = false,
                    defaultValue = "0.6"
                )
            ),
            returnType = "object"
        )
    }
}

/**
 * Sleep analysis tool using motion and heart rate data
 */
class SleepAnalysisTool(
    private val sensorManager: SensorToolManager
) : AgentTool(
    name = "sleep_analysis",
    description = "Analyze sleep patterns using sensor data"
) {
    
    override suspend fun execute(parameters: Map<String, Any>): ToolResult {
        return try {
            val hoursBack = parameters["hours_back"] as? Int ?: 8
            val analysis = analyzeSleepData(hoursBack)
            ToolResult.success(analysis)
        } catch (e: Exception) {
            Timber.e(e, "Error analyzing sleep data")
            ToolResult.error("Failed to analyze sleep: ${e.message}")
        }
    }
    
    private suspend fun analyzeSleepData(hoursBack: Int): Map<String, Any> {
        val timeRangeMs = hoursBack * 3600000L
        
        // Get motion and heart rate data
        val motionData = sensorManager.getRecentData("activity", timeRangeMs)
        val heartRateData = sensorManager.getRecentData("heart_rate", timeRangeMs)
        
        if (motionData.isEmpty() && heartRateData.isEmpty()) {
            return mapOf("error" to "No sleep-related sensor data available")
        }
        
        // Analyze sleep patterns
        val sleepPeriods = identifySleepPeriods(motionData, heartRateData)
        val sleepQuality = calculateSleepQuality(sleepPeriods, heartRateData)
        
        return mapOf(
            "sleep_periods" to sleepPeriods,
            "total_sleep_hours" to (sleepPeriods.sumOf { it["duration_minutes"] as? Double ?: 0.0 } / 60.0),
            "sleep_quality_score" to sleepQuality,
            "sleep_efficiency" to calculateSleepEfficiency(sleepPeriods, hoursBack),
            "recommendations" to generateSleepRecommendations(sleepQuality, sleepPeriods)
        )
    }
    
    private fun identifySleepPeriods(
        motionData: List<SensorReading>,
        heartRateData: List<SensorReading>
    ): List<Map<String, Any>> {
        // Simple sleep detection based on low motion periods
        val sleepPeriods = mutableListOf<Map<String, Any>>()
        
        // Group motion data into 30-minute windows
        val windowSize = 30 * 60 * 1000L // 30 minutes in ms
        val windows = motionData.groupBy { it.timestamp / windowSize }
        
        windows.forEach { (windowStart, readings) ->
            val avgActivity = readings.mapNotNull { 
                it.values["confidence"]?.takeIf { confidence -> 
                    it.values["activity"] == "stationary" 
                }
            }.average()
            
            if (avgActivity > 0.7) { // High confidence of being stationary
                sleepPeriods.add(mapOf(
                    "start_time" to (windowStart * windowSize),
                    "duration_minutes" to 30,
                    "sleep_stage" to "light_sleep", // Simplified
                    "confidence" to avgActivity
                ))
            }
        }
        
        return sleepPeriods
    }
    
    private fun calculateSleepQuality(
        sleepPeriods: List<Map<String, Any>>,
        heartRateData: List<SensorReading>
    ): Double {
        if (sleepPeriods.isEmpty()) return 0.0
        
        val totalSleepMinutes = sleepPeriods.sumOf { it["duration_minutes"] as? Double ?: 0.0 }
        val avgConfidence = sleepPeriods.mapNotNull { it["confidence"] as? Double }.average()
        
        // Factor in heart rate variability during sleep
        val sleepHR = heartRateData.filter { reading ->
            sleepPeriods.any { period ->
                val startTime = period["start_time"] as? Long ?: 0L
                val duration = (period["duration_minutes"] as? Double ?: 0.0) * 60 * 1000
                reading.timestamp in startTime..(startTime + duration.toLong())
            }
        }
        
        val hrVariability = if (sleepHR.isNotEmpty()) {
            val hrs = sleepHR.mapNotNull { it.values["bpm"] }
            val avg = hrs.average()
            hrs.map { abs(it - avg) }.average()
        } else 0.0
        
        // Simple quality score (0-1)
        val durationScore = minOf(totalSleepMinutes / 480.0, 1.0) // 8 hours ideal
        val stabilityScore = avgConfidence
        val hrScore = 1.0 - minOf(hrVariability / 20.0, 1.0) // Lower variability = better
        
        return (durationScore + stabilityScore + hrScore) / 3.0
    }
    
    private fun calculateSleepEfficiency(sleepPeriods: List<Map<String, Any>>, totalHours: Int): Double {
        val totalSleepMinutes = sleepPeriods.sumOf { it["duration_minutes"] as? Double ?: 0.0 }
        val totalTimeMinutes = totalHours * 60.0
        return totalSleepMinutes / totalTimeMinutes
    }
    
    private fun generateSleepRecommendations(
        quality: Double,
        sleepPeriods: List<Map<String, Any>>
    ): List<String> {
        val recommendations = mutableListOf<String>()
        
        when {
            quality < 0.3 -> {
                recommendations.add("Consider improving sleep environment (darkness, temperature, noise)")
                recommendations.add("Try to maintain consistent sleep schedule")
                recommendations.add("Limit screen time before bed")
            }
            quality < 0.6 -> {
                recommendations.add("Your sleep quality is moderate - consider relaxation techniques before bed")
                recommendations.add("Ensure adequate sleep duration (7-9 hours)")
            }
            else -> {
                recommendations.add("Great sleep quality! Keep maintaining your current sleep habits")
            }
        }
        
        return recommendations
    }
    
    override fun getSchema(): ToolSchema {
        return ToolSchema(
            name = name,
            description = description,
            parameters = listOf(
                ParameterSchema(
                    name = "hours_back",
                    type = "number",
                    description = "Number of hours back to analyze for sleep data",
                    required = false,
                    defaultValue = "8"
                )
            ),
            returnType = "object"
        )
    }
}

/**
 * Stress detection tool using heart rate variability and motion patterns
 */
class StressDetectionTool(
    private val sensorManager: SensorToolManager
) : AgentTool(
    name = "stress_detection",
    description = "Detect stress levels using physiological and behavioral indicators"
) {
    
    override suspend fun execute(parameters: Map<String, Any>): ToolResult {
        return try {
            val timeRangeMinutes = parameters["time_range_minutes"] as? Int ?: 30
            val analysis = analyzeStressIndicators(timeRangeMinutes)
            ToolResult.success(analysis)
        } catch (e: Exception) {
            Timber.e(e, "Error detecting stress")
            ToolResult.error("Failed to detect stress: ${e.message}")
        }
    }
    
    private suspend fun analyzeStressIndicators(timeRangeMinutes: Int): Map<String, Any> {
        val timeRangeMs = timeRangeMinutes * 60 * 1000L
        
        // Get physiological data
        val heartRateData = sensorManager.getRecentData("heart_rate", timeRangeMs)
        val motionData = sensorManager.getRecentData("activity", timeRangeMs)
        
        if (heartRateData.isEmpty() && motionData.isEmpty()) {
            return mapOf("error" to "No stress-related sensor data available")
        }
        
        // Analyze stress indicators
        val hrStress = analyzeHeartRateStress(heartRateData)
        val motionStress = analyzeMotionStress(motionData)
        
        val overallStressLevel = combineStressIndicators(hrStress, motionStress)
        
        return mapOf(
            "stress_level" to overallStressLevel["level"],
            "confidence" to overallStressLevel["confidence"],
            "heart_rate_indicators" to hrStress,
            "motion_indicators" to motionStress,
            "recommendations" to generateStressRecommendations(overallStressLevel["level"] as String),
            "analysis_period_minutes" to timeRangeMinutes
        )
    }
    
    private fun analyzeHeartRateStress(heartRateData: List<SensorReading>): Map<String, Any> {
        if (heartRateData.isEmpty()) {
            return mapOf("available" to false)
        }
        
        val heartRates = heartRateData.mapNotNull { it.values["bpm"] }
        val avgHR = heartRates.average()
        val maxHR = heartRates.maxOrNull() ?: 0.0
        val minHR = heartRates.minOrNull() ?: 0.0
        
        // Calculate heart rate variability (simplified)
        val hrv = if (heartRates.size > 1) {
            heartRates.zipWithNext { a, b -> abs(b - a) }.average()
        } else 0.0
        
        // Stress indicators from HR data
        val elevatedHR = avgHR > 80 // Simplified threshold
        val lowHRV = hrv < 5 // Low variability can indicate stress
        val highVariability = (maxHR - minHR) > 30
        
        val stressScore = listOf(elevatedHR, lowHRV, highVariability).count { it } / 3.0
        
        return mapOf(
            "available" to true,
            "average_hr" to avgHR,
            "hr_variability" to hrv,
            "stress_score" to stressScore,
            "indicators" to mapOf(
                "elevated_heart_rate" to elevatedHR,
                "low_hrv" to lowHRV,
                "high_variability" to highVariability
            )
        )
    }
    
    private fun analyzeMotionStress(motionData: List<SensorReading>): Map<String, Any> {
        if (motionData.isEmpty()) {
            return mapOf("available" to false)
        }
        
        // Analyze motion patterns for stress indicators
        val activities = motionData.mapNotNull { it.values["activity"]?.toString() }
        val restlessness = activities.count { it in listOf("irregular_movement", "fidgeting") } / activities.size.toDouble()
        val inactivity = activities.count { it == "stationary" } / activities.size.toDouble()
        
        // High restlessness or excessive inactivity can indicate stress
        val stressScore = when {
            restlessness > 0.3 -> 0.8 // High restlessness
            inactivity > 0.8 -> 0.6 // Excessive inactivity
            else -> 0.2
        }
        
        return mapOf(
            "available" to true,
            "restlessness_score" to restlessness,
            "inactivity_score" to inactivity,
            "stress_score" to stressScore,
            "dominant_activity" to (activities.groupBy { it }.maxByOrNull { it.value.size }?.key ?: "unknown")
        )
    }
    
    private fun combineStressIndicators(
        hrStress: Map<String, Any>,
        motionStress: Map<String, Any>
    ): Map<String, Any> {
        val hrAvailable = hrStress["available"] as? Boolean ?: false
        val motionAvailable = motionStress["available"] as? Boolean ?: false
        
        if (!hrAvailable && !motionAvailable) {
            return mapOf(
                "level" to "unknown",
                "confidence" to 0.0
            )
        }
        
        val hrScore = if (hrAvailable) hrStress["stress_score"] as? Double ?: 0.0 else 0.0
        val motionScore = if (motionAvailable) motionStress["stress_score"] as? Double ?: 0.0 else 0.0
        
        val combinedScore = if (hrAvailable && motionAvailable) {
            (hrScore + motionScore) / 2.0
        } else {
            maxOf(hrScore, motionScore)
        }
        
        val stressLevel = when {
            combinedScore < 0.3 -> "low"
            combinedScore < 0.6 -> "moderate"
            else -> "high"
        }
        
        val confidence = if (hrAvailable && motionAvailable) 0.8 else 0.6
        
        return mapOf(
            "level" to stressLevel,
            "confidence" to confidence,
            "score" to combinedScore
        )
    }
    
    private fun generateStressRecommendations(stressLevel: String): List<String> {
        return when (stressLevel) {
            "high" -> listOf(
                "Consider taking a break and practicing deep breathing",
                "Try a short walk or light physical activity",
                "Consider meditation or mindfulness exercises",
                "Ensure adequate hydration and nutrition"
            )
            "moderate" -> listOf(
                "Take a few minutes for relaxation",
                "Consider gentle stretching or movement",
                "Practice mindful breathing"
            )
            "low" -> listOf(
                "Great! Your stress levels appear to be well-managed",
                "Continue with your current stress management practices"
            )
            else -> listOf(
                "Unable to determine stress level - ensure sensors are working properly"
            )
        }
    }
    
    override fun getSchema(): ToolSchema {
        return ToolSchema(
            name = name,
            description = description,
            parameters = listOf(
                ParameterSchema(
                    name = "time_range_minutes",
                    type = "number",
                    description = "Time range in minutes to analyze for stress indicators",
                    required = false,
                    defaultValue = "30"
                )
            ),
            returnType = "object"
        )
    }
}