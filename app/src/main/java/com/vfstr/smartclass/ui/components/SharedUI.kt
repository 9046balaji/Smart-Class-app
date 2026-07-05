package com.vfstr.smartclass.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vfstr.smartclass.domain.models.ApprovalStep
import com.vfstr.smartclass.domain.models.StepState
import com.vfstr.smartclass.ui.theme.DesignSystem
import kotlin.math.sin

/**
 * Premium Glassmorphism Card with neon border glows and inner highlights.
 * Rule 8 & 23 compliant.
 */
@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    borderColor: Color = DesignSystem.Border,
    backgroundColor: Color = DesignSystem.CardBg,
    glowColor: Color = Color.Transparent,
    shape: RoundedCornerShape = RoundedCornerShape(DesignSystem.CornerRadiusLarge),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = if (glowColor != Color.Transparent) 8.dp else 0.dp,
                shape = shape,
                spotColor = glowColor,
                ambientColor = glowColor
            )
            .clip(shape)
            .background(backgroundColor)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(
                        borderColor,
                        if (glowColor != Color.Transparent) glowColor else borderColor,
                        borderColor
                    )
                ),
                shape = shape
            )
    ) {
        // Inner top-left highlight for extra glass feel
        Canvas(modifier = Modifier.matchParentSize()) {
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(Color.White.copy(alpha = 0.05f), Color.Transparent),
                    start = Offset(0f, 0f),
                    end = Offset(size.width * 0.3f, size.height * 0.3f)
                ),
                size = size,
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(DesignSystem.CornerRadiusLarge.toPx())
            )
        }
        content()
    }
}

/**
 * Animated Mesh Gradient Background with drifting radial blobs.
 */
@Composable
fun MeshBackground(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "mesh")
    
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Blob 1: Cyan
        val x1 = width * 0.2f + (width * 0.1f * sin(phase.toDouble())).toFloat()
        val y1 = height * 0.3f + (height * 0.1f * sin(phase.toDouble() * 0.7)).toFloat()
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(DesignSystem.Cyan.copy(alpha = 0.08f), Color.Transparent),
                center = Offset(x1, y1),
                radius = width * 0.6f
            ),
            center = Offset(x1, y1),
            radius = width * 0.6f
        )

        // Blob 2: Violet
        val x2 = width * 0.8f + (width * 0.1f * sin(phase.toDouble() * 0.5 + 1.0)).toFloat()
        val y2 = height * 0.7f + (height * 0.1f * sin(phase.toDouble() * 0.8 + 0.5)).toFloat()
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(DesignSystem.Violet.copy(alpha = 0.08f), Color.Transparent),
                center = Offset(x2, y2),
                radius = width * 0.7f
            ),
            center = Offset(x2, y2),
            radius = width * 0.7f
        )
    }
}

// concentric radial rings for attendance (Screen 2)
@Composable
fun ConcentricRadialGauges(
    today: Float,
    weekly: Float,
    monthly: Float,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp
) {
    var animationTriggered by remember { mutableStateOf(false) }
    
    val animatedToday by animateFloatAsState(
        targetValue = if (animationTriggered) today else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessVeryLow)
    )
    val animatedWeekly by animateFloatAsState(
        targetValue = if (animationTriggered) weekly else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessVeryLow)
    )
    val animatedMonthly by animateFloatAsState(
        targetValue = if (animationTriggered) monthly else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessVeryLow)
    )

    LaunchedEffect(Unit) {
        animationTriggered = true
    }

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 10.dp.toPx()
            val spacing = 4.dp.toPx()
            
            // Monthly (Outer) - Green
            drawArc(
                color = DesignSystem.Success.copy(alpha = 0.1f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                size = Size(size.toPx() - strokeWidth, size.toPx() - strokeWidth),
                topLeft = Offset(strokeWidth/2, strokeWidth/2)
            )
            drawArc(
                color = DesignSystem.Success,
                startAngle = -90f,
                sweepAngle = (animatedMonthly / 100f) * 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                size = Size(size.toPx() - strokeWidth, size.toPx() - strokeWidth),
                topLeft = Offset(strokeWidth/2, strokeWidth/2)
            )

            // Weekly (Middle) - Violet
            val midSize = size.toPx() - (strokeWidth * 2) - (spacing * 2)
            val midOffset = strokeWidth + spacing
            drawArc(
                color = DesignSystem.Violet.copy(alpha = 0.1f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                size = Size(midSize, midSize),
                topLeft = Offset(midOffset + strokeWidth/2, midOffset + strokeWidth/2)
            )
            drawArc(
                color = DesignSystem.Violet,
                startAngle = -90f,
                sweepAngle = (animatedWeekly / 100f) * 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                size = Size(midSize, midSize),
                topLeft = Offset(midOffset + strokeWidth/2, midOffset + strokeWidth/2)
            )

            // Today (Inner) - Cyan
            val innerSize = size.toPx() - (strokeWidth * 4) - (spacing * 4)
            val innerOffset = (strokeWidth + spacing) * 2
            drawArc(
                color = DesignSystem.Cyan.copy(alpha = 0.1f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                size = Size(innerSize, innerSize),
                topLeft = Offset(innerOffset + strokeWidth/2, innerOffset + strokeWidth/2)
            )
            drawArc(
                color = DesignSystem.Cyan,
                startAngle = -90f,
                sweepAngle = (animatedToday / 100f) * 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                size = Size(innerSize, innerSize),
                topLeft = Offset(innerOffset + strokeWidth/2, innerOffset + strokeWidth/2)
            )
        }
    }
}

// SVG-Style circular progress rings
@Composable
fun RadialGauge(
    percentage: Float,
    statusText: String,
    strokeColor: Color,
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
    strokeWidth: Dp = 8.dp
) {
    var animationTriggered by remember { mutableStateOf(false) }
    val animatedPercentage by animateFloatAsState(
        targetValue = if (animationTriggered) percentage else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessVeryLow)
    )

    LaunchedEffect(Unit) {
        animationTriggered = true
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            // Background ring track
            drawCircle(
                color = Color(0x10FFFFFF),
                radius = (size.toPx() - strokeWidth.toPx()) / 2f,
                style = Stroke(width = strokeWidth.toPx())
            )
            // Foreground dynamic level ring with glow
            drawArc(
                color = strokeColor.copy(alpha = 0.3f),
                startAngle = -90f,
                sweepAngle = (animatedPercentage / 100f) * 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx() + 4.dp.toPx(), cap = StrokeCap.Round),
                size = Size(
                    width = size.toPx() - strokeWidth.toPx(),
                    height = size.toPx() - strokeWidth.toPx()
                ),
                topLeft = Offset(strokeWidth.toPx() / 2f, strokeWidth.toPx() / 2f)
            )
            drawArc(
                color = strokeColor,
                startAngle = -90f,
                sweepAngle = (animatedPercentage / 100f) * 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round),
                size = Size(
                    width = size.toPx() - strokeWidth.toPx(),
                    height = size.toPx() - strokeWidth.toPx()
                ),
                topLeft = Offset(strokeWidth.toPx() / 2f, strokeWidth.toPx() / 2f)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${animatedPercentage.toInt()}%",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = (size.value * 0.22f).sp,
                    fontFamily = FontFamily.Monospace,
                    color = DesignSystem.TextPrimary
                )
            )
            if (statusText.isNotEmpty()) {
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = (size.value * 0.12f).sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DesignSystem.TextSecondary
                    )
                )
            }
        }
    }
}

@Composable
fun ShimmerSkeleton(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(DesignSystem.CornerRadius)
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_pos"
    )

    val shimmerColors = listOf(
        DesignSystem.Surface,
        Color(0xFF1E293B),
        DesignSystem.Surface
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - 300f, translateAnim - 300f),
        end = Offset(translateAnim, translateAnim)
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(brush)
    )
}

@Composable
fun ErrorCard(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassmorphicCard(
        modifier = modifier.fillMaxWidth(),
        borderColor = DesignSystem.Danger.copy(alpha = 0.25f),
        backgroundColor = DesignSystem.Danger.copy(alpha = 0.05f),
        glowColor = DesignSystem.Danger.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier.padding(DesignSystem.PaddingLarge),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Cancel, contentDescription = null, tint = DesignSystem.Danger, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(DesignSystem.SpacingSmall))
            Text(
                text = "Network Error",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = DesignSystem.Danger,
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(DesignSystem.SpacingSmall))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium.copy(color = DesignSystem.TextSecondary),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(DesignSystem.Padding))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = DesignSystem.Danger),
                shape = RoundedCornerShape(DesignSystem.CornerRadius)
            ) {
                Text("Tap to Retry", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ApprovalTimeline(
    steps: List<ApprovalStep>,
    modifier: Modifier = Modifier
) {
    if (steps.isEmpty()) {
        Box(modifier = modifier.fillMaxWidth().padding(DesignSystem.Padding), contentAlignment = Alignment.Center) {
            Text(
                text = "Select a request to inspect its status transitions.",
                style = MaterialTheme.typography.bodyMedium.copy(color = DesignSystem.TextSecondary),
                textAlign = TextAlign.Center
            )
        }
        return
    }

    Column(modifier = modifier.fillMaxWidth().padding(horizontal = DesignSystem.SpacingSmall)) {
        steps.forEachIndexed { index, step ->
            Row(modifier = Modifier.fillMaxWidth()) {
                // Stepper Column
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(32.dp)) {
                    val icon = when (step.state) {
                        StepState.COMPLETE -> Icons.Default.CheckCircle
                        StepState.REJECTED -> Icons.Default.Cancel
                        StepState.PENDING -> Icons.Default.Schedule
                    }
                    val color = when (step.state) {
                        StepState.COMPLETE -> DesignSystem.Success
                        StepState.REJECTED -> DesignSystem.Danger
                        StepState.PENDING -> DesignSystem.Warning
                    }

                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))

                    if (index < steps.size - 1) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(40.dp)
                                .background(DesignSystem.Border)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(DesignSystem.SpacingLarge))

                // Content Column
                Column(modifier = Modifier.weight(1f).padding(bottom = if (index < steps.size - 1) DesignSystem.Padding else 0.dp)) {
                    Text(
                        text = step.stepTitle,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = DesignSystem.TextPrimary
                        )
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = step.roleLabel,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = DesignSystem.Violet
                            )
                        )
                        if (!step.actor.isNullOrEmpty()) {
                            Text(
                                text = " • ${step.actor}",
                                style = MaterialTheme.typography.bodySmall.copy(color = DesignSystem.TextSecondary)
                            )
                        }
                    }
                    
                    if (!step.timestamp.isNullOrEmpty()) {
                        Text(
                            text = step.timestamp ?: "",
                            style = MaterialTheme.typography.labelSmall.copy(color = DesignSystem.TextMuted),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    if (!step.comment.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(DesignSystem.SpacingSmall))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, DesignSystem.Border, RoundedCornerShape(DesignSystem.SpacingSmall))
                                .background(DesignSystem.Surface.copy(alpha = 0.5f), RoundedCornerShape(DesignSystem.SpacingSmall))
                                .padding(DesignSystem.SpacingSmall)
                        ) {
                            Text(
                                text = step.comment ?: "",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = DesignSystem.TextSecondary,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
