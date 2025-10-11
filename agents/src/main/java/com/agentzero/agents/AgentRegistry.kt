package com.agentzero.agents

import com.agentzero.core.Agent
import com.agentzero.core.AgentFramework
import com.agentzero.core.VoiceProfile
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap

/**
 * Registry for managing agent types and instances
 * 
 * Handles creation, activation, and lifecycle management of specialized agents.
 */
class AgentRegistry(
    private val framework: AgentFramework
) {
    private val agentTypes = ConcurrentHashMap<String, String>()
    private val agentInstances = ConcurrentHashMap<String, Agent>()
    
    fun registerAgentType(type: String, className: String) {
        agentTypes[type] = className
        Timber.d("Registered agent type: $type -> $className")
    }
    
    fun createAgent(type: String, name: String): Agent? {
        val className = agentTypes[type] ?: return null
        
        val agent = when (type) {
            "health" -> createHealthAgent(name)
            "fitness" -> createFitnessAgent(name)
            "finance" -> createFinanceAgent(name)
            "productivity" -> createProductivityAgent(name)
            "wellness" -> createWellnessAgent(name)
            else -> {
                Timber.w("Unknown agent type: $type")
                return null
            }
        }
        
        if (agent != null) {
            agentInstances[agent.id] = agent
            framework.registerAgent(agent)
            Timber.d("Created agent: $name ($type) with ID ${agent.id}")
        }
        
        return agent
    }
    
    private fun createHealthAgent(name: String): HealthAgent {
        return HealthAgent(
            name = name,
            voiceProfile = VoiceProfile(
                name = "Dr. Sarah",
                model = "sherpa-onnx-female-calm",
                gender = "female",
                speed = 0.9f,
                pitch = 1.0f
            )
        )
    }
    
    private fun createFitnessAgent(name: String): FitnessAgent {
        return FitnessAgent(
            name = name,
            voiceProfile = VoiceProfile(
                name = "Coach Mike",
                model = "sherpa-onnx-male-energetic",
                gender = "male",
                speed = 1.1f,
                pitch = 1.1f
            )
        )
    }
    
    private fun createFinanceAgent(name: String): FinanceAgent {
        return FinanceAgent(
            name = name,
            voiceProfile = VoiceProfile(
                name = "Advisor Alex",
                model = "sherpa-onnx-neutral-professional",
                gender = "neutral",
                speed = 1.0f,
                pitch = 0.95f
            )
        )
    }
    
    private fun createProductivityAgent(name: String): ProductivityAgent {
        return ProductivityAgent(
            name = name,
            voiceProfile = VoiceProfile(
                name = "Assistant Pat",
                model = "sherpa-onnx-neutral-efficient",
                gender = "neutral",
                speed = 1.05f,
                pitch = 1.0f
            )
        )
    }
    
    private fun createWellnessAgent(name: String): WellnessAgent {
        return WellnessAgent(
            name = name,
            voiceProfile = VoiceProfile(
                name = "Zen Maya",
                model = "sherpa-onnx-female-soothing",
                gender = "female",
                speed = 0.85f,
                pitch = 0.9f
            )
        )
    }
    
    fun getAgent(agentId: String): Agent? = agentInstances[agentId]
    
    fun getAllAgents(): List<Agent> = agentInstances.values.toList()
    
    fun getActiveAgents(): List<Agent> = agentInstances.values.filter { it.isActive }
    
    fun getAgentsByType(type: String): List<Agent> = agentInstances.values.filter { it.type == type }
    
    fun activateAgent(agentId: String): Boolean {
        val agent = agentInstances[agentId]
        return if (agent != null) {
            agent.activate()
            true
        } else {
            false
        }
    }
    
    fun deactivateAgent(agentId: String): Boolean {
        val agent = agentInstances[agentId]
        return if (agent != null) {
            agent.deactivate()
            true
        } else {
            false
        }
    }
    
    fun removeAgent(agentId: String): Boolean {
        val agent = agentInstances.remove(agentId)
        return if (agent != null) {
            framework.unregisterAgent(agentId)
            agent.shutdown()
            true
        } else {
            false
        }
    }
    
    fun getRegisteredTypes(): List<String> = agentTypes.keys.toList()
    
    fun shutdown() {
        Timber.d("Shutting down Agent Registry")
        
        // Shutdown all agent instances
        agentInstances.values.forEach { it.shutdown() }
        agentInstances.clear()
        agentTypes.clear()
        
        Timber.d("Agent Registry shutdown complete")
    }
}