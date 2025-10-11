package com.agentzero.core

import kotlinx.serialization.Serializable
import timber.log.Timber

/**
 * Base class for all agent tools
 * 
 * Tools provide specific capabilities to agents (sensor access, web search, etc.)
 * Each tool is a discrete function that agents can call to perform actions.
 */
abstract class AgentTool(
    val name: String,
    val description: String,
    val requiredPermissions: List<String> = emptyList()
) {
    
    /**
     * Execute the tool with given parameters
     */
    abstract suspend fun execute(parameters: Map<String, Any>): ToolResult
    
    /**
     * Validate parameters before execution
     */
    open fun validateParameters(parameters: Map<String, Any>): ValidationResult {
        return ValidationResult.success()
    }
    
    /**
     * Get tool schema for AI agents to understand how to use this tool
     */
    abstract fun getSchema(): ToolSchema
    
    /**
     * Check if tool has required permissions
     */
    open fun hasRequiredPermissions(): Boolean {
        // Default implementation - subclasses should override for permission checking
        return true
    }
    
    override fun toString(): String {
        return "AgentTool(name='$name', description='$description')"
    }
}

/**
 * Result of tool execution
 */
@Serializable
sealed class ToolResult {
    @Serializable
    data class Success(val data: Map<String, Any>) : ToolResult()
    
    @Serializable
    data class Error(val message: String, val code: String = "UNKNOWN") : ToolResult()
    
    @Serializable
    data class PermissionDenied(val permission: String, val message: String) : ToolResult()
    
    companion object {
        fun success(data: Map<String, Any> = emptyMap()) = Success(data)
        fun success(key: String, value: Any) = Success(mapOf(key to value))
        fun error(message: String, code: String = "UNKNOWN") = Error(message, code)
        fun permissionDenied(permission: String, message: String) = PermissionDenied(permission, message)
    }
    
    fun isSuccess(): Boolean = this is Success
    fun isError(): Boolean = this is Error
    fun isPermissionDenied(): Boolean = this is PermissionDenied
    
    fun getDataOrNull(): Map<String, Any>? = (this as? Success)?.data
    fun getErrorOrNull(): String? = (this as? Error)?.message
}

/**
 * Parameter validation result
 */
sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()
    
    companion object {
        fun success() = Success
        fun error(message: String) = Error(message)
    }
    
    fun isSuccess(): Boolean = this is Success
    fun getErrorOrNull(): String? = (this as? Error)?.message
}

/**
 * Tool schema for AI agents to understand tool usage
 */
@Serializable
data class ToolSchema(
    val name: String,
    val description: String,
    val parameters: List<ParameterSchema>,
    val returnType: String,
    val examples: List<ToolExample> = emptyList()
)

/**
 * Parameter schema for tools
 */
@Serializable
data class ParameterSchema(
    val name: String,
    val type: String, // "string", "number", "boolean", "array", "object"
    val description: String,
    val required: Boolean = false,
    val defaultValue: String? = null,
    val allowedValues: List<String>? = null
)

/**
 * Example usage of a tool
 */
@Serializable
data class ToolExample(
    val description: String,
    val parameters: Map<String, Any>,
    val expectedResult: String
)

// Core tool implementations

/**
 * Tool for querying sensor data
 */
class SensorQueryTool(
    private val sensorManager: Any // TODO: Replace with actual SensorToolManager
) : AgentTool(
    name = "sensor_query",
    description = "Query current and historical sensor data",
    requiredPermissions = listOf("android.permission.BODY_SENSORS")
) {
    
    override suspend fun execute(parameters: Map<String, Any>): ToolResult {
        return try {
            val sensorType = parameters["sensor_type"] as? String
                ?: return ToolResult.error("Missing sensor_type parameter")
            
            val timeRange = parameters["time_range"] as? String ?: "current"
            
            // TODO: Implement actual sensor query
            val mockData = mapOf(
                "sensor_type" to sensorType,
                "value" to 72.0,
                "timestamp" to System.currentTimeMillis(),
                "unit" to "bpm"
            )
            
            ToolResult.success(mockData)
        } catch (e: Exception) {
            Timber.e(e, "Error querying sensor data")
            ToolResult.error("Failed to query sensor data: ${e.message}")
        }
    }
    
    override fun getSchema(): ToolSchema {
        return ToolSchema(
            name = name,
            description = description,
            parameters = listOf(
                ParameterSchema(
                    name = "sensor_type",
                    type = "string",
                    description = "Type of sensor to query",
                    required = true,
                    allowedValues = listOf("heart_rate", "steps", "accelerometer", "gyroscope")
                ),
                ParameterSchema(
                    name = "time_range",
                    type = "string",
                    description = "Time range for data",
                    required = false,
                    defaultValue = "current",
                    allowedValues = listOf("current", "hour", "day", "week")
                )
            ),
            returnType = "object",
            examples = listOf(
                ToolExample(
                    description = "Get current heart rate",
                    parameters = mapOf("sensor_type" to "heart_rate"),
                    expectedResult = "Current heart rate reading with timestamp"
                )
            )
        )
    }
    
    suspend fun getRelevantData(domains: List<String>): Map<String, Any> {
        // TODO: Implement domain-specific sensor data retrieval
        return emptyMap()
    }
}

/**
 * Tool for querying knowledge store
 */
class KnowledgeQueryTool(
    private val knowledgeStore: Any // TODO: Replace with actual KnowledgeStore
) : AgentTool(
    name = "knowledge_query",
    description = "Query the shared knowledge store for information"
) {
    
    override suspend fun execute(parameters: Map<String, Any>): ToolResult {
        return try {
            val query = parameters["query"] as? String
                ?: return ToolResult.error("Missing query parameter")
            
            val domains = parameters["domains"] as? List<*> ?: emptyList<String>()
            
            // TODO: Implement actual knowledge store query
            val mockResults = listOf(
                "Knowledge item 1 related to: $query",
                "Knowledge item 2 related to: $query"
            )
            
            ToolResult.success("results" to mockResults)
        } catch (e: Exception) {
            Timber.e(e, "Error querying knowledge store")
            ToolResult.error("Failed to query knowledge store: ${e.message}")
        }
    }
    
    override fun getSchema(): ToolSchema {
        return ToolSchema(
            name = name,
            description = description,
            parameters = listOf(
                ParameterSchema(
                    name = "query",
                    type = "string",
                    description = "Search query for knowledge store",
                    required = true
                ),
                ParameterSchema(
                    name = "domains",
                    type = "array",
                    description = "Knowledge domains to search in",
                    required = false
                )
            ),
            returnType = "array"
        )
    }
    
    suspend fun query(query: String, domains: List<String>): List<String> {
        // TODO: Implement actual knowledge store query
        return emptyList()
    }
}

/**
 * Tool for agent coordination
 */
class AgentCoordinationTool(
    private val framework: AgentFramework
) : AgentTool(
    name = "agent_coordinate",
    description = "Coordinate with other agents"
) {
    
    override suspend fun execute(parameters: Map<String, Any>): ToolResult {
        return try {
            val action = parameters["action"] as? String
                ?: return ToolResult.error("Missing action parameter")
            
            when (action) {
                "get_active_agents" -> {
                    val agents = framework.getActiveAgents().map { 
                        mapOf("id" to it.id, "name" to it.name, "type" to it.type)
                    }
                    ToolResult.success("agents" to agents)
                }
                "get_agent_response" -> {
                    val agentId = parameters["agent_id"] as? String
                        ?: return ToolResult.error("Missing agent_id parameter")
                    
                    val query = parameters["query"] as? String
                        ?: return ToolResult.error("Missing query parameter")
                    
                    // TODO: Implement agent-to-agent communication
                    ToolResult.success("response" to "Mock response from agent $agentId")
                }
                else -> ToolResult.error("Unknown coordination action: $action")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error in agent coordination")
            ToolResult.error("Agent coordination failed: ${e.message}")
        }
    }
    
    override fun getSchema(): ToolSchema {
        return ToolSchema(
            name = name,
            description = description,
            parameters = listOf(
                ParameterSchema(
                    name = "action",
                    type = "string",
                    description = "Coordination action to perform",
                    required = true,
                    allowedValues = listOf("get_active_agents", "get_agent_response")
                ),
                ParameterSchema(
                    name = "agent_id",
                    type = "string",
                    description = "Target agent ID (for some actions)",
                    required = false
                ),
                ParameterSchema(
                    name = "query",
                    type = "string",
                    description = "Query to send to other agent",
                    required = false
                )
            ),
            returnType = "object"
        )
    }
}