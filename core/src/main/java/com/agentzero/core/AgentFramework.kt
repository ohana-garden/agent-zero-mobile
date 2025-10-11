package com.agentzero.core

import android.content.Context
import com.agentzero.sensors.SensorToolManager
import com.agentzero.voice.VoiceManager
import com.agentzero.knowledge.KnowledgeStore
import com.agentzero.security.SecurityManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.Serializable
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap

/**
 * Core Agent Framework
 * 
 * Manages the lifecycle and coordination of multiple AI agents.
 * Provides the foundation for multi-agent conversations and tool usage.
 */
class AgentFramework(
    private val context: Context,
    private val sensorManager: SensorToolManager,
    private val voiceManager: VoiceManager,
    private val knowledgeStore: KnowledgeStore,
    private val securityManager: SecurityManager
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val agents = ConcurrentHashMap<String, Agent>()
    private val tools = ConcurrentHashMap<String, AgentTool>()
    
    // Event flows for agent coordination
    private val _agentEvents = MutableSharedFlow<AgentEvent>()
    val agentEvents: SharedFlow<AgentEvent> = _agentEvents.asSharedFlow()
    
    private val _userInputs = MutableSharedFlow<UserInput>()
    val userInputs: SharedFlow<UserInput> = _userInputs.asSharedFlow()
    
    fun initialize() {
        Timber.d("Initializing Agent Framework")
        
        // Register core tools
        registerCoreTool("sensor_query", SensorQueryTool(sensorManager))
        registerCoreTool("knowledge_query", KnowledgeQueryTool(knowledgeStore))
        registerCoreTool("agent_coordinate", AgentCoordinationTool(this))
        
        // Start event processing
        startEventProcessing()
        
        Timber.d("Agent Framework initialized")
    }
    
    fun registerAgent(agent: Agent) {
        agents[agent.id] = agent
        agent.initialize(this)
        
        scope.launch {
            _agentEvents.emit(AgentEvent.AgentRegistered(agent.id, agent.type))
        }
        
        Timber.d("Registered agent: ${agent.name} (${agent.type})")
    }
    
    fun unregisterAgent(agentId: String) {
        agents.remove(agentId)?.let { agent ->
            agent.shutdown()
            scope.launch {
                _agentEvents.emit(AgentEvent.AgentUnregistered(agentId))
            }
            Timber.d("Unregistered agent: $agentId")
        }
    }
    
    fun getAgent(agentId: String): Agent? = agents[agentId]
    
    fun getAllAgents(): List<Agent> = agents.values.toList()
    
    fun getActiveAgents(): List<Agent> = agents.values.filter { it.isActive }
    
    fun registerCoreTool(name: String, tool: AgentTool) {
        tools[name] = tool
        Timber.d("Registered core tool: $name")
    }
    
    fun getTool(name: String): AgentTool? = tools[name]
    
    suspend fun processUserInput(input: String): Map<Agent, String> {
        Timber.d("Processing user input: $input")
        
        val userInput = UserInput(
            text = input,
            timestamp = System.currentTimeMillis(),
            context = gatherContext()
        )
        
        _userInputs.emit(userInput)
        
        // Determine which agents should respond
        val relevantAgents = determineRelevantAgents(userInput)
        
        // Process input through each relevant agent
        val responses = mutableMapOf<Agent, String>()
        
        relevantAgents.forEach { agent ->
            try {
                val response = agent.processInput(userInput)
                if (response.isNotBlank()) {
                    responses[agent] = response
                }
            } catch (e: Exception) {
                Timber.e(e, "Error processing input for agent ${agent.name}")
            }
        }
        
        // Store interaction in knowledge store
        storeInteraction(userInput, responses)
        
        return responses
    }
    
    private suspend fun determineRelevantAgents(input: UserInput): List<Agent> {
        // Simple keyword-based routing for now
        // TODO: Use more sophisticated intent classification
        
        val activeAgents = getActiveAgents()
        val text = input.text.lowercase()
        
        return when {
            // Health-related keywords
            text.contains("health") || text.contains("heart") || text.contains("blood") ||
            text.contains("exercise") || text.contains("sleep") || text.contains("stress") -> {
                activeAgents.filter { it.type in listOf("health", "fitness", "wellness") }
            }
            
            // Finance-related keywords
            text.contains("money") || text.contains("budget") || text.contains("invest") ||
            text.contains("save") || text.contains("spend") || text.contains("cost") -> {
                activeAgents.filter { it.type == "finance" }
            }
            
            // Fitness-related keywords
            text.contains("workout") || text.contains("run") || text.contains("gym") ||
            text.contains("fitness") || text.contains("training") -> {
                activeAgents.filter { it.type in listOf("fitness", "health") }
            }
            
            // General queries - all agents can respond
            else -> activeAgents.take(3) // Limit to 3 agents to avoid overwhelming user
        }
    }
    
    private suspend fun gatherContext(): InputContext {
        // Gather contextual information for agents
        return InputContext(
            timestamp = System.currentTimeMillis(),
            recentSensorData = sensorManager.getRecentData(),
            conversationHistory = knowledgeStore.getRecentConversations(limit = 5),
            userPreferences = knowledgeStore.getUserPreferences()
        )
    }
    
    private suspend fun storeInteraction(
        input: UserInput,
        responses: Map<Agent, String>
    ) {
        try {
            knowledgeStore.storeInteraction(
                userInput = input.text,
                agentResponses = responses.mapKeys { it.key.id },
                timestamp = input.timestamp,
                context = input.context
            )
        } catch (e: Exception) {
            Timber.e(e, "Error storing interaction in knowledge store")
        }
    }
    
    private fun startEventProcessing() {
        // Process agent events for coordination
        scope.launch {
            agentEvents.collect { event ->
                when (event) {
                    is AgentEvent.AgentRegistered -> {
                        Timber.d("Agent registered: ${event.agentId}")
                    }
                    is AgentEvent.AgentUnregistered -> {
                        Timber.d("Agent unregistered: ${event.agentId}")
                    }
                    is AgentEvent.AgentResponse -> {
                        // Handle agent responses for coordination
                        handleAgentResponse(event)
                    }
                }
            }
        }
    }
    
    private suspend fun handleAgentResponse(event: AgentEvent.AgentResponse) {
        // Notify other agents about responses for coordination
        val otherAgents = agents.values.filter { it.id != event.agentId }
        
        otherAgents.forEach { agent ->
            try {
                agent.onOtherAgentResponse(event.agentId, event.response)
            } catch (e: Exception) {
                Timber.e(e, "Error notifying agent ${agent.name} of other agent response")
            }
        }
    }
    
    fun shutdown() {
        Timber.d("Shutting down Agent Framework")
        
        // Shutdown all agents
        agents.values.forEach { it.shutdown() }
        agents.clear()
        
        // Clear tools
        tools.clear()
        
        // Cancel scope
        scope.cancel()
        
        Timber.d("Agent Framework shutdown complete")
    }
}

/**
 * Events for agent coordination
 */
sealed class AgentEvent {
    data class AgentRegistered(val agentId: String, val agentType: String) : AgentEvent()
    data class AgentUnregistered(val agentId: String) : AgentEvent()
    data class AgentResponse(val agentId: String, val response: String, val timestamp: Long) : AgentEvent()
}

/**
 * User input with context
 */
@Serializable
data class UserInput(
    val text: String,
    val timestamp: Long,
    val context: InputContext
)

/**
 * Context information for processing user input
 */
@Serializable
data class InputContext(
    val timestamp: Long,
    val recentSensorData: Map<String, String> = emptyMap(),
    val conversationHistory: List<String> = emptyList(),
    val userPreferences: Map<String, String> = emptyMap()
)