package com.agentzero.mobile

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        var conversationHistory by remember { mutableStateOf(listOf<ConversationMessage>()) }
        var textInput by remember { mutableStateOf("") }
        
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
            // Header with security status
            AgentZeroHeader()
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Active agents section
            AgentSelectionCard(
                activeAgents = activeAgents,
                onToggleAgent = { agent, enabled ->
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
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Conversation area
            ConversationArea(
                conversationHistory = conversationHistory,
                modifier = Modifier.weight(1f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Voice and text input controls
            VoiceControlsRow(
                textInput = textInput,
                onTextInputChange = { textInput = it },
                isListening = isListening,
                onStartListening = {
                    scope.launch {
                        startListening()
                        isListening = true
                    }
                },
                onStopListening = {
                    scope.launch {
                        stopListening()
                        isListening = false
                    }
                },
                onSendMessage = { message ->
                    if (message.isNotBlank()) {
                        scope.launch {
                            // Add user message to conversation
                            conversationHistory = conversationHistory + ConversationMessage(message, true)
                            
                            // Process through agents
                            val responses = app.agentFramework.processUserInput(message)
                            
                            // Add agent responses to conversation
                            responses.forEach { (agent, response) ->
                                conversationHistory = conversationHistory + ConversationMessage(
                                    "${agent.name}: $response", false
                                )
                                // Speak the response
                                app.voiceManager.speakAsAgent(agent, response)
                            }
                            
                            lastMessage = message
                            textInput = ""
                        }
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Status bar
            StatusBar()
        }
    }
    
    @Composable
    private fun AgentZeroHeader() {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Agent Zero Mobile",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Text(
                    text = "NSA-Defeating Multi-Agent AI Advisory Team",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
    
    @Composable
    private fun AgentSelectionCard(
        activeAgents: List<Agent>,
        onToggleAgent: (Agent, Boolean) -> Unit
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AI Advisory Team (${activeAgents.size} active)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(activeAgents) { agent ->
                        EnhancedAgentCard(
                            agent = agent,
                            onToggle = { enabled -> onToggleAgent(agent, enabled) }
                        )
                    }
                }
            }
        }
    }
    
    @Composable
    private fun ConversationArea(
        conversationHistory: List<ConversationMessage>,
        modifier: Modifier = Modifier
    ) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            if (conversationHistory.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Start your conversation with the AI advisory team",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Use voice or text input below",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(conversationHistory) { message ->
                        MessageBubble(message)
                    }
                }
            }
        }
    }
    
    @Composable
    private fun MessageBubble(message: ConversationMessage) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
        ) {
            Card(
                modifier = Modifier.widthIn(max = 280.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (message.isUser) 
                        MaterialTheme.colorScheme.primary
                    else 
                        MaterialTheme.colorScheme.secondary
                ),
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (message.isUser) 16.dp else 4.dp,
                    bottomEnd = if (message.isUser) 4.dp else 16.dp
                )
            ) {
                Text(
                    text = message.text,
                    modifier = Modifier.padding(12.dp),
                    color = if (message.isUser) 
                        MaterialTheme.colorScheme.onPrimary
                    else 
                        MaterialTheme.colorScheme.onSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
    
    @Composable
    private fun VoiceControlsRow(
        textInput: String,
        onTextInputChange: (String) -> Unit,
        isListening: Boolean,
        onStartListening: () -> Unit,
        onStopListening: () -> Unit,
        onSendMessage: (String) -> Unit
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Text input
            OutlinedTextField(
                value = textInput,
                onValueChange = onTextInputChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Ask your AI advisory team...") },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null
                    )
                }
            )
            
            // Voice button
            FloatingActionButton(
                onClick = {
                    if (isListening) onStopListening() else onStartListening()
                },
                modifier = Modifier.size(48.dp),
                containerColor = if (isListening) 
                    MaterialTheme.colorScheme.error
                else 
                    MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = if (isListening) "Stop listening" else "Start listening"
                )
            }
            
            // Send button
            FloatingActionButton(
                onClick = { onSendMessage(textInput) },
                modifier = Modifier.size(48.dp),
                containerColor = MaterialTheme.colorScheme.secondary
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send message"
                )
            }
        }
    }
    
    @Composable
    private fun StatusBar() {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatusItem("🔒 Encrypted", MaterialTheme.colorScheme.primary)
                StatusItem("📊 Sensors Active", MaterialTheme.colorScheme.secondary)
                StatusItem("🌐 P2P Ready", MaterialTheme.colorScheme.tertiary)
            }
        }
    }
    
    @Composable
    private fun StatusItem(status: String, color: Color) {
        Text(
            text = status,
            style = MaterialTheme.typography.bodySmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
    
    @Composable
    private fun EnhancedAgentCard(
        agent: Agent,
        onToggle: (Boolean) -> Unit
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (agent.isActive) 
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                else 
                    MaterialTheme.colorScheme.surface
            ),
            border = if (agent.isActive) 
                androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
            else null
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Agent Avatar
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(getAgentColor(agent.name)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = getAgentEmoji(agent.name),
                        fontSize = 24.sp
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = agent.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = agent.type,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "Voice: ${agent.voiceProfile.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Switch(
                    checked = agent.isActive,
                    onCheckedChange = onToggle
                )
            }
        }
    }
    
    private fun getAgentColor(agentName: String): Color {
        return when (agentName.lowercase()) {
            "dr. sarah" -> Color(0xFF4CAF50)
            "coach mike" -> Color(0xFFFF9800)
            "advisor alex" -> Color(0xFF2196F3)
            "assistant pat" -> Color(0xFF9C27B0)
            "zen maya" -> Color(0xFF00BCD4)
            else -> Color(0xFF607D8B)
        }
    }
    
    private fun getAgentEmoji(agentName: String): String {
        return when (agentName.lowercase()) {
            "dr. sarah" -> "👩‍⚕️"
            "coach mike" -> "💪"
            "advisor alex" -> "💰"
            "assistant pat" -> "📋"
            "zen maya" -> "🧘‍♀️"
            else -> "🤖"
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

/**
 * Data class representing a conversation message
 */
data class ConversationMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)