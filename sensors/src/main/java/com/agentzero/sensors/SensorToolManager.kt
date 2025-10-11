package com.agentzero.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.agentzero.core.AgentTool
import com.agentzero.core.ToolResult
import com.agentzero.security.SecurityManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.Serializable
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap

/**
 * Manages all sensor-related tools for Agent Zero
 * 
 * Provides secure, privacy-preserving access to device sensors
 * with user consent and data minimization principles.
 */
class SensorToolManager(
    private val context: Context,
    private val securityManager: SecurityManager
) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    // Active sensor collectors
    private val collectors = ConcurrentHashMap<String, SensorCollector>()
    
    // Sensor data flows
    private val _sensorData = MutableSharedFlow<SensorReading>()
    val sensorData: SharedFlow<SensorReading> = _sensorData.asSharedFlow()
    
    // Available sensor tools
    private val sensorTools = mutableMapOf<String, AgentTool>()
    
    fun initialize() {
        Timber.d("Initializing Sensor Tool Manager")
        
        // Register available sensor tools
        registerSensorTools()
        
        // Start background sensor monitoring (with user consent)
        startBackgroundMonitoring()
        
        Timber.d("Sensor Tool Manager initialized with ${sensorTools.size} tools")
    }
    
    private fun registerSensorTools() {
        // Heart rate sensor tool
        if (hasHeartRateSensor()) {
            sensorTools["heart_rate"] = HeartRateTool(this)
        }
        
        // Step counter tool
        if (hasStepCounterSensor()) {
            sensorTools["step_counter"] = StepCounterTool(this)
        }
        
        // Accelerometer tool
        if (hasAccelerometer()) {
            sensorTools["accelerometer"] = AccelerometerTool(this)
        }
        
        // Gyroscope tool
        if (hasGyroscope()) {
            sensorTools["gyroscope"] = GyroscopeTool(this)
        }
        
        // Activity recognition tool
        sensorTools["activity_recognition"] = ActivityRecognitionTool(this)
        
        // Sleep analysis tool
        sensorTools["sleep_analysis"] = SleepAnalysisTool(this)
        
        // Stress detection tool
        sensorTools["stress_detection"] = StressDetectionTool(this)
    }
    
    private fun startBackgroundMonitoring() {
        // Only start if user has given consent
        if (!securityManager.hasUserConsent("sensor_monitoring")) {
            Timber.d("No user consent for sensor monitoring")
            return
        }
        
        scope.launch {
            // Start continuous monitoring of key health sensors
            startHeartRateMonitoring()
            startStepCountMonitoring()
            startActivityMonitoring()
        }
    }
    
    private suspend fun startHeartRateMonitoring() {
        if (!hasHeartRateSensor()) return
        
        val collector = HeartRateCollector(sensorManager) { reading ->
            scope.launch {
                _sensorData.emit(reading)
            }
        }
        
        collectors["heart_rate"] = collector
        collector.start()
    }
    
    private suspend fun startStepCountMonitoring() {
        if (!hasStepCounterSensor()) return
        
        val collector = StepCounterCollector(sensorManager) { reading ->
            scope.launch {
                _sensorData.emit(reading)
            }
        }
        
        collectors["step_counter"] = collector
        collector.start()
    }
    
    private suspend fun startActivityMonitoring() {
        val collector = ActivityCollector(sensorManager) { reading ->
            scope.launch {
                _sensorData.emit(reading)
            }
        }
        
        collectors["activity"] = collector
        collector.start()
    }
    
    fun getSensorTool(name: String): AgentTool? = sensorTools[name]
    
    fun getAllSensorTools(): List<AgentTool> = sensorTools.values.toList()
    
    suspend fun getRecentData(sensorType: String? = null, timeRangeMs: Long = 3600000): List<SensorReading> {
        // Get recent sensor data from secure storage
        return securityManager.getSecureSensorData(sensorType, timeRangeMs)
    }
    
    suspend fun getCurrentReading(sensorType: String): SensorReading? {
        val collector = collectors[sensorType] ?: return null
        return collector.getCurrentReading()
    }
    
    // Sensor availability checks
    private fun hasHeartRateSensor(): Boolean {
        return sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE) != null
    }
    
    private fun hasStepCounterSensor(): Boolean {
        return sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null
    }
    
    private fun hasAccelerometer(): Boolean {
        return sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null
    }
    
    private fun hasGyroscope(): Boolean {
        return sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null
    }
    
    fun getRecentData(): Map<String, String> {
        // Return recent sensor data as string map for agent context
        return collectors.mapValues { (_, collector) ->
            collector.getCurrentReading()?.toString() ?: "No data"
        }
    }
    
    fun shutdown() {
        Timber.d("Shutting down Sensor Tool Manager")
        
        // Stop all collectors
        collectors.values.forEach { it.stop() }
        collectors.clear()
        
        // Cancel scope
        scope.cancel()
        
        Timber.d("Sensor Tool Manager shutdown complete")
    }
}

/**
 * Base class for sensor data collectors
 */
abstract class SensorCollector(
    protected val sensorManager: SensorManager,
    private val onReading: (SensorReading) -> Unit
) : SensorEventListener {
    
    protected var isActive = false
    protected var lastReading: SensorReading? = null
    
    abstract val sensorType: Int
    abstract val sensorName: String
    
    open fun start() {
        val sensor = sensorManager.getDefaultSensor(sensorType)
        if (sensor != null) {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
            isActive = true
            Timber.d("Started $sensorName collector")
        } else {
            Timber.w("$sensorName sensor not available")
        }
    }
    
    open fun stop() {
        sensorManager.unregisterListener(this)
        isActive = false
        Timber.d("Stopped $sensorName collector")
    }
    
    fun getCurrentReading(): SensorReading? = lastReading
    
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Handle accuracy changes if needed
    }
    
    override fun onSensorChanged(event: SensorEvent?) {
        event?.let { 
            val reading = processSensorEvent(it)
            lastReading = reading
            onReading(reading)
        }
    }
    
    protected abstract fun processSensorEvent(event: SensorEvent): SensorReading
}

/**
 * Heart rate sensor collector
 */
class HeartRateCollector(
    sensorManager: SensorManager,
    onReading: (SensorReading) -> Unit
) : SensorCollector(sensorManager, onReading) {
    
    override val sensorType = Sensor.TYPE_HEART_RATE
    override val sensorName = "Heart Rate"
    
    override fun processSensorEvent(event: SensorEvent): SensorReading {
        return SensorReading(
            sensorType = "heart_rate",
            timestamp = System.currentTimeMillis(),
            values = mapOf(
                "bpm" to event.values[0].toDouble(),
                "accuracy" to event.accuracy.toDouble()
            ),
            metadata = mapOf(
                "sensor_name" to event.sensor.name,
                "vendor" to event.sensor.vendor
            )
        )
    }
}

/**
 * Step counter sensor collector
 */
class StepCounterCollector(
    sensorManager: SensorManager,
    onReading: (SensorReading) -> Unit
) : SensorCollector(sensorManager, onReading) {
    
    override val sensorType = Sensor.TYPE_STEP_COUNTER
    override val sensorName = "Step Counter"
    
    override fun processSensorEvent(event: SensorEvent): SensorReading {
        return SensorReading(
            sensorType = "step_counter",
            timestamp = System.currentTimeMillis(),
            values = mapOf(
                "steps" to event.values[0].toDouble()
            ),
            metadata = mapOf(
                "sensor_name" to event.sensor.name
            )
        )
    }
}

/**
 * Activity recognition collector (using accelerometer + gyroscope)
 */
class ActivityCollector(
    sensorManager: SensorManager,
    onReading: (SensorReading) -> Unit
) : SensorCollector(sensorManager, onReading) {
    
    override val sensorType = Sensor.TYPE_ACCELEROMETER
    override val sensorName = "Activity Recognition"
    
    private val activityClassifier = SimpleActivityClassifier()
    
    override fun processSensorEvent(event: SensorEvent): SensorReading {
        val activity = activityClassifier.classify(event.values)
        
        return SensorReading(
            sensorType = "activity",
            timestamp = System.currentTimeMillis(),
            values = mapOf(
                "activity" to activity.name,
                "confidence" to activity.confidence,
                "x" to event.values[0].toDouble(),
                "y" to event.values[1].toDouble(),
                "z" to event.values[2].toDouble()
            ),
            metadata = mapOf(
                "classifier" to "simple_threshold"
            )
        )
    }
}

/**
 * Simple activity classifier based on accelerometer data
 */
class SimpleActivityClassifier {
    
    fun classify(accelerometerValues: FloatArray): ActivityResult {
        val x = accelerometerValues[0]
        val y = accelerometerValues[1] 
        val z = accelerometerValues[2]
        
        val magnitude = kotlin.math.sqrt(x*x + y*y + z*z)
        
        return when {
            magnitude < 2.0 -> ActivityResult("stationary", 0.8)
            magnitude < 8.0 -> ActivityResult("walking", 0.7)
            magnitude < 15.0 -> ActivityResult("running", 0.6)
            else -> ActivityResult("vigorous_activity", 0.5)
        }
    }
}

/**
 * Activity classification result
 */
data class ActivityResult(
    val name: String,
    val confidence: Double
)

/**
 * Sensor reading data structure
 */
@Serializable
data class SensorReading(
    val sensorType: String,
    val timestamp: Long,
    val values: Map<String, Double>,
    val metadata: Map<String, String> = emptyMap()
)