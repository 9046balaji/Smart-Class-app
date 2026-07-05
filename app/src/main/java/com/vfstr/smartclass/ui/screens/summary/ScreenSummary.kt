package com.vfstr.smartclass.ui.screens.summary

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vfstr.smartclass.ui.MainViewModel
import com.vfstr.smartclass.ui.components.*
import com.vfstr.smartclass.ui.theme.DesignSystem

@Composable
fun ScreenSummary(
    vm: MainViewModel,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DesignSystem.Background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(DesignSystem.PaddingLarge),
            verticalArrangement = Arrangement.spacedBy(DesignSystem.SpacingLarge)
        ) {
            item {
                Text(
                    text = "Consolidated Report",
                    style = MaterialTheme.typography.headlineSmall.copy(color = DesignSystem.TextPrimary, fontWeight = FontWeight.Bold)
                )
                Text(text = "Semester performance & compliance metrics", color = DesignSystem.TextSecondary, style = MaterialTheme.typography.bodySmall)
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(DesignSystem.SpacingMedium)) {
                    SummaryMetricCard("Overall %", "84.2%", DesignSystem.Cyan, Modifier.weight(1f))
                    SummaryMetricCard("Eligible", "142", DesignSystem.Success, Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(DesignSystem.SpacingMedium))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(DesignSystem.SpacingMedium)) {
                    SummaryMetricCard("Conditional", "28", DesignSystem.Warning, Modifier.weight(1f))
                    SummaryMetricCard("Barred", "15", DesignSystem.Danger, Modifier.weight(1f))
                }
            }

            item {
                GlassmorphicCard {
                    Column(Modifier.padding(DesignSystem.PaddingLarge)) {
                        Text("Subject Breakdown", color = Color.White, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(16.dp))
                        AnimatedBarChart(
                            data = listOf(82f, 78f, 91f, 65f, 88f),
                            colors = listOf(DesignSystem.Cyan, DesignSystem.Violet, DesignSystem.Success, DesignSystem.Danger, DesignSystem.Warning),
                            modifier = Modifier.height(180.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Top performing subjects vs lowest threshold", color = DesignSystem.TextMuted, fontSize = 10.sp)
                    }
                }
            }

            item {
                GlassmorphicCard {
                    Row(
                        modifier = Modifier.padding(DesignSystem.PaddingLarge).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Total Distribution", color = Color.White, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            Text("Present: 842", color = DesignSystem.Success, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("Absent: 178", color = DesignSystem.Danger, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        AnimatedDonutChart(
                            data = listOf("Present" to 842f, "Absent" to 178f),
                            colors = listOf(DesignSystem.Success, DesignSystem.Danger),
                            modifier = Modifier.size(120.dp)
                        )
                    }
                }
            }

            item {
                Text("Student Compliance Detail", color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                StickyDataTable(
                    headers = listOf("Roll No", "Name", "Attendance %", "Status", "Review Week"),
                    rows = listOf(
                        listOf("22L11A0501", "Abhishek Naidu", "82.5%", "ELIGIBLE", "12"),
                        listOf("22L11A0502", "Bhavana Rao", "74.1%", "CONDITIONAL", "12"),
                        listOf("22L11A0503", "Chaitanya K", "91.8%", "ELIGIBLE", "12"),
                        listOf("22L11A0504", "Divya S", "65.2%", "BARRED", "12")
                    ),
                    columnWidths = listOf(100.dp, 150.dp, 100.dp, 100.dp, 100.dp),
                    stickyColumns = 1,
                    modifier = Modifier.clip(RoundedCornerShape(DesignSystem.CornerRadius))
                )
            }

            item {
                Surface(
                    color = DesignSystem.Warning.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, null, tint = DesignSystem.Warning)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Upcoming R22 Review Cycle (Week 12) starts in 3 days. Ensure all OD and Leave requests are processed.",
                            color = DesignSystem.Warning,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryMetricCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    GlassmorphicCard(modifier = modifier) {
        Column(Modifier.padding(DesignSystem.PaddingLarge)) {
            Text(label, color = DesignSystem.TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(value, color = color, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}
