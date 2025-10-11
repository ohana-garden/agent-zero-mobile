package com.agentzero.agents

import com.agentzero.core.*
import timber.log.Timber

/**
 * Fitness-focused AI agent
 * 
 * Specializes in:
 * - Workout planning and tracking
 * - Activity recognition and analysis
 * - Performance optimization
 * - Recovery monitoring
 * - Motivation and coaching
 */
class FitnessAgent(
    name: String,
    voiceProfile: VoiceProfile
) : Agent(
    name = name,
    type = "fitness",
    voiceProfile = voiceProfile
) {
    
    override val tools: List<AgentTool> = listOf()
    
    override val personality = AgentPersonality(
        traits = mapOf(
            "motivation" to 0.9f,
            "enthusiasm" to 0.8f,
            "supportiveness" to 0.9f,
            "goal_oriented" to 0.8f,
            "encouraging" to 0.9f
        ),
        communicationStyle = "energetic_coach",
        responseLength = "medium",
        expertise = listOf(
            "exercise_physiology",
            "workout_planning",
            "performance_tracking",
            "recovery_optimization",
            "sports_nutrition",
            "injury_prevention"
        ),
        limitations = listOf(
            "Not a certified personal trainer",
            "Cannot provide medical advice for injuries",
            "Recommendations are general fitness guidance"
        )
    )
    
    override val knowledgeDomains = listOf(
        "fitness",
        "exercise",
        "sports_science",
        "nutrition",
        "recovery",
        "performance"
    )
    
    override suspend fun generateResponse(input: String, context: AgentContext): String {
        val inputLower = input.lowercase()
        
        return when {
            // Workout and exercise queries
            inputLower.contains("workout") || inputLower.contains("exercise") || inputLower.contains("train") -> {
                handleWorkoutQuery(input, context)
            }
            
            // Steps and walking
            inputLower.contains("steps") || inputLower.contains("walk") || inputLower.contains("run") -> {
                handleStepsQuery(input, context)
            }
            
            // Activity and movement
            inputLower.contains("activity") || inputLower.contains("move") || inputLower.contains("active") -> {
                handleActivityQuery(input, context)
            }
            
            // Performance and goals
            inputLower.contains("goal") || inputLower.contains("progress") || inputLower.contains("improve") -> {
                handleGoalsQuery(input, context)
            }
            
            // Recovery and rest
            inputLower.contains("recovery") || inputLower.contains("rest") || inputLower.contains("sore") -> {
                handleRecoveryQuery(input, context)
            }
            
            // Motivation and encouragement
            inputLower.contains("motivat") || inputLower.contains("lazy") || inputLower.contains("tired") -> {
                handleMotivationQuery(input, context)
            }
            
            else -> {
                handleGeneralQuery(input, context)
            }
        }
    }
    
    private suspend fun handleWorkoutQuery(input: String, context: AgentContext): String {
        val activityResult = executeTool("activity_recognition", mapOf("min_confidence" to 0.6))
        val heartRateResult = executeTool("heart_rate", mapOf("time_range" to "current"))
        
        val activityData = activityResult.getDataOrNull() ?: emptyMap()
        val hrData = heartRateResult.getDataOrNull() ?: emptyMap()
        
        return buildString {
            append("Hey there, fitness champion! Let's talk about your workout. ")
            
            val currentActivity = activityData["activity"] as? String
            val currentHR = hrData["heart_rate_bpm"] as? Double
            
            if (currentActivity != null) {
                when (currentActivity) {
                    "running" -> {
                        append("I can see you're running right now - awesome! ")
                        if (currentHR != null) {
                            when {
                                currentHR > 150 -> append("Your heart rate is ${currentHR.toInt()} BPM - you're in the high-intensity zone! Great for building cardiovascular fitness. ")
                                currentHR > 120 -> append("Your heart rate is ${currentHR.toInt()} BPM - perfect moderate intensity zone for endurance building. ")
                                else -> append("Your heart rate is ${currentHR.toInt()} BPM - nice easy pace for active recovery or warm-up. ")
                            }
                        }
                    }
                    "walking" -> {
                        append("Walking is fantastic exercise! It's low-impact, sustainable, and great for your overall health. ")
                        if (currentHR != null && currentHR > 100) {
                            append("Your elevated heart rate shows you're getting a good cardiovascular benefit. ")
                        }
                    }
                    "vigorous_activity" -> {
                        append("I can see you're doing some intense activity - you're crushing it! ")
                        append("Make sure to stay hydrated and listen to your body. ")
                    }
                    else -> {
                        append("Ready to get moving? ")
                    }
                }
            }
            
            append("\n\nHere are some workout ideas based on your current state:\n")
            
            if (currentHR != null && currentHR > 120) {
                append("• You're already warmed up - perfect time for strength training or intervals\n")
                append("• Try 30 seconds of high-intensity movement, 30 seconds rest, repeat 8 times\n")
            } else {
                append("• Start with a 5-minute warm-up: light movement or dynamic stretching\n")
                append("• Try a bodyweight circuit: squats, push-ups, lunges, planks\n")
                append("• Or go for a brisk 20-30 minute walk or jog\n")
            }
            
            append("\nRemember: consistency beats perfection! Even 10 minutes of movement is better than none.")
        }
    }
    
    private suspend fun handleStepsQuery(input: String, context: AgentContext): String {
        val stepsResult = executeTool("step_counter", mapOf("time_range" to "today"))
        
        return if (stepsResult.isSuccess()) {
            val data = stepsResult.getDataOrNull() ?: emptyMap()
            val stepsToday = data["steps_today"] as? Double
            val estimatedDistance = data["estimated_distance_km"] as? Double
            val estimatedCalories = data["estimated_calories"] as? Double
            val activePeriods = data["active_periods"] as? List<*>
            
            buildString {
                append("Let's check out your step game today! ")
                
                if (stepsToday != null) {
                    val steps = stepsToday.toInt()
                    append("You've crushed $steps steps so far. ")
                    
                    when {
                        steps < 2000 -> {
                            append("We're just getting started! Every step counts, and you've got plenty of day left to add more. ")
                            append("How about a quick 5-minute walk right now? That's about 500 more steps! ")
                        }
                        steps < 5000 -> {
                            append("You're building momentum! You're about halfway to the basic daily recommendation. ")
                            append("A 10-15 minute walk could easily get you to 5,000 steps. ")
                        }
                        steps < 8000 -> {
                            append("Solid progress! You're well on your way to that 10,000-step goal. ")
                            append("Maybe take the stairs instead of the elevator, or park a bit further away? ")
                        }
                        steps < 10000 -> {
                            append("You're so close to 10,000 steps! Just ${10000 - steps} more to go. ")
                            append("A short evening walk could easily get you there! ")
                        }
                        else -> {
                            append("BOOM! You've smashed the 10,000-step goal! You're a walking machine! ")
                            append("This level of activity is fantastic for your cardiovascular health. ")
                        }
                    }
                }
                
                if (estimatedDistance != null) {
                    append("That's approximately ${String.format("%.1f", estimatedDistance)} kilometers of movement. ")
                }
                
                if (estimatedCalories != null) {
                    append("You've burned roughly ${estimatedCalories.toInt()} calories just from walking - nice! ")
                }
                
                if (activePeriods is List<*> && activePeriods.isNotEmpty()) {
                    append("\n\nI noticed you had ${activePeriods.size} active periods today - great job staying consistent throughout the day!")
                }
                
                append("\n\nPro tip: Breaking up sitting time with regular movement is just as important as total step count!")
            }
        } else {
            "I can't access your step data right now, but that doesn't stop us from getting moving! How about we start with a simple goal: take a 2-minute walk right now. Your body will thank you, and it's a great way to boost energy and mood. Every journey starts with a single step!"
        }
    }
    
    private suspend fun handleActivityQuery(input: String, context: AgentContext): String {
        val activityResult = executeTool("activity_recognition", mapOf("min_confidence" to 0.5))
        val motionResult = executeTool("accelerometer", mapOf("duration_seconds" to 10))
        
        val activityData = activityResult.getDataOrNull() ?: emptyMap()
        val motionData = motionResult.getDataOrNull() ?: emptyMap()
        
        return buildString {
            append("Let me analyze your current activity level! ")
            
            val currentActivity = activityData["activity"] as? String
            val confidence = activityData["confidence"] as? Double
            val motionLevel = motionData["motion_level"] as? String
            
            when (currentActivity) {
                "stationary", "resting" -> {
                    append("I can see you're taking it easy right now. ")
                    append("That's totally fine - rest is important! But if you've been sitting for a while, ")
                    append("how about a quick movement break? Even 30 seconds of stretching or marching in place can help. ")
                }
                "walking" -> {
                    append("Nice! You're walking - one of the best exercises there is. ")
                    append("Walking improves cardiovascular health, strengthens bones, and boosts mood. Keep it up! ")
                }
                "running" -> {
                    append("You're running! That's fantastic cardio work. ")
                    append("Remember to maintain good form and breathe rhythmically. You've got this! ")
                }
                "vigorous_activity" -> {
                    append("Wow, you're really going for it with some intense activity! ")
                    append("Make sure to stay hydrated and take breaks if you need them. ")
                }
                else -> {
                    append("I'm not entirely sure what activity you're doing right now, but ")
                }
            }
            
            if (confidence != null && confidence > 0.7) {
                append("I'm ${(confidence * 100).toInt()}% confident in this assessment. ")
            }
            
            append("\n\nActivity recommendations based on your current state:\n")
            
            when (motionLevel) {
                "stationary" -> {
                    append("• Try desk exercises: shoulder rolls, neck stretches, seated spinal twists\n")
                    append("• Stand up and do 10 bodyweight squats\n")
                    append("• Take a 2-minute walking break\n")
                }
                "light_movement" -> {
                    append("• Perfect time to increase intensity slightly\n")
                    append("• Add some arm movements to your walk\n")
                    append("• Try walking up some stairs\n")
                }
                "moderate_movement" -> {
                    append("• You're in a great zone for sustained activity\n")
                    append("• This is perfect for building endurance\n")
                    append("• Keep this pace for 20-30 minutes if possible\n")
                }
                "vigorous_movement" -> {
                    append("• Awesome intensity! Make sure you can still hold a conversation\n")
                    append("• Consider interval training: alternate high and moderate intensity\n")
                    append("• Don't forget to cool down afterwards\n")
                }
            }
            
            append("\nRemember: The best exercise is the one you'll actually do consistently!")
        }
    }
    
    private suspend fun handleGoalsQuery(input: String, context: AgentContext): String {
        val stepsResult = executeTool("step_counter", mapOf("time_range" to "today"))
        val heartRateResult = executeTool("heart_rate", mapOf("time_range" to "recent"))
        
        return buildString {
            append("Let's talk about crushing your fitness goals! ")
            
            val stepsData = stepsResult.getDataOrNull()
            val stepsToday = stepsData?.get("steps_today") as? Double
            
            if (stepsToday != null) {
                val steps = stepsToday.toInt()
                val progressPercent = minOf((steps / 10000.0 * 100).toInt(), 100)
                
                append("Today you're $progressPercent% of the way to the 10,000-step goal with $steps steps. ")
                
                when {
                    progressPercent < 25 -> append("We're just getting started - every champion begins with the first step! ")
                    progressPercent < 50 -> append("You're building momentum - I can see the dedication! ")
                    progressPercent < 75 -> append("You're more than halfway there - the finish line is in sight! ")
                    progressPercent < 100 -> append("So close to your goal - you've got this! ")
                    else -> append("Goal SMASHED! You're a fitness rockstar! ")
                }
            }
            
            append("\n\nHere's how to level up your fitness game:\n")
            append("• Set SMART goals: Specific, Measurable, Achievable, Relevant, Time-bound\n")
            append("• Track your progress daily - what gets measured gets managed\n")
            append("• Celebrate small wins - they add up to big victories\n")
            append("• Focus on consistency over perfection\n")
            
            append("\nSome goal ideas to consider:\n")
            append("• Increase daily steps by 500 each week\n")
            append("• Add 2 strength training sessions per week\n")
            append("• Improve your 1-mile walk/run time by 30 seconds monthly\n")
            append("• Try a new physical activity each month\n")
            
            append("\nRemember: Progress isn't always linear, but persistence always pays off!")
        }
    }
    
    private suspend fun handleRecoveryQuery(input: String, context: AgentContext): String {
        val heartRateResult = executeTool("heart_rate", mapOf("time_range" to "recent"))
        val sleepResult = executeTool("sleep_analysis", mapOf("hours_back" to 8))
        
        return buildString {
            append("Recovery is where the magic happens! Let me check how your body is doing. ")
            
            val hrData = heartRateResult.getDataOrNull()
            val sleepData = sleepResult.getDataOrNull()
            
            val restingHR = hrData?.get("average_bpm") as? Double
            val hrv = hrData?.get("heart_rate_variability") as? Double
            val sleepQuality = sleepData?.get("sleep_quality_score") as? Double
            
            if (restingHR != null) {
                append("Your recent average heart rate is ${restingHR.toInt()} BPM. ")
                when {
                    restingHR > 80 -> append("This might indicate you need more recovery time. ")
                    restingHR < 60 -> append("Great resting heart rate - shows good cardiovascular fitness! ")
                    else -> append("Your heart rate looks normal. ")
                }
            }
            
            if (hrv != null) {
                when {
                    hrv < 5 -> append("Your heart rate variability suggests you might be under-recovered. Time to prioritize rest! ")
                    hrv > 15 -> append("Excellent heart rate variability - your body is recovering well! ")
                    else -> append("Your recovery markers look decent. ")
                }
            }
            
            if (sleepQuality != null) {
                val qualityPercent = (sleepQuality * 100).toInt()
                append("Your sleep quality score is $qualityPercent%. ")
                when {
                    sleepQuality < 0.4 -> append("Poor sleep can really impact recovery - let's work on that sleep hygiene! ")
                    sleepQuality > 0.7 -> append("Great sleep quality - that's your secret weapon for recovery! ")
                }
            }
            
            append("\n\nRecovery optimization tips:\n")
            append("• Prioritize 7-9 hours of quality sleep\n")
            append("• Stay hydrated - aim for clear or light yellow urine\n")
            append("• Include protein in your post-workout meal (within 2 hours)\n")
            append("• Try gentle movement on rest days - walking, yoga, stretching\n")
            append("• Manage stress through meditation, deep breathing, or relaxing activities\n")
            
            append("\nRemember: You don't get stronger during workouts - you get stronger during recovery!")
        }
    }
    
    private suspend fun handleMotivationQuery(input: String, context: AgentContext): String {
        return buildString {
            append("Hey, I hear you! We all have those moments when motivation feels low. ")
            append("But here's the thing - you don't need motivation, you need momentum! ")
            
            append("\n\nLet's start ridiculously small:\n")
            append("• Put on your workout clothes (that's it!)\n")
            append("• Do 5 jumping jacks\n")
            append("• Walk to the end of your street and back\n")
            append("• Do a 2-minute dance party in your living room\n")
            
            append("\nMotivation boosters that actually work:\n")
            append("• Remember your 'why' - what made you want to get fit?\n")
            append("• Focus on how you FEEL after exercise, not just how you look\n")
            append("• Find activities you actually enjoy - fitness should be fun!\n")
            append("• Get an accountability buddy or join a community\n")
            append("• Track your progress - seeing improvement is incredibly motivating\n")
            
            append("\nHere's the truth: Discipline beats motivation every time. ")
            append("Motivation gets you started, but habits keep you going. ")
            append("You've got this - I believe in you! ")
            
            append("\nWhat's one tiny thing you can do right now to move your body? ")
            append("Even 30 seconds counts!")
        }
    }
    
    private suspend fun handleGeneralQuery(input: String, context: AgentContext): String {
        return "Hey there, superstar! I'm Coach Mike, your personal fitness motivator and movement expert! I'm here to help you crush your fitness goals, whether that's getting more steps, planning workouts, tracking your progress, or just finding ways to move more throughout your day. I believe every body is capable of amazing things - what fitness adventure should we tackle together today?"
    }
    
    override suspend fun onOtherAgentResponse(otherAgentId: String, response: String) {
        // Fitness agent can coordinate with health and wellness agents
        Timber.d("Fitness agent received input from $otherAgentId: ${response.take(50)}...")
        
        // Could provide fitness recommendations based on health agent's stress/sleep analysis
        // Or coordinate with wellness agent for holistic health approach
    }
}