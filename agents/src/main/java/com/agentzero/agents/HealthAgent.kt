package com.agentzero.agents

import com.agentzero.core.*
import com.agentzero.sensors.*
import timber.log.Timber

/**
 * Health-focused AI agent
 * 
 * Specializes in:
 * - Heart rate monitoring and analysis
 * - Sleep pattern analysis
 * - Stress detection and management
 * - Health trend identification
 * - Wellness recommendations
 */
class HealthAgent(
    name: String,
    voiceProfile: VoiceProfile
) : Agent(
    name = name,
    type = "health",
    voiceProfile = voiceProfile
) {
    
    override val tools: List<AgentTool> = listOf(
        // Will be populated with sensor tools during initialization
    )
    
    override val personality = AgentPersonality(
        traits = mapOf(
            "empathy" to 0.9f,
            "professionalism" to 0.8f,
            "supportiveness" to 0.9f,
            "analytical" to 0.7f
        ),
        communicationStyle = "caring_professional",
        responseLength = "medium",
        expertise = listOf(
            "cardiovascular_health",
            "sleep_analysis", 
            "stress_management",
            "wellness_coaching",
            "health_monitoring"
        ),
        limitations = listOf(
            "Not a replacement for professional medical advice",
            "Cannot diagnose medical conditions",
            "Recommendations are general wellness guidance"
        )
    )
    
    override val knowledgeDomains = listOf(
        "health",
        "wellness", 
        "cardiology",
        "sleep_science",
        "stress_physiology",
        "preventive_medicine"
    )
    
    override suspend fun generateResponse(input: String, context: AgentContext): String {
        val inputLower = input.lowercase()
        
        return when {
            // Heart rate related queries
            inputLower.contains("heart") || inputLower.contains("pulse") || inputLower.contains("bpm") -> {
                handleHeartRateQuery(input, context)
            }
            
            // Sleep related queries
            inputLower.contains("sleep") || inputLower.contains("tired") || inputLower.contains("rest") -> {
                handleSleepQuery(input, context)
            }
            
            // Stress related queries
            inputLower.contains("stress") || inputLower.contains("anxious") || inputLower.contains("overwhelmed") -> {
                handleStressQuery(input, context)
            }
            
            // General health queries
            inputLower.contains("health") || inputLower.contains("wellness") || inputLower.contains("feel") -> {
                handleGeneralHealthQuery(input, context)
            }
            
            // Exercise and activity
            inputLower.contains("exercise") || inputLower.contains("activity") || inputLower.contains("workout") -> {
                handleActivityQuery(input, context)
            }
            
            else -> {
                handleGeneralQuery(input, context)
            }
        }
    }
    
    private suspend fun handleHeartRateQuery(input: String, context: AgentContext): String {
        val heartRateResult = executeTool("heart_rate", mapOf("time_range" to "recent"))
        
        return if (heartRateResult.isSuccess()) {
            val data = heartRateResult.getDataOrNull() ?: emptyMap()
            val currentBpm = data["current_bpm"] as? Double
            val avgBpm = data["average_bpm"] as? Double
            val hrv = data["heart_rate_variability"] as? Double
            
            buildString {
                append("I've analyzed your recent heart rate data. ")
                
                if (currentBpm != null) {
                    append("Your current heart rate is ${currentBpm.toInt()} BPM. ")
                    
                    when {
                        currentBpm < 60 -> append("This is on the lower side, which could indicate good cardiovascular fitness or potential bradycardia. ")
                        currentBpm > 100 -> append("This is elevated, which might be due to activity, stress, or other factors. ")
                        else -> append("This is within the normal resting range. ")
                    }
                }
                
                if (avgBpm != null) {
                    append("Your average heart rate recently has been ${avgBpm.toInt()} BPM. ")
                }
                
                if (hrv != null) {
                    append("Your heart rate variability is ${String.format("%.1f", hrv)}, ")
                    when {
                        hrv < 5 -> append("which suggests you might be experiencing stress or fatigue. Consider relaxation techniques. ")
                        hrv > 15 -> append("which indicates good cardiovascular health and recovery. ")
                        else -> append("which is within normal range. ")
                    }
                }
                
                append("\n\nRemember, I provide general wellness insights. For any health concerns, please consult with a healthcare professional.")
            }
        } else {
            "I'm having trouble accessing your heart rate data right now. Please ensure your heart rate sensor is enabled and you've granted the necessary permissions. In the meantime, you can manually check your pulse by placing two fingers on your wrist or neck and counting beats for 15 seconds, then multiplying by 4."
        }
    }
    
    private suspend fun handleSleepQuery(input: String, context: AgentContext): String {
        val sleepResult = executeTool("sleep_analysis", mapOf("hours_back" to 8))
        
        return if (sleepResult.isSuccess()) {
            val data = sleepResult.getDataOrNull() ?: emptyMap()
            val totalSleepHours = data["total_sleep_hours"] as? Double
            val qualityScore = data["sleep_quality_score"] as? Double
            val efficiency = data["sleep_efficiency"] as? Double
            val recommendations = data["recommendations"] as? List<*>
            
            buildString {
                append("Based on your sleep data analysis: ")
                
                if (totalSleepHours != null) {
                    append("You got approximately ${String.format("%.1f", totalSleepHours)} hours of sleep. ")
                    when {
                        totalSleepHours < 6 -> append("This is below the recommended 7-9 hours for most adults. ")
                        totalSleepHours > 9 -> append("This is more than the typical recommendation, which might indicate recovery needs. ")
                        else -> append("This falls within the healthy range for most adults. ")
                    }
                }
                
                if (qualityScore != null) {
                    val qualityPercent = (qualityScore * 100).toInt()
                    append("Your sleep quality score is $qualityPercent%. ")
                    when {
                        qualityScore < 0.4 -> append("There's room for improvement in your sleep quality. ")
                        qualityScore > 0.7 -> append("You're getting good quality sleep! ")
                        else -> append("Your sleep quality is moderate. ")
                    }
                }
                
                if (recommendations is List<*> && recommendations.isNotEmpty()) {
                    append("\n\nHere are some personalized recommendations:\n")
                    recommendations.take(3).forEach { rec ->
                        append("• $rec\n")
                    }
                }
            }
        } else {
            "I don't have enough sleep data to provide a detailed analysis. For better sleep insights, try to keep your phone nearby while sleeping (but in airplane mode to minimize disruption). In general, aim for 7-9 hours of sleep, maintain a consistent sleep schedule, and create a relaxing bedtime routine."
        }
    }
    
    private suspend fun handleStressQuery(input: String, context: AgentContext): String {
        val stressResult = executeTool("stress_detection", mapOf("time_range_minutes" to 30))
        
        return if (stressResult.isSuccess()) {
            val data = stressResult.getDataOrNull() ?: emptyMap()
            val stressLevel = data["stress_level"] as? String
            val confidence = data["confidence"] as? Double
            val recommendations = data["recommendations"] as? List<*>
            
            buildString {
                append("I've analyzed your recent physiological indicators for stress. ")
                
                when (stressLevel) {
                    "low" -> append("Good news! Your stress levels appear to be low right now. ")
                    "moderate" -> append("You're showing moderate stress indicators. ")
                    "high" -> append("I'm detecting elevated stress indicators. ")
                    else -> append("I'm having difficulty determining your current stress level. ")
                }
                
                if (confidence != null && confidence > 0.6) {
                    append("I'm ${(confidence * 100).toInt()}% confident in this assessment. ")
                }
                
                if (recommendations is List<*> && recommendations.isNotEmpty()) {
                    append("\n\nHere's what I recommend:\n")
                    recommendations.take(3).forEach { rec ->
                        append("• $rec\n")
                    }
                }
                
                append("\nRemember, stress is normal, but chronic stress can impact your health. If you're consistently feeling overwhelmed, consider speaking with a mental health professional.")
            }
        } else {
            "I don't have enough physiological data to assess your stress levels right now. However, if you're feeling stressed, try these evidence-based techniques: take 5 deep breaths (4 seconds in, 6 seconds out), do a quick body scan to release tension, or step outside for a few minutes if possible."
        }
    }
    
    private suspend fun handleActivityQuery(input: String, context: AgentContext): String {
        val activityResult = executeTool("activity_recognition", mapOf("min_confidence" to 0.6))
        val stepsResult = executeTool("step_counter", mapOf("time_range" to "today"))
        
        val activityData = activityResult.getDataOrNull() ?: emptyMap()
        val stepsData = stepsResult.getDataOrNull() ?: emptyMap()
        
        return buildString {
            append("Let me check your recent activity levels. ")
            
            val currentActivity = activityData["activity"] as? String
            if (currentActivity != null && currentActivity != "unknown") {
                append("Right now, it looks like you're $currentActivity. ")
            }
            
            val stepsToday = stepsData["steps_today"] as? Double
            if (stepsToday != null) {
                append("You've taken ${stepsToday.toInt()} steps today. ")
                when {
                    stepsToday < 5000 -> append("Consider adding more movement to your day - even short walks can boost your health! ")
                    stepsToday > 10000 -> append("Great job staying active! You've exceeded the recommended daily step count. ")
                    else -> append("You're making good progress toward the recommended 10,000 daily steps. ")
                }
            }
            
            val estimatedCalories = stepsData["estimated_calories"] as? Double
            if (estimatedCalories != null) {
                append("This activity has burned approximately ${estimatedCalories.toInt()} calories. ")
            }
            
            append("\n\nRegular physical activity is one of the best things you can do for your health. It improves cardiovascular health, mood, sleep quality, and helps manage stress.")
        }
    }
    
    private suspend fun handleGeneralHealthQuery(input: String, context: AgentContext): String {
        // Get multiple health indicators for a comprehensive overview
        val heartRateResult = executeTool("heart_rate", mapOf("time_range" to "current"))
        val activityResult = executeTool("activity_recognition", emptyMap())
        val stressResult = executeTool("stress_detection", mapOf("time_range_minutes" to 15))
        
        return buildString {
            append("Let me give you a quick health overview based on your current data. ")
            
            // Heart rate status
            val hrData = heartRateResult.getDataOrNull()
            val currentHR = hrData?.get("heart_rate_bpm") as? Double
            if (currentHR != null) {
                append("Your heart rate is ${currentHR.toInt()} BPM. ")
            }
            
            // Activity status
            val activityData = activityResult.getDataOrNull()
            val activity = activityData?.get("activity") as? String
            if (activity != null && activity != "unknown") {
                append("You appear to be $activity right now. ")
            }
            
            // Stress status
            val stressData = stressResult.getDataOrNull()
            val stressLevel = stressData?.get("stress_level") as? String
            if (stressLevel != null) {
                append("Your stress levels seem to be $stressLevel. ")
            }
            
            append("\n\nOverall health tips for today:\n")
            append("• Stay hydrated - aim for 8 glasses of water\n")
            append("• Take breaks from sitting every hour\n")
            append("• Practice deep breathing if you feel stressed\n")
            append("• Aim for 7-9 hours of sleep tonight\n")
            
            append("\nI'm here to help you understand your health patterns, but always consult healthcare professionals for medical concerns.")
        }
    }
    
    private suspend fun handleGeneralQuery(input: String, context: AgentContext): String {
        return "I'm Dr. Sarah, your health and wellness advisor. I can help you understand your heart rate patterns, analyze your sleep quality, detect stress levels, and provide personalized wellness recommendations based on your sensor data. What aspect of your health would you like to explore today?"
    }
    
    override suspend fun onOtherAgentResponse(otherAgentId: String, response: String) {
        // Health agent can coordinate with fitness and wellness agents
        Timber.d("Health agent received input from $otherAgentId: ${response.take(50)}...")
        
        // Could analyze other agents' responses for health implications
        // For example, if fitness agent mentions fatigue, health agent could suggest rest
    }
}