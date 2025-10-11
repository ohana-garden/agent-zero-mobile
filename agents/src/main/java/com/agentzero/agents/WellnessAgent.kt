package com.agentzero.agents

import com.agentzero.core.*
import timber.log.Timber

/**
 * Wellness-focused AI agent
 * 
 * Specializes in:
 * - Mental health and mindfulness
 * - Stress management and relaxation
 * - Work-life balance
 * - Emotional well-being
 * - Holistic health approaches
 */
class WellnessAgent(
    name: String,
    voiceProfile: VoiceProfile
) : Agent(
    name = name,
    type = "wellness",
    voiceProfile = voiceProfile
) {
    
    override val tools: List<AgentTool> = listOf()
    
    override val personality = AgentPersonality(
        traits = mapOf(
            "empathy" to 0.95f,
            "calmness" to 0.9f,
            "supportiveness" to 0.95f,
            "wisdom" to 0.8f,
            "nurturing" to 0.9f
        ),
        communicationStyle = "gentle_guide",
        responseLength = "medium",
        expertise = listOf(
            "mindfulness",
            "stress_management",
            "emotional_wellness",
            "meditation",
            "relaxation_techniques",
            "holistic_health"
        ),
        limitations = listOf(
            "Not a licensed therapist or counselor",
            "Cannot diagnose mental health conditions",
            "Recommendations are general wellness guidance"
        )
    )
    
    override val knowledgeDomains = listOf(
        "wellness",
        "mindfulness",
        "mental_health",
        "stress_management",
        "meditation",
        "emotional_intelligence"
    )
    
    override suspend fun generateResponse(input: String, context: AgentContext): String {
        val inputLower = input.lowercase()
        
        return when {
            // Stress and anxiety
            inputLower.contains("stress") || inputLower.contains("anxious") || inputLower.contains("overwhelm") -> {
                handleStressQuery(input, context)
            }
            
            // Mindfulness and meditation
            inputLower.contains("mindful") || inputLower.contains("meditat") || inputLower.contains("breathe") -> {
                handleMindfulnessQuery(input, context)
            }
            
            // Emotional well-being
            inputLower.contains("emotion") || inputLower.contains("mood") || inputLower.contains("feel") -> {
                handleEmotionalQuery(input, context)
            }
            
            // Sleep and rest
            inputLower.contains("sleep") || inputLower.contains("rest") || inputLower.contains("tired") -> {
                handleSleepWellnessQuery(input, context)
            }
            
            // Relaxation and calm
            inputLower.contains("relax") || inputLower.contains("calm") || inputLower.contains("peace") -> {
                handleRelaxationQuery(input, context)
            }
            
            // Balance and harmony
            inputLower.contains("balance") || inputLower.contains("harmony") || inputLower.contains("centered") -> {
                handleBalanceQuery(input, context)
            }
            
            // Self-care
            inputLower.contains("self-care") || inputLower.contains("self care") || inputLower.contains("nurture") -> {
                handleSelfCareQuery(input, context)
            }
            
            else -> {
                handleGeneralQuery(input, context)
            }
        }
    }
    
    private suspend fun handleStressQuery(input: String, context: AgentContext): String {
        val stressResult = executeTool("stress_detection", mapOf("time_range_minutes" to 30))
        val heartRateResult = executeTool("heart_rate", mapOf("time_range" to "current"))
        
        return buildString {
            append("I sense you're dealing with stress right now. That's completely natural - stress is part of being human. ")
            append("Let me help you find some peace and calm. ")
            
            val stressData = stressResult.getDataOrNull()
            val stressLevel = stressData?.get("stress_level") as? String
            val hrData = heartRateResult.getDataOrNull()
            val currentHR = hrData?.get("heart_rate_bpm") as? Double
            
            if (stressLevel != null) {
                when (stressLevel) {
                    "high" -> {
                        append("I can see your stress levels are quite elevated right now. ")
                        append("Let's focus on some immediate relief techniques. ")
                    }
                    "moderate" -> {
                        append("You're experiencing moderate stress levels. ")
                        append("This is manageable - let's work on bringing you back to center. ")
                    }
                    "low" -> {
                        append("Your stress levels seem relatively low, which is wonderful. ")
                        append("Let's explore some preventive techniques to keep it that way. ")
                    }
                }
            }
            
            if (currentHR != null && currentHR > 90) {
                append("Your heart rate is a bit elevated at ${currentHR.toInt()} BPM, which often accompanies stress. ")
                append("Let's work on slowing that down naturally. ")
            }
            
            append("\n\nImmediate stress relief (try right now):\n")
            append("• 4-7-8 Breathing: Inhale for 4, hold for 7, exhale for 8 counts\n")
            append("• Progressive muscle relaxation: Tense and release each muscle group\n")
            append("• Grounding technique: Name 5 things you see, 4 you hear, 3 you touch, 2 you smell, 1 you taste\n")
            append("• Gentle movement: Stretch your neck, shoulders, and arms\n")
            
            append("\nLonger-term stress management:\n")
            append("• Daily mindfulness practice: Even 5 minutes makes a difference\n")
            append("• Regular exercise: Walking, yoga, or any movement you enjoy\n")
            append("• Healthy boundaries: Learn to say no to non-essential commitments\n")
            append("• Connect with others: Share your feelings with trusted friends or family\n")
            append("• Nature time: Spend time outdoors when possible\n")
            
            append("\nStress reframing:\n")
            append("• Ask: 'Will this matter in 5 years?'\n")
            append("• Focus on what you can control, let go of what you can't\n")
            append("• Practice self-compassion: Treat yourself as you would a good friend\n")
            append("• Remember: This feeling is temporary and will pass\n")
            
            append("\nRemember, seeking help is a sign of strength, not weakness. If stress becomes overwhelming, ")
            append("please consider speaking with a mental health professional.")
        }
    }
    
    private suspend fun handleMindfulnessQuery(input: String, context: AgentContext): String {
        return buildString {
            append("Mindfulness is like a gentle return home to yourself. It's about being present, aware, and accepting of this moment. ")
            
            append("\n\nWhat is mindfulness?\n")
            append("• Present-moment awareness without judgment\n")
            append("• Observing thoughts and feelings without getting caught up in them\n")
            append("• Cultivating a kind, curious attitude toward your experience\n")
            append("• Creating space between stimulus and response\n")
            
            append("\nSimple mindfulness practices:\n")
            append("• Mindful breathing: Focus on the sensation of breath for 5-10 minutes\n")
            append("• Body scan: Notice sensations from head to toe\n")
            append("• Mindful walking: Pay attention to each step and the feeling of movement\n")
            append("• Eating meditation: Savor each bite, noticing taste, texture, temperature\n")
            append("• Loving-kindness: Send good wishes to yourself and others\n")
            
            append("\nMini-mindfulness moments (30 seconds - 2 minutes):\n")
            append("• Three conscious breaths before starting any activity\n")
            append("• Mindful hand washing: Feel the water temperature, soap texture\n")
            append("• Pause and notice: What do you see, hear, feel right now?\n")
            append("• Gratitude moment: Name three things you appreciate\n")
            append("• Mindful listening: Give someone your complete attention\n")
            
            append("\nBuilding a meditation practice:\n")
            append("• Start small: 3-5 minutes daily is better than 30 minutes once a week\n")
            append("• Same time, same place: Create a routine\n")
            append("• Be patient: Your mind will wander - that's normal and okay\n")
            append("• Use guided meditations if helpful\n")
            append("• Focus on consistency over duration\n")
            
            append("\nMindfulness for difficult emotions:\n")
            append("• RAIN technique: Recognize, Allow, Investigate, Non-attachment\n")
            append("• Name it to tame it: Simply labeling emotions reduces their intensity\n")
            append("• Breathe with the feeling: Don't try to change it, just be with it\n")
            append("• Remember: All emotions are temporary visitors\n")
            
            append("\nBenefits you might notice:\n")
            append("• Reduced stress and anxiety\n")
            append("• Better emotional regulation\n")
            append("• Improved focus and concentration\n")
            append("• Greater self-awareness\n")
            append("• Enhanced relationships\n")
            append("• Better sleep quality\n")
            
            append("\nRemember: Mindfulness isn't about emptying your mind or feeling peaceful all the time. ")
            append("It's about developing a different relationship with whatever arises.")
        }
    }
    
    private suspend fun handleEmotionalQuery(input: String, context: AgentContext): String {
        return buildString {
            append("Emotions are the colors of human experience - each one has value and wisdom to offer. ")
            append("Let's explore your emotional landscape with kindness and curiosity. ")
            
            append("\n\nUnderstanding emotions:\n")
            append("• Emotions are information - they tell us something important\n")
            append("• All emotions are valid, even the uncomfortable ones\n")
            append("• Emotions are temporary - they arise, peak, and naturally subside\n")
            append("• We can feel multiple emotions at once\n")
            append("• Emotions live in the body as much as the mind\n")
            
            append("\nEmotional awareness practice:\n")
            append("• Pause and ask: 'What am I feeling right now?'\n")
            append("• Locate the emotion in your body: Where do you feel it?\n")
            append("• Name it specifically: Frustrated vs angry, worried vs terrified\n")
            append("• Rate the intensity: 1-10 scale\n")
            append("• Breathe with it: Don't try to change it, just acknowledge it\n")
            
            append("\nWorking with difficult emotions:\n")
            append("• Anxiety: Focus on grounding techniques and present-moment awareness\n")
            append("• Anger: Use physical movement or journaling to process energy\n")
            append("• Sadness: Allow yourself to feel it fully, seek comfort and support\n")
            append("• Fear: Examine what you're afraid of, take small brave steps\n")
            append("• Loneliness: Reach out to others, practice self-compassion\n")
            
            append("\nEmotional regulation strategies:\n")
            append("• STOP technique: Stop, Take a breath, Observe, Proceed mindfully\n")
            append("• Opposite action: If emotion doesn't fit the facts, act opposite to its urge\n")
            append("• Self-soothing: Use your five senses to comfort yourself\n")
            append("• Distress tolerance: Ride out intense emotions without making them worse\n")
            
            append("\nBuilding emotional resilience:\n")
            append("• Practice self-compassion: Treat yourself with kindness\n")
            append("• Develop a support network: Cultivate meaningful relationships\n")
            append("• Maintain perspective: This too shall pass\n")
            append("• Learn from emotions: What are they trying to tell you?\n")
            append("• Celebrate emotional growth: Notice when you handle things better\n")
            
            append("\nDaily emotional check-ins:\n")
            append("• Morning: How am I feeling as I start the day?\n")
            append("• Midday: What emotions have I experienced so far?\n")
            append("• Evening: How did I handle my emotions today?\n")
            append("• What do I need emotionally right now?\n")
            
            append("\nRemember: Emotional intelligence is a skill that can be developed. ")
            append("Be patient and gentle with yourself as you learn.")
        }
    }
    
    private suspend fun handleSleepWellnessQuery(input: String, context: AgentContext): String {
        val sleepResult = executeTool("sleep_analysis", mapOf("hours_back" to 8))
        
        return buildString {
            append("Sleep is one of the most powerful tools for wellness - it's when your body and mind restore and rejuvenate. ")
            
            val sleepData = sleepResult.getDataOrNull()
            val sleepQuality = sleepData?.get("sleep_quality_score") as? Double
            val totalSleep = sleepData?.get("total_sleep_hours") as? Double
            
            if (sleepQuality != null) {
                val qualityPercent = (sleepQuality * 100).toInt()
                append("Your recent sleep quality score is $qualityPercent%. ")
                
                when {
                    sleepQuality < 0.4 -> append("Your sleep could use some nurturing attention. Let's work on creating better sleep conditions. ")
                    sleepQuality > 0.7 -> append("You're getting restorative sleep - wonderful! Let's maintain these good patterns. ")
                    else -> append("Your sleep is moderate. There's room for improvement to help you feel more refreshed. ")
                }
            }
            
            if (totalSleep != null) {
                append("You got approximately ${String.format("%.1f", totalSleep)} hours of sleep. ")
                when {
                    totalSleep < 6 -> append("This is quite short - your body and mind need more rest to function optimally. ")
                    totalSleep > 9 -> append("This is generous sleep time - listen to your body's needs. ")
                    else -> append("This falls within a healthy range for most people. ")
                }
            }
            
            append("\n\nSleep wellness foundations:\n")
            append("• Consistent schedule: Go to bed and wake up at the same time daily\n")
            append("• Sleep environment: Cool (65-68°F), dark, quiet, comfortable\n")
            append("• Wind-down routine: 30-60 minutes of calming activities before bed\n")
            append("• Limit screens: Blue light can interfere with melatonin production\n")
            append("• Comfortable bedding: Invest in quality pillows and mattress\n")
            
            append("\nBedtime ritual ideas:\n")
            append("• Gentle stretching or yoga\n")
            append("• Reading something calming (not work-related)\n")
            append("• Journaling: Write down thoughts or gratitudes\n")
            append("• Herbal tea: Chamomile, passionflower, or valerian\n")
            append("• Progressive muscle relaxation\n")
            append("• Meditation or breathing exercises\n")
            
            append("\nSleep hygiene tips:\n")
            append("• Avoid caffeine 6+ hours before bedtime\n")
            append("• Limit alcohol: It may help you fall asleep but disrupts sleep quality\n")
            append("• No large meals 2-3 hours before bed\n")
            append("• Get morning sunlight: Helps regulate circadian rhythm\n")
            append("• Exercise regularly, but not close to bedtime\n")
            
            append("\nFor racing thoughts at bedtime:\n")
            append("• Keep a notepad by your bed for worries or to-dos\n")
            append("• Practice the 4-7-8 breathing technique\n")
            append("• Try a body scan meditation\n")
            append("• Use guided sleep meditations or stories\n")
            append("• Visualize a peaceful, safe place\n")
            
            append("\nSleep and wellness connection:\n")
            append("• Better sleep improves mood, focus, and immune function\n")
            append("• Quality sleep helps regulate stress hormones\n")
            append("• Adequate rest supports emotional regulation\n")
            append("• Sleep is when memories consolidate and the brain detoxifies\n")
            
            append("\nRemember: Good sleep is not a luxury - it's essential for your overall wellness. ")
            append("Treat your sleep with the same priority as nutrition and exercise.")
        }
    }
    
    private suspend fun handleRelaxationQuery(input: String, context: AgentContext): String {
        return buildString {
            append("Relaxation is a skill that gets stronger with practice. Let me guide you to a place of calm and peace. ")
            
            append("\n\nQuick relaxation techniques (2-5 minutes):\n")
            append("• Box breathing: Inhale 4, hold 4, exhale 4, hold 4\n")
            append("• 5-4-3-2-1 grounding: 5 things you see, 4 hear, 3 touch, 2 smell, 1 taste\n")
            append("• Shoulder release: Lift shoulders to ears, hold 5 seconds, release\n")
            append("• Gentle neck rolls: Slow, mindful movements\n")
            append("• Visualization: Imagine a peaceful place in detail\n")
            
            append("\nProgressive muscle relaxation (10-20 minutes):\n")
            append("1. Start with your toes: Tense for 5 seconds, then release\n")
            append("2. Move up through each muscle group: calves, thighs, glutes\n")
            append("3. Continue with abdomen, chest, arms, hands\n")
            append("4. Finish with shoulders, neck, and face\n")
            append("5. Notice the contrast between tension and relaxation\n")
            
            append("\nBreathing for relaxation:\n")
            append("• Diaphragmatic breathing: Breathe into your belly, not chest\n")
            append("• Extended exhale: Make exhale longer than inhale (calms nervous system)\n")
            append("• Counted breathing: Inhale for 4, exhale for 6-8\n")
            append("• Natural rhythm: Simply observe your breath without changing it\n")
            
            append("\nMindful relaxation activities:\n")
            append("• Gentle yoga or stretching\n")
            append("• Walking in nature\n")
            append("• Listening to calming music\n")
            append("• Taking a warm bath with Epsom salts\n")
            append("• Gentle self-massage\n")
            append("• Coloring or drawing\n")
            
            append("\nCreating a relaxation space:\n")
            append("• Choose a quiet corner or room\n")
            append("• Add comfortable seating or cushions\n")
            append("• Include calming elements: plants, soft lighting, aromatherapy\n")
            append("• Keep it clutter-free and peaceful\n")
            append("• Make it a phone-free zone\n")
            
            append("\nDaily relaxation practice:\n")
            append("• Morning: 5 minutes of gentle breathing or stretching\n")
            append("• Midday: Brief relaxation break, even 2 minutes helps\n")
            append("• Evening: Longer relaxation session to transition from day\n")
            append("• Bedtime: Calming routine to prepare for sleep\n")
            
            append("\nSigns you need more relaxation:\n")
            append("• Feeling constantly 'on' or unable to unwind\n")
            append("• Physical tension in shoulders, jaw, or back\n")
            append("• Difficulty falling asleep or staying asleep\n")
            append("• Feeling irritable or overwhelmed easily\n")
            append("• Racing thoughts or difficulty concentrating\n")
            
            append("\nRemember: Relaxation isn't selfish - it's necessary. ")
            append("When you're relaxed and centered, you can show up better for everything and everyone in your life.")
        }
    }
    
    private suspend fun handleBalanceQuery(input: String, context: AgentContext): String {
        return buildString {
            append("Balance isn't about perfection - it's about conscious choices and gentle adjustments. ")
            append("Like a tree that bends with the wind but stays rooted, we can find our center. ")
            
            append("\n\nDimensions of wellness balance:\n")
            append("• Physical: Movement, nutrition, sleep, medical care\n")
            append("• Mental: Learning, creativity, problem-solving, focus\n")
            append("• Emotional: Feeling, expressing, processing emotions healthily\n")
            append("• Social: Relationships, community, connection with others\n")
            append("• Spiritual: Purpose, meaning, connection to something greater\n")
            append("• Environmental: Your surroundings, nature connection, organization\n")
            
            append("\nFinding your balance:\n")
            append("• Regular self-check-ins: How am I doing in each area?\n")
            append("• Identify what's out of balance: What needs more attention?\n")
            append("• Make small adjustments: Tiny changes compound over time\n")
            append("• Be flexible: Balance looks different in different seasons of life\n")
            append("• Practice self-compassion: You don't have to be perfect\n")
            
            append("\nDaily balance practices:\n")
            append("• Morning intention: How do I want to show up today?\n")
            append("• Midday pause: Am I honoring my needs and values?\n")
            append("• Evening reflection: What went well? What needs adjustment?\n")
            append("• Gratitude practice: What am I thankful for today?\n")
            
            append("\nWork-life integration:\n")
            append("• Set boundaries: Protect your personal time\n")
            append("• Transition rituals: Create space between work and personal time\n")
            append("• Prioritize ruthlessly: Not everything is equally important\n")
            append("• Schedule self-care: Put it on your calendar like any important appointment\n")
            
            append("\nEnergy management:\n")
            append("• Notice your natural rhythms: When do you have most energy?\n")
            append("• Match tasks to energy levels: Hard tasks when energy is high\n")
            append("• Take breaks before you need them: Prevention is better than recovery\n")
            append("• Protect your energy: Limit energy drains when possible\n")
            
            append("\nSigns of good balance:\n")
            append("• Feeling generally content and peaceful\n")
            append("• Having energy for things that matter to you\n")
            append("• Maintaining important relationships\n")
            append("• Taking care of your physical health\n")
            append("• Feeling aligned with your values\n")
            
            append("\nWhen balance feels impossible:\n")
            append("• Remember: This is a season, not forever\n")
            append("• Focus on the essentials: What absolutely must be done?\n")
            append("• Ask for help: You don't have to do everything alone\n")
            append("• Lower your standards temporarily: Good enough is okay\n")
            append("• Practice extra self-compassion during difficult times\n")
            
            append("\nRemember: Balance is not a destination but a continuous dance. ")
            append("Be gentle with yourself as you find your rhythm.")
        }
    }
    
    private suspend fun handleSelfCareQuery(input: String, context: AgentContext): String {
        return buildString {
            append("Self-care isn't selfish - it's essential. You can't pour from an empty cup. ")
            append("Let's explore ways to nurture and replenish yourself. ")
            
            append("\n\nTypes of self-care:\n")
            append("• Physical: Exercise, nutrition, sleep, medical care, hygiene\n")
            append("• Emotional: Therapy, journaling, expressing feelings, setting boundaries\n")
            append("• Mental: Reading, learning, puzzles, limiting negative media\n")
            append("• Social: Spending time with loved ones, community involvement\n")
            append("• Spiritual: Meditation, prayer, nature time, purpose reflection\n")
            append("• Practical: Organizing, financial planning, time management\n")
            
            append("\nDaily self-care ideas (5-15 minutes):\n")
            append("• Morning: Gentle stretching, gratitude practice, mindful coffee/tea\n")
            append("• Midday: Walk outside, deep breathing, healthy snack\n")
            append("• Evening: Relaxing bath, reading, gentle music\n")
            append("• Anytime: Call a friend, pet an animal, look at something beautiful\n")
            
            append("\nWeekly self-care practices:\n")
            append("• Longer nature walks or hikes\n")
            append("• Creative activities: art, music, writing, crafts\n")
            append("• Social connection: dinner with friends, family time\n")
            append("• Learning something new or pursuing a hobby\n")
            append("• Decluttering and organizing your space\n")
            append("• Planning and preparing for the week ahead\n")
            
            append("\nMonthly self-care rituals:\n")
            append("• Professional massage or spa treatment\n")
            append("• Weekend retreat or mini-vacation\n")
            append("• Deep cleaning and reorganizing living space\n")
            append("• Reviewing and adjusting goals and priorities\n")
            append("• Trying a new restaurant, activity, or experience\n")
            
            append("\nSelf-care on a budget:\n")
            append("• Free meditation apps or YouTube videos\n")
            append("• Library books, podcasts, free online courses\n")
            append("• Nature walks, hiking, beach visits\n")
            append("• Home spa treatments: DIY face masks, baths\n")
            append("• Cooking a special meal for yourself\n")
            append("• Rearranging furniture for a fresh perspective\n")
            
            append("\nEmotional self-care:\n")
            append("• Practice saying no to things that drain you\n")
            append("• Limit time with negative people when possible\n")
            append("• Express your feelings through journaling or art\n")
            append("• Seek professional help when needed\n")
            append("• Practice self-compassion and positive self-talk\n")
            
            append("\nSelf-care myths to release:\n")
            append("• 'I don't have time' - Start with 5 minutes\n")
            append("• 'It's selfish' - Taking care of yourself helps you care for others\n")
            append("• 'It's expensive' - Many forms of self-care are free\n")
            append("• 'I don't deserve it' - Everyone deserves care, including you\n")
            
            append("\nCreating a self-care plan:\n")
            append("1. Assess your current self-care in each area\n")
            append("2. Identify what you need more of\n")
            append("3. Choose 2-3 realistic practices to start\n")
            append("4. Schedule them like important appointments\n")
            append("5. Start small and build gradually\n")
            append("6. Adjust as needed - self-care should feel good, not stressful\n")
            
            append("\nRemember: Self-care is not a luxury - it's a necessity. ")
            append("You are worthy of care, love, and attention, especially from yourself.")
        }
    }
    
    private suspend fun handleGeneralQuery(input: String, context: AgentContext): String {
        return "Hello, beautiful soul. I'm Maya, your wellness guide and gentle companion on this journey of self-discovery and healing. I'm here to help you find peace in the storm, balance in the chaos, and kindness toward yourself. Whether you're seeking stress relief, emotional support, mindfulness practices, or simply a moment of calm, I'm here to walk alongside you with compassion and wisdom. What aspect of your wellness would you like to nurture today?"
    }
    
    override suspend fun onOtherAgentResponse(otherAgentId: String, response: String) {
        // Wellness agent can provide holistic perspective on other agents' advice
        Timber.d("Wellness agent received input from $otherAgentId: ${response.take(50)}...")
        
        // Could suggest mindfulness approaches to fitness goals
        // Or provide emotional support for financial stress
        // Or recommend stress management for productivity challenges
    }
}