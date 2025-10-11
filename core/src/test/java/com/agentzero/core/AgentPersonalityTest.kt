package com.agentzero.core

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for AgentPersonality
 */
class AgentPersonalityTest {

    @Test
    fun `test agent personality creation`() {
        val personality = AgentPersonality(
            traits = mapOf(
                "empathy" to 0.9f,
                "professionalism" to 0.8f
            ),
            communicationStyle = "caring_professional",
            responseLength = "medium",
            expertise = listOf("health", "wellness"),
            limitations = listOf("Not medical advice")
        )

        assertEquals(0.9f, personality.traits["empathy"])
        assertEquals(0.8f, personality.traits["professionalism"])
        assertEquals("caring_professional", personality.communicationStyle)
        assertEquals("medium", personality.responseLength)
        assertTrue(personality.expertise.contains("health"))
        assertTrue(personality.limitations.contains("Not medical advice"))
    }

    @Test
    fun `test personality trait validation`() {
        val personality = AgentPersonality(
            traits = mapOf(
                "empathy" to 1.5f, // Should be clamped to 1.0
                "analytical" to -0.5f // Should be clamped to 0.0
            ),
            communicationStyle = "professional",
            responseLength = "short",
            expertise = emptyList(),
            limitations = emptyList()
        )

        // Traits should be within valid range [0.0, 1.0]
        assertTrue("Empathy should be <= 1.0", personality.traits["empathy"]!! <= 1.0f)
        assertTrue("Analytical should be >= 0.0", personality.traits["analytical"]!! >= 0.0f)
    }

    @Test
    fun `test personality comparison`() {
        val personality1 = AgentPersonality(
            traits = mapOf("empathy" to 0.9f),
            communicationStyle = "caring",
            responseLength = "medium",
            expertise = listOf("health"),
            limitations = emptyList()
        )

        val personality2 = AgentPersonality(
            traits = mapOf("empathy" to 0.9f),
            communicationStyle = "caring",
            responseLength = "medium",
            expertise = listOf("health"),
            limitations = emptyList()
        )

        val personality3 = AgentPersonality(
            traits = mapOf("empathy" to 0.5f),
            communicationStyle = "professional",
            responseLength = "short",
            expertise = listOf("finance"),
            limitations = emptyList()
        )

        assertEquals(personality1, personality2)
        assertNotEquals(personality1, personality3)
    }
}