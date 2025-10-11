package com.agentzero.agents

import com.agentzero.core.*
import timber.log.Timber

/**
 * Productivity-focused AI agent
 * 
 * Specializes in:
 * - Time management and scheduling
 * - Task organization and prioritization
 * - Focus and concentration techniques
 * - Workflow optimization
 * - Goal setting and tracking
 */
class ProductivityAgent(
    name: String,
    voiceProfile: VoiceProfile
) : Agent(
    name = name,
    type = "productivity",
    voiceProfile = voiceProfile
) {
    
    override val tools: List<AgentTool> = listOf()
    
    override val personality = AgentPersonality(
        traits = mapOf(
            "organized" to 0.9f,
            "efficient" to 0.9f,
            "goal_oriented" to 0.8f,
            "systematic" to 0.8f,
            "supportive" to 0.7f
        ),
        communicationStyle = "efficient_assistant",
        responseLength = "medium",
        expertise = listOf(
            "time_management",
            "task_prioritization",
            "workflow_optimization",
            "focus_techniques",
            "goal_setting",
            "habit_formation"
        ),
        limitations = listOf(
            "Cannot access external calendars or task systems",
            "Recommendations are general productivity principles",
            "Cannot manage actual tasks or schedules"
        )
    )
    
    override val knowledgeDomains = listOf(
        "productivity",
        "time_management",
        "organization",
        "goal_setting",
        "workflow",
        "efficiency"
    )
    
    override suspend fun generateResponse(input: String, context: AgentContext): String {
        val inputLower = input.lowercase()
        
        return when {
            // Time management queries
            inputLower.contains("time") || inputLower.contains("schedule") || inputLower.contains("calendar") -> {
                handleTimeManagementQuery(input, context)
            }
            
            // Task and project management
            inputLower.contains("task") || inputLower.contains("project") || inputLower.contains("organize") -> {
                handleTaskManagementQuery(input, context)
            }
            
            // Focus and concentration
            inputLower.contains("focus") || inputLower.contains("concentrate") || inputLower.contains("distract") -> {
                handleFocusQuery(input, context)
            }
            
            // Goals and planning
            inputLower.contains("goal") || inputLower.contains("plan") || inputLower.contains("achieve") -> {
                handleGoalsQuery(input, context)
            }
            
            // Habits and routines
            inputLower.contains("habit") || inputLower.contains("routine") || inputLower.contains("consistent") -> {
                handleHabitsQuery(input, context)
            }
            
            // Procrastination and motivation
            inputLower.contains("procrastinat") || inputLower.contains("motivat") || inputLower.contains("lazy") -> {
                handleProcrastinationQuery(input, context)
            }
            
            // Work-life balance
            inputLower.contains("balance") || inputLower.contains("overwhelm") || inputLower.contains("busy") -> {
                handleWorkLifeBalanceQuery(input, context)
            }
            
            else -> {
                handleGeneralQuery(input, context)
            }
        }
    }
    
    private suspend fun handleTimeManagementQuery(input: String, context: AgentContext): String {
        return buildString {
            append("Time management is the foundation of productivity! Let me help you make the most of your time. ")
            
            append("\n\nCore time management principles:\n")
            append("• Time blocking: Schedule specific times for specific activities\n")
            append("• The 2-minute rule: If it takes less than 2 minutes, do it now\n")
            append("• Batch similar tasks together to minimize context switching\n")
            append("• Use the Eisenhower Matrix: Urgent vs Important\n")
            append("• Plan your day the night before or first thing in the morning\n")
            
            append("\nTime blocking strategy:\n")
            append("1. List all your regular activities and responsibilities\n")
            append("2. Estimate how long each typically takes\n")
            append("3. Block out time in your calendar for each\n")
            append("4. Include buffer time between activities\n")
            append("5. Protect your blocks - treat them like important meetings\n")
            
            append("\nThe Eisenhower Matrix:\n")
            append("• Quadrant 1: Urgent + Important = Do first\n")
            append("• Quadrant 2: Not Urgent + Important = Schedule\n")
            append("• Quadrant 3: Urgent + Not Important = Delegate\n")
            append("• Quadrant 4: Not Urgent + Not Important = Eliminate\n")
            
            append("\nTime audit exercise:\n")
            append("Track your time for 3 days in 15-minute increments. You'll be surprised where time actually goes! ")
            append("This awareness is the first step to better time management.\n")
            
            append("\nRemember: You can't manage time, but you can manage your attention and energy!")
        }
    }
    
    private suspend fun handleTaskManagementQuery(input: String, context: AgentContext): String {
        return buildString {
            append("Great task management turns chaos into clarity! Let's organize your work effectively. ")
            
            append("\n\nTask management system (GTD-inspired):\n")
            append("1. Capture: Write down everything that has your attention\n")
            append("2. Clarify: What does each item mean? Is it actionable?\n")
            append("3. Organize: Put items in the right categories\n")
            append("4. Reflect: Review your system regularly\n")
            append("5. Engage: Take action with confidence\n")
            
            append("\nTask categories:\n")
            append("• Next Actions: Specific, actionable tasks you can do now\n")
            append("• Projects: Outcomes requiring multiple steps\n")
            append("• Waiting For: Items you're waiting on from others\n")
            append("• Someday/Maybe: Things you might want to do later\n")
            append("• Reference: Information you might need later\n")
            
            append("\nPrioritization techniques:\n")
            append("• MoSCoW: Must have, Should have, Could have, Won't have\n")
            append("• ABCDE: A=Must do, B=Should do, C=Nice to do, D=Delegate, E=Eliminate\n")
            append("• Impact vs Effort matrix: High impact, low effort tasks first\n")
            
            append("\nProject planning:\n")
            append("1. Define the successful outcome clearly\n")
            append("2. Brainstorm all the steps needed\n")
            append("3. Organize steps in logical order\n")
            append("4. Identify the very next action\n")
            append("5. Set up regular review checkpoints\n")
            
            append("\nDaily task management:\n")
            append("• Start each day by reviewing your task list\n")
            append("• Pick 3 most important tasks (MIT)\n")
            append("• Do your hardest task when your energy is highest\n")
            append("• Review and plan for tomorrow before ending your day\n")
            
            append("\nRemember: A good system is one you'll actually use consistently!")
        }
    }
    
    private suspend fun handleFocusQuery(input: String, context: AgentContext): String {
        // Check if user might be stressed or distracted based on sensor data
        val stressResult = executeTool("stress_detection", mapOf("time_range_minutes" to 15))
        val activityResult = executeTool("activity_recognition", emptyMap())
        
        return buildString {
            append("Focus is your superpower in a distracted world! Let me help you sharpen it. ")
            
            val stressData = stressResult.getDataOrNull()
            val stressLevel = stressData?.get("stress_level") as? String
            
            if (stressLevel == "high") {
                append("I notice your stress levels seem elevated, which can definitely impact focus. ")
                append("Let's address that first with some quick stress-reduction techniques. ")
            }
            
            append("\n\nFocus enhancement techniques:\n")
            append("• Pomodoro Technique: 25 minutes focused work, 5 minute break\n")
            append("• Deep Work blocks: 90-120 minutes of uninterrupted focus\n")
            append("• Single-tasking: Do one thing at a time, completely\n")
            append("• Environment design: Remove distractions from your workspace\n")
            append("• Digital minimalism: Turn off non-essential notifications\n")
            
            append("\nThe Focus Formula:\n")
            append("1. Clear intention: Know exactly what you want to accomplish\n")
            append("2. Eliminate distractions: Phone away, notifications off\n")
            append("3. Optimize environment: Good lighting, comfortable temperature\n")
            append("4. Prime your brain: Light exercise or meditation beforehand\n")
            append("5. Start small: Begin with just 15-20 minutes of focused work\n")
            
            append("\nDealing with distractions:\n")
            append("• Internal distractions: Keep a 'capture' notepad for random thoughts\n")
            append("• Digital distractions: Use website blockers, app timers\n")
            append("• People distractions: Set boundaries, use 'focus signals'\n")
            append("• Environmental: Find or create a dedicated focus space\n")
            
            if (stressLevel == "high") {
                append("\nQuick stress-busting for better focus:\n")
                append("• 4-7-8 breathing: Inhale 4, hold 7, exhale 8\n")
                append("• Progressive muscle relaxation: Tense and release muscle groups\n")
                append("• 5-minute walk: Movement helps reset your mental state\n")
            }
            
            append("\nFocus nutrition:\n")
            append("• Stay hydrated: Even mild dehydration affects concentration\n")
            append("• Stable blood sugar: Avoid sugar crashes with balanced meals\n")
            append("• Brain foods: Blueberries, nuts, dark chocolate, green tea\n")
            
            append("\nRemember: Focus is like a muscle - the more you train it, the stronger it gets!")
        }
    }
    
    private suspend fun handleGoalsQuery(input: String, context: AgentContext): String {
        return buildString {
            append("Goal setting is the bridge between dreams and reality! Let's build that bridge together. ")
            
            append("\n\nSMART Goals framework:\n")
            append("• Specific: Clearly defined, not vague\n")
            append("• Measurable: You can track progress objectively\n")
            append("• Achievable: Challenging but realistic\n")
            append("• Relevant: Aligned with your values and priorities\n")
            append("• Time-bound: Has a clear deadline\n")
            
            append("\nGoal hierarchy:\n")
            append("1. Vision: Your long-term life direction (10+ years)\n")
            append("2. Goals: Specific outcomes you want to achieve (1-3 years)\n")
            append("3. Objectives: Milestones on the way to goals (3-12 months)\n")
            append("4. Tasks: Specific actions you can take (daily/weekly)\n")
            
            append("\nGoal setting process:\n")
            append("1. Brainstorm: Write down everything you might want to achieve\n")
            append("2. Prioritize: Choose 3-5 most important goals\n")
            append("3. Make them SMART: Apply the framework to each goal\n")
            append("4. Break down: Identify key milestones and next actions\n")
            append("5. Schedule: Put goal-related tasks in your calendar\n")
            append("6. Review: Check progress weekly, adjust as needed\n")
            
            append("\nGoal categories to consider:\n")
            append("• Career/Professional: Skills, promotions, career changes\n")
            append("• Health/Fitness: Exercise, nutrition, medical checkups\n")
            append("• Financial: Savings, debt reduction, investments\n")
            append("• Relationships: Family, friends, networking\n")
            append("• Personal Growth: Learning, hobbies, experiences\n")
            append("• Contribution: Volunteering, mentoring, community service\n")
            
            append("\nGoal achievement strategies:\n")
            append("• Implementation intentions: 'When X happens, I will do Y'\n")
            append("• Habit stacking: Attach new behaviors to existing habits\n")
            append("• Accountability: Share goals with others or track publicly\n")
            append("• Visualization: Regularly imagine achieving your goals\n")
            append("• Celebrate milestones: Acknowledge progress along the way\n")
            
            append("\nRemember: Goals are dreams with deadlines. Make them specific, make them yours, and make them happen!")
        }
    }
    
    private suspend fun handleHabitsQuery(input: String, context: AgentContext): String {
        return buildString {
            append("Habits are the compound interest of self-improvement! Small changes, massive results over time. ")
            
            append("\n\nThe Habit Loop:\n")
            append("1. Cue: The trigger that starts the habit\n")
            append("2. Routine: The behavior itself\n")
            append("3. Reward: The benefit you get from the behavior\n")
            
            append("Understanding this loop helps you build good habits and break bad ones.\n")
            
            append("\nBuilding new habits:\n")
            append("• Start ridiculously small: 1 push-up, 1 page, 2 minutes\n")
            append("• Stack habits: Attach new habit to existing routine\n")
            append("• Design your environment: Make good habits obvious and easy\n")
            append("• Track your progress: Use a habit tracker or calendar\n")
            append("• Focus on identity: 'I am someone who exercises daily'\n")
            
            append("\nThe 4 Laws of Behavior Change:\n")
            append("1. Make it Obvious: Clear cues, visible reminders\n")
            append("2. Make it Attractive: Pair with something you enjoy\n")
            append("3. Make it Easy: Reduce friction, lower the barrier\n")
            append("4. Make it Satisfying: Immediate reward or tracking\n")
            
            append("\nBreaking bad habits:\n")
            append("• Make it Invisible: Remove cues from environment\n")
            append("• Make it Unattractive: Focus on negative consequences\n")
            append("• Make it Difficult: Add friction, increase barriers\n")
            append("• Make it Unsatisfying: Find accountability partner\n")
            
            append("\nHabit stacking examples:\n")
            append("• After I pour my morning coffee, I will write in my journal\n")
            append("• After I sit down at my desk, I will review my daily priorities\n")
            append("• After I put on my pajamas, I will prepare tomorrow's clothes\n")
            
            append("\nCommon habit-building mistakes:\n")
            append("• Starting too big: Aim for 1% better, not 100% different\n")
            append("• Trying to change everything at once: Focus on one habit at a time\n")
            append("• Expecting immediate results: Habits take 21-66 days to form\n")
            append("• Not having a system: Track progress and review regularly\n")
            
            append("\nRemember: You don't rise to the level of your goals, you fall to the level of your systems!")
        }
    }
    
    private suspend fun handleProcrastinationQuery(input: String, context: AgentContext): String {
        return buildString {
            append("Procrastination is the thief of time, but we can catch it! Let's understand why it happens and how to beat it. ")
            
            append("\n\nWhy we procrastinate:\n")
            append("• Task seems too big or overwhelming\n")
            append("• Fear of failure or perfectionism\n")
            append("• Lack of clarity about what to do\n")
            append("• Task is boring or unpleasant\n")
            append("• Feeling tired or low energy\n")
            append("• Too many distractions available\n")
            
            append("\nAnti-procrastination strategies:\n")
            append("• The 2-minute rule: If it takes less than 2 minutes, do it now\n")
            append("• The 5-minute rule: Commit to just 5 minutes on the task\n")
            append("• Break it down: Divide big tasks into tiny, specific steps\n")
            append("• Eat the frog: Do your hardest task first thing in the morning\n")
            append("• Use implementation intentions: 'At 2pm, I will work on X for 30 minutes'\n")
            
            append("\nThe Procrastination Equation:\n")
            append("Motivation = (Expectancy × Value) / (Impulsiveness × Delay)\n")
            append("• Increase expectancy: Believe you can succeed\n")
            append("• Increase value: Connect task to your goals and values\n")
            append("• Decrease impulsiveness: Remove distractions\n")
            append("• Decrease delay: Make rewards more immediate\n")
            
            append("\nDealing with perfectionism:\n")
            append("• Aim for 'good enough' first drafts\n")
            append("• Set time limits for tasks\n")
            append("• Remember: Done is better than perfect\n")
            append("• Focus on progress, not perfection\n")
            
            append("\nEnvironment design:\n")
            append("• Remove temptations from your workspace\n")
            append("• Use website blockers during focus time\n")
            append("• Keep your phone in another room\n")
            append("• Have everything you need within reach\n")
            
            append("\nMotivation boosters:\n")
            append("• Visualize the satisfaction of completion\n")
            append("• Remember your 'why' - connect to bigger purpose\n")
            append("• Use positive self-talk: 'I can do this'\n")
            append("• Reward yourself for starting, not just finishing\n")
            
            append("\nRemember: The best time to plant a tree was 20 years ago. The second best time is now!")
        }
    }
    
    private suspend fun handleWorkLifeBalanceQuery(input: String, context: AgentContext): String {
        return buildString {
            append("Work-life balance isn't about perfect equilibrium - it's about conscious choices and boundaries. ")
            
            append("\n\nWork-life integration principles:\n")
            append("• Set clear boundaries between work and personal time\n")
            append("• Be fully present in whatever you're doing\n")
            append("• Regularly assess and adjust your priorities\n")
            append("• Learn to say no to non-essential commitments\n")
            append("• Schedule personal time like you would important meetings\n")
            
            append("\nBoundary setting strategies:\n")
            append("• Physical boundaries: Separate workspace if possible\n")
            append("• Time boundaries: Set specific work hours and stick to them\n")
            append("• Digital boundaries: Turn off work notifications after hours\n")
            append("• Mental boundaries: Practice transitioning between work and personal mindset\n")
            
            append("\nTime management for balance:\n")
            append("• Use time blocking for both work and personal activities\n")
            append("• Batch similar activities together\n")
            append("• Delegate or outsource when possible\n")
            append("• Regularly review and eliminate low-value activities\n")
            
            append("\nStress management:\n")
            append("• Take regular breaks throughout the day\n")
            append("• Practice deep breathing or meditation\n")
            append("• Get adequate sleep (7-9 hours)\n")
            append("• Exercise regularly, even if just short walks\n")
            append("• Maintain social connections outside of work\n")
            
            append("\nSigns you need better balance:\n")
            append("• Constantly thinking about work during personal time\n")
            append("• Feeling guilty when not working\n")
            append("• Neglecting relationships, health, or hobbies\n")
            append("• Chronic fatigue or stress symptoms\n")
            append("• Decreased productivity despite long hours\n")
            
            append("\nWeekly balance check:\n")
            append("• How many hours did I work vs. personal time?\n")
            append("• Did I engage in activities I enjoy?\n")
            append("• How are my energy levels?\n")
            append("• Am I maintaining important relationships?\n")
            append("• What needs adjustment next week?\n")
            
            append("\nRemember: You can't pour from an empty cup. Taking care of yourself isn't selfish - it's necessary!")
        }
    }
    
    private suspend fun handleGeneralQuery(input: String, context: AgentContext): String {
        return "Hello! I'm Pat, your productivity assistant. I'm here to help you optimize your time, organize your tasks, achieve your goals, and create systems that work for you. Whether you're struggling with procrastination, trying to build better habits, or looking to improve your focus, I've got evidence-based strategies to help you become more effective and efficient. What productivity challenge can we tackle together today?"
    }
    
    override suspend fun onOtherAgentResponse(otherAgentId: String, response: String) {
        // Productivity agent can coordinate with other agents for holistic optimization
        Timber.d("Productivity agent received input from $otherAgentId: ${response.take(50)}...")
        
        // Could suggest productivity techniques based on health/stress data
        // Or help organize financial goals and health routines
    }
}