package com.vfstr.smartclass.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

/**
 * Premium Animated Area Chart with gradient fill and stroke animations.
 * Rule 16 compliant.
 */
@Composable
fun AnimatedAreaChart(
    data: List<Float>,
    color: Color,
    modifier: Modifier = Modifier
) {
    var animationTriggered by remember { mutableStateOf(false) }
    val animatedProgress by animateFloatAsState(
        targetValue = if (animationTriggered) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessVeryLow),
        label = "area_chart"
    )

    LaunchedEffect(Unit) {
        animationTriggered = true
    }

    Canvas(modifier = modifier.fillMaxWidth().height(200.dp)) {
        if (data.isEmpty()) return@Canvas
        val spacing = size.width / (data.size - 1)
        val maxVal = data.maxOrNull() ?: 1f
        
        val path = Path().apply {
            data.forEachIndexed { index, value ->
                val x = index * spacing
                // Base at the bottom, animate height
                val y = size.height - (value / maxVal * size.height * animatedProgress)
                if (index == 0) moveTo(x, y) else lineTo(x, y)
            }
        }

        val fillPath = Path().apply {
            addPath(path)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(color.copy(alpha = 0.4f), Color.Transparent)
            )
        )
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw points with extra glow
        data.forEachIndexed { index, value ->
            val x = index * spacing
            val y = size.height - (value / maxVal * size.height * animatedProgress)
            drawCircle(color = color.copy(alpha = 0.2f), radius = 6.dp.toPx(), center = Offset(x, y))
            drawCircle(color = color, radius = 3.dp.toPx(), center = Offset(x, y))
        }
    }
}

/**
 * Premium Animated Donut Chart with staggered segment animation.
 */
@Composable
fun AnimatedDonutChart(
    data: List<Pair<String, Float>>,
    colors: List<Color>,
    modifier: Modifier = Modifier,
    thickness: Float = 44f
) {
    var animationTriggered by remember { mutableStateOf(false) }
    val animatedProgress by animateFloatAsState(
        targetValue = if (animationTriggered) 1f else 0f,
        animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
        label = "donut_chart"
    )

    LaunchedEffect(Unit) {
        animationTriggered = true
    }

    Canvas(modifier = modifier.size(200.dp)) {
        val total = data.sumOf { it.second.toDouble() }.toFloat()
        var startAngle = -90f
        
        data.forEachIndexed { index, pair ->
            val sweepAngle = (pair.second / total) * 360f * animatedProgress
            
            // Outer glow effect
            drawArc(
                color = colors[index % colors.size].copy(alpha = 0.1f),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = thickness + 8f, cap = StrokeCap.Round)
            )
            
            drawArc(
                color = colors[index % colors.size],
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = thickness, cap = StrokeCap.Round)
            )
            startAngle += (pair.second / total) * 360f
        }
    }
}

/**
 * Premium Animated Radar Chart.
 */
@Composable
fun AnimatedRadarChart(
    data: List<Float>,
    labels: List<String>,
    color: Color,
    modifier: Modifier = Modifier
) {
    var animationTriggered by remember { mutableStateOf(false) }
    val animatedProgress by animateFloatAsState(
        targetValue = if (animationTriggered) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessVeryLow),
        label = "radar_chart"
    )

    LaunchedEffect(Unit) {
        animationTriggered = true
    }

    Canvas(modifier = modifier.size(250.dp)) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.width / 2 * 0.8f
        val numAxes = data.size
        val angleStep = (2 * Math.PI / numAxes).toFloat()

        // Draw web/grid with glass feel
        for (i in 1..4) {
            val r = radius * (i / 4f)
            val gridPath = Path()
            for (j in 0 until numAxes) {
                val x = center.x + r * cos(j * angleStep - Math.PI / 2).toFloat()
                val y = center.y + r * sin(j * angleStep - Math.PI / 2).toFloat()
                if (j == 0) gridPath.moveTo(x, y) else gridPath.lineTo(x, y)
            }
            gridPath.close()
            drawPath(gridPath, Color.White.copy(alpha = 0.1f), style = Stroke(1.dp.toPx()))
        }

        // Draw data area with gradient fill
        val dataPath = Path()
        data.forEachIndexed { i, value ->
            val r = radius * (value / 100f) * animatedProgress
            val x = center.x + r * cos(i * angleStep - Math.PI / 2).toFloat()
            val y = center.y + r * sin(i * angleStep - Math.PI / 2).toFloat()
            if (i == 0) dataPath.moveTo(x, y) else dataPath.lineTo(x, y)
        }
        dataPath.close()

        drawPath(dataPath, color.copy(alpha = 0.3f))
        drawPath(dataPath, color, style = Stroke(2.5.dp.toPx(), cap = StrokeCap.Round))
    }
}

/**
 * Premium Animated Bar Chart with staggered bar entry and gradient fills.
 * Rule 16 compliant.
 */
@Composable
fun AnimatedBarChart(
    data: List<Float>,
    colors: List<Color>,
    modifier: Modifier = Modifier
) {
    var animationTriggered by remember { mutableStateOf(false) }
    val animatedProgress by animateFloatAsState(
        targetValue = if (animationTriggered) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "bar_chart"
    )

    LaunchedEffect(Unit) {
        animationTriggered = true
    }

    Canvas(modifier = modifier.fillMaxWidth().height(200.dp)) {
        val barCount = data.size
        val barWidth = size.width / (barCount * 2)
        val maxVal = data.maxOrNull() ?: 1f

        data.forEachIndexed { index, value ->
            // Height animation
            val barHeight = (value / maxVal) * size.height * animatedProgress
            val left = (index * 2 + 0.5f) * barWidth
            val top = size.height - barHeight

            // Draw bar with gradient
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(colors[index % colors.size], colors[index % colors.size].copy(alpha = 0.4f))
                ),
                topLeft = Offset(left, top),
                size = Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx())
            )
            
            // Draw top glow highlight
            if (barHeight > 5f) {
                drawRect(
                    color = Color.White.copy(alpha = 0.3f),
                    topLeft = Offset(left + 4f, top + 4f),
                    size = Size(barWidth - 8f, 2.dp.toPx())
                )
            }
        }
    }
}
