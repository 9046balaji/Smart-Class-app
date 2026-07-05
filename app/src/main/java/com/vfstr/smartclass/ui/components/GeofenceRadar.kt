package com.vfstr.smartclass.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vfstr.smartclass.ui.theme.DesignSystem

@Composable
fun GeofenceRadar(
    tier1: Boolean,
    tier2: Boolean,
    tier3: Boolean,
    message: String,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "radar_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(size * 1.2f)) {
            Canvas(modifier = Modifier.size(size)) {
                val strokeWidth = 8f
                
                // Tier 1 Ring (Campus)
                drawCircle(
                    color = if (tier1) DesignSystem.Cyan else DesignSystem.TextMuted.copy(alpha = 0.3f),
                    radius = size.toPx() / 2,
                    style = Stroke(width = strokeWidth)
                )

                // Tier 2 Ring (Building)
                drawCircle(
                    color = if (tier2) DesignSystem.Violet else DesignSystem.TextMuted.copy(alpha = 0.3f),
                    radius = size.toPx() / 2.8f,
                    style = Stroke(width = strokeWidth)
                )

                // Tier 3 Ring (Classroom)
                drawCircle(
                    color = if (tier3) DesignSystem.Success else DesignSystem.TextMuted.copy(alpha = 0.3f),
                    radius = size.toPx() / 5f,
                    style = Stroke(width = strokeWidth)
                )
                
                // Core Pulse
                if (tier1 || tier2 || tier3) {
                    drawCircle(
                        color = (if (tier3) DesignSystem.Success else if (tier2) DesignSystem.Violet else DesignSystem.Cyan).copy(alpha = 0.2f),
                        radius = (size.toPx() / 10f) * pulseScale
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                color = if (tier1) DesignSystem.Cyan else DesignSystem.TextMuted,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
            )
        )
    }
}
