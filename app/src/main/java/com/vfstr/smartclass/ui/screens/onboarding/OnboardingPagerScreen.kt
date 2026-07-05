package com.vfstr.smartclass.ui.screens.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vfstr.smartclass.ui.theme.DesignSystem
import kotlinx.coroutines.launch

import com.vfstr.smartclass.ui.components.OnboardingAnimation
import com.vfstr.smartclass.ui.components.LegalPoliciesAccordion
import com.vfstr.smartclass.R

data class OnboardingPage(
    val title: String,
    val description: String,
    val color: Color,
    val animationRes: Int,
    val isLegal: Boolean = false
)

val onboardingPages = listOf(
    OnboardingPage(
        "Smart Attendance",
        "Experience high-fidelity 3-tier geofencing and facial recognition for secure presence verification.",
        DesignSystem.Cyan,
        R.raw.onboarding_attendance
    ),
    OnboardingPage(
        "Location & Geofencing",
        "We use high-precision GPS to verify your presence within VFSTR boundaries. Your privacy is protected via localized geofencing.",
        DesignSystem.Cyan,
        R.raw.onboarding_attendance
    ),
    OnboardingPage(
        "Secure Camera Sync",
        "Facial recognition transforms your features into secure mathematical hashes. No raw biometric images are stored on our servers.",
        DesignSystem.Violet,
        R.raw.onboarding_attendance
    ),
    OnboardingPage(
        "Real-time Analytics",
        "Monitor campus compliance and academic performance with live-updating charts and dashboard.",
        DesignSystem.Violet,
        R.raw.onboarding_analytics
    ),
    OnboardingPage(
        "Seamless Workflow",
        "Manage leaves, ODs, and timetables efficiently through a unified enterprise portal.",
        DesignSystem.Success,
        R.raw.onboarding_workflow
    ),
    OnboardingPage(
        "Legal & Policies",
        "Please review our core operating policies regarding biometric data, privacy, and academic integrity.",
        DesignSystem.Warning,
        R.raw.onboarding_workflow,
        isLegal = true
    )
)

@Composable
fun OnboardingPagerScreen(
    onOnboardingComplete: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DesignSystem.Background)
    ) {
        // Background Particle Effect (Rule 3)
        BackgroundParticles()

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { pageIndex ->
                OnboardingPageContent(onboardingPages[pageIndex])
            }

            // Bottom Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Page Indicator
                Row {
                    repeat(onboardingPages.size) { index ->
                        val isSelected = pagerState.currentPage == index
                        val width by animateDpAsState(
                            targetValue = if (isSelected) 24.dp else 8.dp,
                            label = "indicatorWidth"
                        )
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .height(8.dp)
                                .width(width)
                                .clip(CircleShape)
                                .background(if (isSelected) DesignSystem.Cyan else DesignSystem.TextMuted)
                        )
                    }
                }

                // Next Button
                Button(
                    onClick = {
                        if (pagerState.currentPage < onboardingPages.size - 1) {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            onOnboardingComplete()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DesignSystem.Cyan),
                    shape = RoundedCornerShape(DesignSystem.CornerRadius)
                ) {
                    Text(
                        text = if (pagerState.currentPage == onboardingPages.size - 1) "Get Started" else "Next",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next",
                        tint = Color.Black
                    )
                }
            }
        }
    }
}

@Composable
fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 40.dp, start = 40.dp, end = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (page.isLegal) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = page.title,
                        color = DesignSystem.TextPrimary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = page.description,
                        color = DesignSystem.TextSecondary,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    LegalPoliciesAccordion()
                }
            }
        } else {
            // Real implementation using Lottie (Rule 3)
            OnboardingAnimation(
                animationRes = page.animationRes,
                modifier = Modifier.size(280.dp)
            )

            Spacer(modifier = Modifier.height(60.dp))

            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + slideInVertically(initialOffsetY = { 20 })
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = page.title,
                        color = DesignSystem.TextPrimary,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = page.description,
                        color = DesignSystem.TextSecondary,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 26.sp
                    )
                }
            }
        }
    }
}

@Composable
fun BackgroundParticles() {
    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        // Simplified particle drawing (Rule 3)
        drawCircle(
            color = DesignSystem.Cyan.copy(alpha = alpha),
            radius = 100f,
            center = center.copy(x = center.x - 400f, y = center.y - 600f)
        )
        drawCircle(
            color = DesignSystem.Violet.copy(alpha = alpha),
            radius = 150f,
            center = center.copy(x = center.x + 300f, y = center.y + 400f)
        )
    }
}
