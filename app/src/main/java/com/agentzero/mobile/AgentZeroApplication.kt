package com.agentzero.mobile

import android.app.Application
import com.agentzero.core.AgentFramework
import com.agentzero.agents.AgentRegistry
import com.agentzero.sensors.SensorToolManager
import com.agentzero.voice.VoiceManager
import com.agentzero.knowledge.KnowledgeStore
import com.agentzero.security.SecurityManager
import com.agentzero.briar.BriarBridge
import com.agentzero.commons.CommonsClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Main application class for Agent Zero Mobile
 * 
 * Initializes all core systems:
 * - Multi-agent framework
 * - Sensor integration
 * - Voice synthesis
 * - Knowledge store
 * - Security systems
 * - Briar messaging
 * - Microagent commons
 */
class AgentZeroApplication : Application() {
    
    // Application-wide coroutine scope
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    // Core systems
    lateinit var agentFramework: AgentFramework
        private set
    
    lateinit var agentRegistry: AgentRegistry
        private set
        
    lateinit var sensorManager: SensorToolManager
        private set
        
    lateinit var voiceManager: VoiceManager
        private set
        
    lateinit var knowledgeStore: KnowledgeStore
        private set
        
    lateinit var securityManager: SecurityManager
        private set
        
    lateinit var briarBridge: BriarBridge
        private set
        
    lateinit var commonsClient: CommonsClient
        private set
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize core systems in dependency order
        initializeSecurity()
        initializeKnowledgeStore()
        initializeSensorManager()
        initializeVoiceManager()
        initializeAgentFramework()
        initializeBriarBridge()
        initializeCommonsClient()
        
        // Register default agents
        registerDefaultAgents()
    }
    
    private fun initializeSecurity() {
        securityManager = SecurityManager(this)
        securityManager.initialize()
    }
    
    private fun initializeKnowledgeStore() {
        knowledgeStore = KnowledgeStore(this, securityManager)
        knowledgeStore.initialize()
    }
    
    private fun initializeSensorManager() {
        sensorManager = SensorToolManager(this, securityManager)
        sensorManager.initialize()
    }
    
    private fun initializeVoiceManager() {
        voiceManager = VoiceManager(this)
        voiceManager.initialize()
    }
    
    private fun initializeAgentFramework() {
        agentFramework = AgentFramework(
            context = this,
            sensorManager = sensorManager,
            voiceManager = voiceManager,
            knowledgeStore = knowledgeStore,
            securityManager = securityManager
        )
        
        agentRegistry = AgentRegistry(agentFramework)
        agentFramework.initialize()
    }
    
    private fun initializeBriarBridge() {
        briarBridge = BriarBridge(this, agentFramework, securityManager)
        briarBridge.initialize()
    }
    
    private fun initializeCommonsClient() {
        commonsClient = CommonsClient(this, securityManager)
        commonsClient.initialize()
    }
    
    private fun registerDefaultAgents() {
        // Register built-in agent types
        agentRegistry.registerAgentType("health", "com.agentzero.agents.HealthAgent")
        agentRegistry.registerAgentType("fitness", "com.agentzero.agents.FitnessAgent")
        agentRegistry.registerAgentType("finance", "com.agentzero.agents.FinanceAgent")
        agentRegistry.registerAgentType("productivity", "com.agentzero.agents.ProductivityAgent")
        agentRegistry.registerAgentType("wellness", "com.agentzero.agents.WellnessAgent")
        
        // Create default agent instances
        agentRegistry.createAgent("health", "Dr. Sarah")
        agentRegistry.createAgent("fitness", "Coach Mike")
        agentRegistry.createAgent("finance", "Advisor Alex")
    }
    
    override fun onTerminate() {
        super.onTerminate()
        
        // Cleanup in reverse order
        commonsClient.shutdown()
        briarBridge.shutdown()
        agentFramework.shutdown()
        voiceManager.shutdown()
        sensorManager.shutdown()
        knowledgeStore.shutdown()
        securityManager.shutdown()
    }
}