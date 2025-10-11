package com.agentzero.voice

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer
import android.speech.RecognizerIntent
import android.content.Intent
import com.agentzero.core.Agent
import com.agentzero.core.VoiceProfile
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import timber.log.Timber
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * Voice Manager for Agent Zero
 * 
 * Handles:
 * - Speech recognition (voice input)
 * - Text-to-speech synthesis with different agent voices
 * - Voice model management
 * - Audio processing and playback
 */
class VoiceManager(
    private val context: Context
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    // Voice synthesis engines
    private val ttsEngines = ConcurrentHashMap<String, TTSEngine>()
    private val voiceModels = ConcurrentHashMap<String, VoiceModel>()
    
    // Speech recognition
    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false
    
    // Audio management
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var currentAudioTrack: AudioTrack? = null
    
    // Voice synthesis queue
    private val synthesisQueue = MutableSharedFlow<VoiceSynthesisRequest>()
    
    fun initialize() {
        Timber.d("Initializing Voice Manager")
        
        // Initialize TTS engines
        initializeTTSEngines()
        
        // Load voice models
        loadVoiceModels()
        
        // Start synthesis processing
        startSynthesisProcessing()
        
        Timber.d("Voice Manager initialized with ${ttsEngines.size} TTS engines and ${voiceModels.size} voice models")
    }
    
    private fun initializeTTSEngines() {
        // Initialize Sherpa-ONNX TTS engine (primary)
        val sherpaEngine = SherpaOnnxTTSEngine(context)
        ttsEngines["sherpa-onnx"] = sherpaEngine
        
        // Initialize Android TTS as fallback
        val androidEngine = AndroidTTSEngine(context)
        ttsEngines["android"] = androidEngine
        
        // Initialize simple neural TTS for basic functionality
        val simpleTTS = SimpleTTSEngine(context)
        ttsEngines["simple"] = simpleTTS
    }
    
    private fun loadVoiceModels() {
        // Define voice models for different agent personalities
        voiceModels["sherpa-onnx-female-calm"] = VoiceModel(
            id = "sherpa-onnx-female-calm",
            name = "Dr. Sarah",
            gender = "female",
            language = "en-US",
            style = "calm",
            modelPath = "models/sherpa-onnx/female-calm.onnx",
            engine = "sherpa-onnx"
        )
        
        voiceModels["sherpa-onnx-male-energetic"] = VoiceModel(
            id = "sherpa-onnx-male-energetic", 
            name = "Coach Mike",
            gender = "male",
            language = "en-US",
            style = "energetic",
            modelPath = "models/sherpa-onnx/male-energetic.onnx",
            engine = "sherpa-onnx"
        )
        
        voiceModels["sherpa-onnx-neutral-professional"] = VoiceModel(
            id = "sherpa-onnx-neutral-professional",
            name = "Advisor Alex", 
            gender = "neutral",
            language = "en-US",
            style = "professional",
            modelPath = "models/sherpa-onnx/neutral-professional.onnx",
            engine = "sherpa-onnx"
        )
        
        voiceModels["sherpa-onnx-neutral-efficient"] = VoiceModel(
            id = "sherpa-onnx-neutral-efficient",
            name = "Assistant Pat",
            gender = "neutral", 
            language = "en-US",
            style = "efficient",
            modelPath = "models/sherpa-onnx/neutral-efficient.onnx",
            engine = "sherpa-onnx"
        )
        
        voiceModels["sherpa-onnx-female-soothing"] = VoiceModel(
            id = "sherpa-onnx-female-soothing",
            name = "Zen Maya",
            gender = "female",
            language = "en-US", 
            style = "soothing",
            modelPath = "models/sherpa-onnx/female-soothing.onnx",
            engine = "sherpa-onnx"
        )
        
        // Fallback Android TTS voices
        voiceModels["android-default"] = VoiceModel(
            id = "android-default",
            name = "Default",
            gender = "neutral",
            language = "en-US",
            style = "default",
            modelPath = "",
            engine = "android"
        )
    }
    
    private fun startSynthesisProcessing() {
        scope.launch {
            synthesisQueue.collect { request ->
                try {
                    processSynthesisRequest(request)
                } catch (e: Exception) {
                    Timber.e(e, "Error processing synthesis request")
                    request.onError?.invoke(e)
                }
            }
        }
    }
    
    suspend fun speakAsAgent(agent: Agent, text: String) {
        val voiceProfile = agent.voiceProfile
        val voiceModel = voiceModels[voiceProfile.model] ?: voiceModels["android-default"]!!
        
        val request = VoiceSynthesisRequest(
            text = text,
            voiceModel = voiceModel,
            voiceProfile = voiceProfile,
            onSuccess = { audioData ->
                playAudio(audioData)
            },
            onError = { error ->
                Timber.e(error, "Failed to synthesize speech for agent ${agent.name}")
                // Fallback to Android TTS
                fallbackToAndroidTTS(text, voiceProfile)
            }
        )
        
        synthesisQueue.emit(request)
    }
    
    private suspend fun processSynthesisRequest(request: VoiceSynthesisRequest) {
        val engine = ttsEngines[request.voiceModel.engine]
        if (engine == null) {
            request.onError?.invoke(Exception("TTS engine not found: ${request.voiceModel.engine}"))
            return
        }
        
        val audioData = engine.synthesize(
            text = request.text,
            voiceModel = request.voiceModel,
            voiceProfile = request.voiceProfile
        )
        
        if (audioData != null) {
            request.onSuccess?.invoke(audioData)
        } else {
            request.onError?.invoke(Exception("Failed to synthesize audio"))
        }
    }
    
    private fun playAudio(audioData: ByteArray) {
        scope.launch(Dispatchers.IO) {
            try {
                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(22050)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(audioData.size)
                    .build()
                
                currentAudioTrack?.stop()
                currentAudioTrack?.release()
                currentAudioTrack = audioTrack
                
                audioTrack.play()
                audioTrack.write(audioData, 0, audioData.size)
                audioTrack.stop()
                audioTrack.release()
                
                currentAudioTrack = null
            } catch (e: Exception) {
                Timber.e(e, "Error playing audio")
            }
        }
    }
    
    private fun fallbackToAndroidTTS(text: String, voiceProfile: VoiceProfile) {
        val androidEngine = ttsEngines["android"] as? AndroidTTSEngine
        androidEngine?.speak(text, voiceProfile)
    }
    
    suspend fun startListening(onResult: (String) -> Unit) {
        if (isListening) {
            Timber.w("Already listening")
            return
        }
        
        try {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            
            val recognitionListener = object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    Timber.d("Ready for speech")
                }
                
                override fun onBeginningOfSpeech() {
                    Timber.d("Beginning of speech")
                }
                
                override fun onRmsChanged(rmsdB: Float) {
                    // Audio level changed
                }
                
                override fun onBufferReceived(buffer: ByteArray?) {
                    // Audio buffer received
                }
                
                override fun onEndOfSpeech() {
                    Timber.d("End of speech")
                }
                
                override fun onError(error: Int) {
                    Timber.e("Speech recognition error: $error")
                    isListening = false
                }
                
                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        val transcript = matches[0]
                        Timber.d("Speech recognition result: $transcript")
                        onResult(transcript)
                    }
                    isListening = false
                }
                
                override fun onPartialResults(partialResults: Bundle?) {
                    // Partial results available
                }
                
                override fun onEvent(eventType: Int, params: Bundle?) {
                    // Speech recognition event
                }
            }
            
            speechRecognizer?.setRecognitionListener(recognitionListener)
            
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }
            
            speechRecognizer?.startListening(intent)
            isListening = true
            
        } catch (e: Exception) {
            Timber.e(e, "Error starting speech recognition")
            isListening = false
        }
    }
    
    suspend fun stopListening() {
        speechRecognizer?.stopListening()
        speechRecognizer?.destroy()
        speechRecognizer = null
        isListening = false
        Timber.d("Stopped listening")
    }
    
    fun getAvailableVoiceModels(): List<VoiceModel> {
        return voiceModels.values.toList()
    }
    
    fun getVoiceModel(modelId: String): VoiceModel? {
        return voiceModels[modelId]
    }
    
    fun isCurrentlyListening(): Boolean = isListening
    
    fun stopCurrentSpeech() {
        currentAudioTrack?.stop()
        currentAudioTrack?.release()
        currentAudioTrack = null
    }
    
    fun shutdown() {
        Timber.d("Shutting down Voice Manager")
        
        // Stop any current speech
        stopCurrentSpeech()
        
        // Stop listening
        scope.launch {
            stopListening()
        }
        
        // Shutdown TTS engines
        ttsEngines.values.forEach { it.shutdown() }
        ttsEngines.clear()
        
        // Clear voice models
        voiceModels.clear()
        
        // Cancel scope
        scope.cancel()
        
        Timber.d("Voice Manager shutdown complete")
    }
}

/**
 * Voice synthesis request
 */
data class VoiceSynthesisRequest(
    val text: String,
    val voiceModel: VoiceModel,
    val voiceProfile: VoiceProfile,
    val onSuccess: ((ByteArray) -> Unit)? = null,
    val onError: ((Exception) -> Unit)? = null
)

/**
 * Voice model definition
 */
data class VoiceModel(
    val id: String,
    val name: String,
    val gender: String,
    val language: String,
    val style: String,
    val modelPath: String,
    val engine: String
)

/**
 * Base interface for TTS engines
 */
interface TTSEngine {
    suspend fun synthesize(
        text: String,
        voiceModel: VoiceModel,
        voiceProfile: VoiceProfile
    ): ByteArray?
    
    fun shutdown()
}

/**
 * Sherpa-ONNX TTS Engine (primary engine for high-quality voices)
 */
class SherpaOnnxTTSEngine(
    private val context: Context
) : TTSEngine {
    
    // Note: This is a placeholder implementation
    // In a real implementation, you would integrate the actual Sherpa-ONNX library
    
    override suspend fun synthesize(
        text: String,
        voiceModel: VoiceModel,
        voiceProfile: VoiceProfile
    ): ByteArray? {
        return withContext(Dispatchers.IO) {
            try {
                // Placeholder: In real implementation, this would:
                // 1. Load the ONNX model from voiceModel.modelPath
                // 2. Process text through the neural TTS model
                // 3. Apply voice profile settings (speed, pitch)
                // 4. Return PCM audio data
                
                Timber.d("Synthesizing with Sherpa-ONNX: $text (model: ${voiceModel.id})")
                
                // For now, return null to fallback to Android TTS
                null
            } catch (e: Exception) {
                Timber.e(e, "Error in Sherpa-ONNX synthesis")
                null
            }
        }
    }
    
    override fun shutdown() {
        // Cleanup ONNX models and resources
    }
}

/**
 * Android TTS Engine (fallback)
 */
class AndroidTTSEngine(
    private val context: Context
) : TTSEngine {
    
    private var tts: android.speech.tts.TextToSpeech? = null
    private var isInitialized = false
    
    init {
        tts = android.speech.tts.TextToSpeech(context) { status ->
            if (status == android.speech.tts.TextToSpeech.SUCCESS) {
                isInitialized = true
                tts?.language = java.util.Locale.US
            }
        }
    }
    
    override suspend fun synthesize(
        text: String,
        voiceModel: VoiceModel,
        voiceProfile: VoiceProfile
    ): ByteArray? {
        // Android TTS doesn't return audio data directly
        // This method is used for the speak() function instead
        return null
    }
    
    fun speak(text: String, voiceProfile: VoiceProfile) {
        if (!isInitialized) {
            Timber.w("Android TTS not initialized")
            return
        }
        
        tts?.setSpeechRate(voiceProfile.speed)
        tts?.setPitch(voiceProfile.pitch)
        tts?.speak(text, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, null)
    }
    
    override fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}

/**
 * Simple TTS Engine (basic implementation)
 */
class SimpleTTSEngine(
    private val context: Context
) : TTSEngine {
    
    override suspend fun synthesize(
        text: String,
        voiceModel: VoiceModel,
        voiceProfile: VoiceProfile
    ): ByteArray? {
        return withContext(Dispatchers.IO) {
            try {
                // Simple synthesis: generate basic audio waveform
                // This is a very basic implementation for demonstration
                val sampleRate = 22050
                val duration = text.length * 0.1 // Rough estimate
                val samples = (sampleRate * duration).toInt()
                val audioData = ByteArray(samples * 2) // 16-bit audio
                
                // Generate simple tone-based speech (placeholder)
                for (i in 0 until samples) {
                    val sample = (Math.sin(2.0 * Math.PI * 440.0 * i / sampleRate) * 16383).toInt()
                    audioData[i * 2] = (sample and 0xFF).toByte()
                    audioData[i * 2 + 1] = ((sample shr 8) and 0xFF).toByte()
                }
                
                audioData
            } catch (e: Exception) {
                Timber.e(e, "Error in simple TTS synthesis")
                null
            }
        }
    }
    
    override fun shutdown() {
        // No resources to cleanup
    }
}