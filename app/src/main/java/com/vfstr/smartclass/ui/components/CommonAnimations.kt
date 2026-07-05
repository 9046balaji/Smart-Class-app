package com.vfstr.smartclass.ui.components

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.vfstr.smartclass.ui.theme.DesignSystem
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

/**
 * Enhanced Particle Background with drifting stars/particles with depth.
 * Rule 16 compliant.
 */
@Composable
fun ParticleBackground(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "t"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val random = Random(42)

        for (i in 0..100) {
            val startX = random.nextFloat() * width
            val startY = random.nextFloat() * height
            
            // Depth levels
            val depth = (i % 3) + 1
            val radius = random.nextFloat() * (2f * depth) + 0.5f
            val speedFactor = random.nextFloat() * (20f * depth) + 5f
            val alphaVal = (0.1f * depth) + (random.nextFloat() * 0.2f)
            
            val offsetVal = sin(time + i) * speedFactor
            drawCircle(
                color = DesignSystem.Cyan.copy(alpha = alphaVal),
                radius = radius,
                center = Offset(startX, (startY + offsetVal) % height)
            )
        }
    }
}

/**
 * Animated number counter that counts up from 0 to the target value.
 * Used for Metric Cards as per Rule 16.
 */
@Composable
fun AnimateNumberCounter(
    targetValue: Int,
    style: TextStyle,
    modifier: Modifier = Modifier,
    prefix: String = "",
    suffix: String = ""
) {
    var animationTriggered by remember { mutableStateOf(false) }
    val animatedValue by animateIntAsState(
        targetValue = if (animationTriggered) targetValue else 0,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "number_counter"
    )

    LaunchedEffect(Unit) {
        animationTriggered = true
    }

    Text(
        text = "$prefix$animatedValue$suffix",
        style = style,
        modifier = modifier
    )
}

/**
 * Hook to get gyroscope tilt values for 3D parallax effects.
 * Rule 16: Gyroscope 3D tilt via SensorManager.
 */
@Composable
fun rememberTiltState(): Pair<Float, Float> {
    val context = LocalContext.current
    var tilt by remember { mutableStateOf(0f to 0f) }

    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        
        if (sensor == null) return@DisposableEffect onDispose {}

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event != null && event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                    val rotationMatrix = FloatArray(9)
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                    val orientation = FloatArray(3)
                    SensorManager.getOrientation(rotationMatrix, orientation)
                    
                    // Pitch (rotation around X) and Roll (rotation around Y)
                    val pitch = orientation[1] // radians
                    val roll = orientation[2] // radians
                    tilt = pitch to roll
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }
    return tilt
}

/**
 * Custom modifier for 3D gyroscope tilt effect.
 * Rule 16: 3D tilt effect on metric cards.
 */
fun Modifier.tiltEffect(
    pitch: Float,
    roll: Float
): Modifier = this.graphicsLayer {
    // Limit to ~5-7 degrees max for premium subtlety
    rotationX = pitch.coerceIn(-0.15f, 0.15f) * -30f 
    rotationY = roll.coerceIn(-0.15f, 0.15f) * 30f
    cameraDistance = 16f * density
}

/**
 * Staggered entry effect with spring physics.
 * Rule 3: spring() physics for natural motion.
 */
@Composable
fun StaggeredSpringEntry(
    index: Int,
    delayMillis: Long = 100L,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(index * delayMillis)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { 100 },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeIn(animationSpec = tween(600)),
        exit = fadeOut()
    ) {
        content()
    }
}

/**
 * Extension for pulsing glow effect.
 */
@Composable
fun Modifier.pulsingGlow(color: Color): Modifier {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    return this.alpha(alpha).shadow(elevation = 8.dp, shape = CircleShape, spotColor = color)
}
