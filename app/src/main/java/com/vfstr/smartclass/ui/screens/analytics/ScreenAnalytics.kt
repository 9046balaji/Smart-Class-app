package com.vfstr.smartclass.ui.screens.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vfstr.smartclass.ui.MainViewModel
import com.vfstr.smartclass.ui.components.*
import com.vfstr.smartclass.ui.theme.DesignSystem

@Composable
fun ScreenAnalytics(
    vm: MainViewModel,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf("Overview") } // Overview, Sections, Risk, Section-Wise

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DesignSystem.Background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(DesignSystem.Padding)
        ) {
            Text(
                text = "Advanced Analytics",
                style = MaterialTheme.typography.headlineSmall.copy(color = DesignSystem.TextPrimary, fontWeight = FontWeight.Bold)
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            // Navigation Tabs
            ScrollableTabRow(
                selectedTabIndex = listOf("Overview", "Sections", "Risk", "Section-Wise").indexOf(activeTab),
                containerColor = Color.Transparent,
                contentColor = DesignSystem.Cyan,
                edgePadding = 0.dp,
                divider = {}
            ) {
                listOf("Overview", "Sections", "Risk", "Section-Wise").forEach { tab ->
                    Tab(
                        selected = activeTab == tab,
                        onClick = { activeTab = tab },
                        text = { Text(tab, fontWeight = FontWeight.Bold) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            when (activeTab) {
                "Overview" -> AnalyticsOverview()
                "Sections" -> SectionsComparison()
                "Risk" -> RiskAnalysis()
                "Section-Wise" -> SectionWiseDetails()
            }
        }
    }
}

@Composable
fun AnalyticsOverview() {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            GlassmorphicCard {
                Column(Modifier.padding(DesignSystem.PaddingLarge)) {
                    Text("Weekly Attendance Trend", color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(16.dp))
                    AnimatedAreaChart(
                        data = listOf(75f, 82f, 78f, 85f, 88f, 82f),
                        color = DesignSystem.Cyan,
                        modifier = Modifier.height(180.dp)
                    )
                }
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                GlassmorphicCard(modifier = Modifier.weight(1f)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Top Department", color = DesignSystem.TextSecondary, fontSize = 11.sp)
                        Text("CSAI", color = DesignSystem.Success, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                        Text("89.2% rate", color = DesignSystem.TextMuted, fontSize = 10.sp)
                    }
                }
                GlassmorphicCard(modifier = Modifier.weight(1f)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Growth", color = DesignSystem.TextSecondary, fontSize = 11.sp)
                        Text("+4.2%", color = DesignSystem.Cyan, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                        Text("vs last month", color = DesignSystem.TextMuted, fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun SectionsComparison() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        GlassmorphicCard {
            Column(Modifier.padding(DesignSystem.PaddingLarge), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Performance Distribution (Radar)", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
                Spacer(Modifier.height(16.dp))
                AnimatedRadarChart(
                    data = listOf(85f, 72f, 90f, 65f, 80f),
                    labels = listOf("AI", "ML", "CS", "IT", "EC"),
                    color = DesignSystem.Violet,
                    modifier = Modifier.size(220.dp)
                )
            }
        }
    }
}

@Composable
fun RiskAnalysis() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        GlassmorphicCard {
            Row(Modifier.padding(DesignSystem.PaddingLarge).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Risk Categorization", color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("● Safe ( >75%)", color = DesignSystem.Success, fontSize = 12.sp)
                    Text("● Watch (65-75%)", color = DesignSystem.Warning, fontSize = 12.sp)
                    Text("● Critical (<65%)", color = DesignSystem.Danger, fontSize = 12.sp)
                }
                AnimatedDonutChart(
                    data = listOf("Safe" to 142f, "Watch" to 28f, "Critical" to 15f),
                    colors = listOf(DesignSystem.Success, DesignSystem.Warning, DesignSystem.Danger),
                    modifier = Modifier.size(120.dp)
                )
            }
        }
    }
}

@Composable
fun SectionWiseDetails() {
    Column {
        Text("Section Performance Ranking", color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        AnimatedBarChart(
            data = listOf(88f, 84f, 81f, 76f, 72f),
            colors = listOf(DesignSystem.Cyan, DesignSystem.Cyan, DesignSystem.Cyan, DesignSystem.Warning, DesignSystem.Warning),
            modifier = Modifier.height(200.dp)
        )
    }
}
