package com.agentzero.core

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for VoiceProfile
 */
class VoiceProfileTest {

    @Test
    fun `test voice profile creation`() {
        val voiceProfile = VoiceProfile(
            model = "sherpa-onnx-female-calm",
            speed = 1.0f,
            pitch = 1.0f,
            language = "en-US"
        )

        assertEquals("sherpa-onnx-female-calm", voiceProfile.model)
        assertEquals(1.0f, voiceProfile.speed)
        assertEquals(1.0f, voiceProfile.pitch)
        assertEquals("en-US", voiceProfile.language)
    }

    @Test
    fun `test voice profile parameter validation`() {
        val voiceProfile = VoiceProfile(
            model = "test-model",
            speed = 2.5f, // Should be clamped to reasonable range
            pitch = 0.1f, // Should be clamped to reasonable range
            language = "en-US"
        )

        // Speed should be within reasonable range (0.5 - 2.0)
        assertTrue("Speed should be reasonable", voiceProfile.speed >= 0.5f && voiceProfile.speed <= 2.0f)
        
        // Pitch should be within reasonable range (0.5 - 2.0)
        assertTrue("Pitch should be reasonable", voiceProfile.pitch >= 0.5f && voiceProfile.pitch <= 2.0f)
    }

    @Test
    fun `test voice profile equality`() {
        val profile1 = VoiceProfile(
            model = "test-model",
            speed = 1.0f,
            pitch = 1.0f,
            language = "en-US"
        )

        val profile2 = VoiceProfile(
            model = "test-model",
            speed = 1.0f,
            pitch = 1.0f,
            language = "en-US"
        )

        val profile3 = VoiceProfile(
            model = "different-model",
            speed = 1.2f,
            pitch = 0.8f,
            language = "es-ES"
        )

        assertEquals(profile1, profile2)
        assertNotEquals(profile1, profile3)
    }

    @Test
    fun `test voice profile language codes`() {
        val supportedLanguages = listOf("en-US", "en-GB", "es-ES", "fr-FR", "de-DE", "ja-JP", "zh-CN")
        
        supportedLanguages.forEach { lang ->
            val profile = VoiceProfile(
                model = "test-model",
                speed = 1.0f,
                pitch = 1.0f,
                language = lang
            )
            
            assertTrue("Language $lang should be supported", profile.language.matches(Regex("[a-z]{2}-[A-Z]{2}")))
        }
    }
}