package com.agentzero.core

import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import timber.log.Timber
import java.util.*

/**
 * Base class for all AI agents in the system
 * 
 * Each agent represents a specialized AI advisor (health, finance, etc.)
 * with its own personality, voice, and capabilities.
 */
abstract class Agent(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: String,
    val voiceProfile: VoiceProfile
) {
    private var framework: AgentFramework? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    var isActive: Boolean = false
        private set
    
    var isInitialized: Boolean = false
        private set
    
    // Agent's specialized tools
    abstract val tools: List<AgentTool>
    
    // Agent's personality and behavior configuration
    abstract val personality: AgentPersonality
    
    // Agent's knowledge domains
    abstract val knowledgeDomains: List<String>
    
    /**
     * Initialize the agent with the framework
     */
    fun initialize(framework: AgentFramework) {
        this.framework = framework
        
        // Register agent's tools with framework
        tools.forEach { tool ->
            framework.registerCoreTool("${type}_${tool.name}", tool)
        }
        
        onInitialize()
        isInitialized = true
        isActive = true
        
        Timber.d("Agent $name initialized")
    }
    
    /**
     * Process user input and generate response
     */
    suspend fun processInput(input: UserInput): String {
        if (!isActive || !isInitialized) {
            return ""
        }
        
        return try {
            val context = buildContext(input)
            val response = generateResponse(input.text, context)
            
            // Notify framework of response
            framework?.let { fw ->
                fw.agentEvents.collect { event ->
                    if (event is AgentEvent.AgentResponse && event.agentId == id) {
                        return@collect
                    }
                }
            }
            
            response
        } catch (e: Exception) {
            Timber.e(e, "Error processing input for agent $name")
            "I'm sorry, I encountered an error processing your request."
        }
    }
    
    /**
     * Handle responses from other agents for coordination
     */
    open suspend fun onOtherAgentResponse(otherAgentId: String, response: String) {
        // Default implementation - agents can override for coordination
        Timber.d("Agent $name received response from $otherAgentId")
    }
    
    /**
     * Build context for response generation
     */
    private suspend fun buildContext(input: UserInput): AgentContext {
        val framework = this.framework ?: return AgentContext.empty()
        
        return AgentContext(
            userInput = input,
            agentPersonality = personality,
            availableTools = tools.map { it.name },
            knowledgeDomains = knowledgeDomains,
            recentInteractions = getRecentInteractions(),
            sensorData = getSensorData(),
            knowledgeBase = getRelevantKnowledge(input.text)
        )
    }
    
    /**
     * Generate response based on input and context
     * This is where the actual AI processing happens
     */
    protected abstract suspend fun generateResponse(input: String, context: AgentContext): String
    
    /**
     * Get recent interactions for context
     */
    private suspend fun getRecentInteractions(): List<String> {
        return framework?.let { fw ->
            // Get recent interactions from knowledge store
            // TODO: Implement knowledge store query
            emptyList()
        } ?: emptyList()
    }
    
    /**
     * Get relevant sensor data for this agent type
     */
    private suspend fun getSensorData(): Map<String, Any> {
        return framework?.let { fw ->
            val sensorTool = fw.getTool("sensor_query") as? SensorQueryTool
            sensorTool?.getRelevantData(knowledgeDomains) ?: emptyMap()
        } ?: emptyMap()
    }
    
    /**
     * Get relevant knowledge from knowledge store
     */
    private suspend fun getRelevantKnowledge(query: String): List<String> {
        return framework?.let { fw ->
            val knowledgeTool = fw.getTool("knowledge_query") as? KnowledgeQueryTool
            knowledgeTool?.query(query, knowledgeDomains) ?: emptyList()
        } ?: emptyList()
    }
    
    /**
     * Execute a tool and return the result
     */
    protected suspend fun executeTool(toolName: String, parameters: Map<String, Any>): ToolResult {
        val tool = tools.find { it.name == toolName }
            ?: framework?.getTool(toolName)
            ?: return ToolResult.error("Tool not found: $toolName")
        
        return try {
            tool.execute(parameters)
        } catch (e: Exception) {
            Timber.e(e, "Error executing tool $toolName")
            ToolResult.error("Tool execution failed: ${e.message}")
        }
    }
    
    /**
     * Activate the agent
     */
    fun activate() {
        isActive = true
        onActivate()
        Timber.d("Agent $name activated")
    }
    
    /**
     * Deactivate the agent
     */
    fun deactivate() {
        isActive = false
        onDeactivate()
        Timber.d("Agent $name deactivated")
    }
    
    /**
     * Shutdown the agent
     */
    fun shutdown() {
        isActive = false
        onShutdown()
        scope.cancel()
        Timber.d("Agent $name shutdown")
    }
    
    // Lifecycle hooks for subclasses
    protected open fun onInitialize() {}
    protected open fun onActivate() {}
    protected open fun onDeactivate() {}
    protected open fun onShutdown() {}
    
    override fun toString(): String {
        return "Agent(id='$id', name='$name', type='$type', active=$isActive)"
    }
}

/**
 * Voice profile for agent speech synthesis
 */
@Serializable
data class VoiceProfile(
    val name: String,
    val model: String = "default",
    val gender: String = "neutral",
    val language: String = "en-US",
    val speed: Float = 1.0f,
    val pitch: Float = 1.0f
)

/**
 * Agent personality configuration
 */
@Serializable
data class AgentPersonality(
    val traits: Map<String, Float> = emptyMap(), // e.g., "empathy" -> 0.8
    val communicationStyle: String = "professional", // professional, casual, friendly, etc.
    val responseLength: String = "medium", // short, medium, long
    val expertise: List<String> = emptyList(),
    val limitations: List<String> = emptyList()
)

/**
 * Context provided to agents for response generation
 */
data class AgentContext(
    val userInput: UserInput,
    val agentPersonality: AgentPersonality,
    val availableTools: List<String>,
    val knowledgeDomains: List<String>,
    val recentInteractions: List<String>,
    val sensorData: Map<String, Any>,
    val knowledgeBase: List<String>
) {
    companion object {
        fun empty() = AgentContext(
            userInput = UserInput("", 0, InputContext(0)),
            agentPersonality = AgentPersonality(),
            availableTools = emptyList(),
            knowledgeDomains = emptyList(),
            recentInteractions = emptyList(),
            sensorData = emptyMap(),
            knowledgeBase = emptyList()
        )
    }
}