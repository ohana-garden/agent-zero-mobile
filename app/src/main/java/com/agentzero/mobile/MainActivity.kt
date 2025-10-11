package com.agentzero.mobile

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.agentzero.mobile.ui.theme.AgentZeroMobileTheme
import com.agentzero.agents.Agent
import kotlinx.coroutines.launch

/**
 * Main activity for Agent Zero Mobile
 * 
 * Provides the primary user interface for:
 * - Voice interaction with multiple agents
 * - Agent status and management
 * - Permission management
 * - System configuration
 */
class MainActivity : ComponentActivity() {
    
    private val app by lazy { application as AgentZeroApplication }
    
    // Permission launcher for runtime permissions
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            initializeAgentSystem()
        } else {
            // Handle permission denial
            showPermissionDeniedDialog()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            AgentZeroMobileTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
        
        // Check and request permissions
        checkPermissions()
    }
    
    @Composable
    private fun MainScreen() {
        var activeAgents by remember { mutableStateOf<List<Agent>>(emptyList()) }
        var isListening by remember { mutableStateOf(false) }
        var lastMessage by remember { mutableStateOf("") }
        
        val scope = rememberCoroutineScope()
        
        // Update active agents
        LaunchedEffect(Unit) {
            activeAgents = app.agentRegistry.getActiveAgents()
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Text(
                text = "Agent Zero Mobile",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Voice interaction section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Voice Interaction",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Button(
                        onClick = {
                            scope.launch {
                                if (isListening) {
                                    stopListening()
                                    isListening = false
                                } else {
                                    startListening()
                                    isListening = true
                                }
                            }
                        },
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Text(if (isListening) "Stop Listening" else "Start Listening")
                    }
                    
                    if (lastMessage.isNotEmpty()) {
                        Text(
                            text = "Last: $lastMessage",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Active agents section
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Active Agents (${activeAgents.size})",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    LazyColumn {
                        items(activeAgents) { agent ->
                            AgentCard(
                                agent = agent,
                                onToggle = { enabled ->
                                    scope.launch {
                                        if (enabled) {
                                            app.agentRegistry.activateAgent(agent.id)
                                        } else {
                                            app.agentRegistry.deactivateAgent(agent.id)
                                        }
                                        activeAgents = app.agentRegistry.getActiveAgents()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
    
    @Composable
    private fun AgentCard(
        agent: Agent,
        onToggle: (Boolean) -> Unit
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = agent.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = agent.type,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Voice: ${agent.voiceProfile.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Switch(
                    checked = agent.isActive,
                    onCheckedChange = onToggle
                )
            }
        }
    }
    
    private fun checkPermissions() {
        val requiredPermissions = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.BODY_SENSORS,
            Manifest.permission.ACTIVITY_RECOGNITION,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE
        )
        
        val missingPermissions = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        
        if (missingPermissions.isNotEmpty()) {
            permissionLauncher.launch(missingPermissions.toTypedArray())
        } else {
            initializeAgentSystem()
        }
    }
    
    private fun initializeAgentSystem() {
        // Agent system is already initialized in Application
        // This is where we'd start any activity-specific initialization
    }
    
    private suspend fun startListening() {
        app.voiceManager.startListening { transcript ->
            // Process voice input through agent framework
            processVoiceInput(transcript)
        }
    }
    
    private suspend fun stopListening() {
        app.voiceManager.stopListening()
    }
    
    private suspend fun processVoiceInput(transcript: String) {
        // Send to agent framework for processing
        val responses = app.agentFramework.processUserInput(transcript)
        
        // Speak responses using different agent voices
        responses.forEach { (agent, response) ->
            app.voiceManager.speakAsAgent(agent, response)
        }
    }
    
    private fun showPermissionDeniedDialog() {
        // TODO: Show dialog explaining why permissions are needed
    }
}