package com.agentzero.core

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for ToolResult
 */
class ToolResultTest {

    @Test
    fun `test successful tool result`() {
        val data = mapOf("temperature" to 23.5, "humidity" to 65)
        val result = ToolResult.success(data)

        assertTrue("Result should be successful", result.isSuccess())
        assertFalse("Result should not be error", result.isError())
        assertEquals(data, result.getDataOrNull())
        assertNull("Error message should be null for success", result.getErrorOrNull())
    }

    @Test
    fun `test error tool result`() {
        val errorMessage = "Sensor not available"
        val result = ToolResult.error(errorMessage)

        assertFalse("Result should not be successful", result.isSuccess())
        assertTrue("Result should be error", result.isError())
        assertNull("Data should be null for error", result.getDataOrNull())
        assertEquals(errorMessage, result.getErrorOrNull())
    }

    @Test
    fun `test tool result with empty data`() {
        val result = ToolResult.success(emptyMap<String, Any>())

        assertTrue("Result should be successful", result.isSuccess())
        assertNotNull("Data should not be null", result.getDataOrNull())
        assertTrue("Data should be empty", result.getDataOrNull()!!.isEmpty())
    }

    @Test
    fun `test tool result with null data handling`() {
        val result = ToolResult.success(null)

        assertTrue("Result should be successful", result.isSuccess())
        assertNull("Data should be null", result.getDataOrNull())
    }

    @Test
    fun `test tool result data extraction`() {
        val data = mapOf(
            "heart_rate" to 72,
            "timestamp" to System.currentTimeMillis(),
            "accuracy" to "high"
        )
        val result = ToolResult.success(data)

        assertEquals(72, result.getDataOrNull()?.get("heart_rate"))
        assertEquals("high", result.getDataOrNull()?.get("accuracy"))
        assertTrue("Timestamp should be a number", result.getDataOrNull()?.get("timestamp") is Long)
    }

    @Test
    fun `test tool result error handling`() {
        val errorResult = ToolResult.error("Network timeout")
        val successResult = ToolResult.success(mapOf("status" to "ok"))

        // Error result should not provide data
        assertNull("Error result should not have data", errorResult.getDataOrNull())
        assertEquals("Network timeout", errorResult.getErrorOrNull())

        // Success result should not provide error
        assertNull("Success result should not have error", successResult.getErrorOrNull())
        assertNotNull("Success result should have data", successResult.getDataOrNull())
    }

    @Test
    fun `test tool result chaining`() {
        val result1 = ToolResult.success(mapOf("value" to 42))
        val result2 = ToolResult.error("Processing failed")

        // Simulate chaining operations
        val chainedResult = if (result1.isSuccess()) {
            val data = result1.getDataOrNull()
            if (data != null && data["value"] as Int > 40) {
                ToolResult.success(mapOf("processed_value" to (data["value"] as Int) * 2))
            } else {
                ToolResult.error("Value too small")
            }
        } else {
            result1
        }

        assertTrue("Chained result should be successful", chainedResult.isSuccess())
        assertEquals(84, chainedResult.getDataOrNull()?.get("processed_value"))
    }
}