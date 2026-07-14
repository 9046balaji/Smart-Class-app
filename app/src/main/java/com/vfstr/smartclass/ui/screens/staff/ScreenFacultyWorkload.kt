package com.vfstr.smartclass.ui.screens.staff

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vfstr.smartclass.ui.MainViewModel
import com.vfstr.smartclass.ui.components.GlassmorphicCard
import com.vfstr.smartclass.ui.components.AnimatedBarChart
import com.vfstr.smartclass.ui.theme.DesignSystem

@Composable
fun ScreenFacultyWorkload(
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
                .padding(DesignSystem.Padding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Faculty Workload & Variance",
                    style = MaterialTheme.typography.headlineSmall.copy(color = DesignSystem.TextPrimary, fontWeight = FontWeight.Bold)
                )
                Text(text = "Monitor teaching workload, actual sessions, and pay compliance", color = DesignSystem.TextSecondary, style = MaterialTheme.typography.bodySmall)
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    GlassmorphicCard(modifier = Modifier.weight(1f)) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Target Hours", color = DesignSystem.TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("16.0h / wk", color = DesignSystem.Cyan, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                    GlassmorphicCard(modifier = Modifier.weight(1f)) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Delivered Hours", color = DesignSystem.TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("18.5h / wk", color = DesignSystem.Success, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }

            item {
                GlassmorphicCard {
                    Column(Modifier.padding(16.dp)) {
                        Text("Variance Trend (Past 5 Weeks)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(Modifier.height(16.dp))
                        AnimatedBarChart(
                            data = listOf(0.5f, 1.2f, -0.8f, 2.5f, 1.8f),
                            colors = listOf(DesignSystem.Cyan, DesignSystem.Success, DesignSystem.Danger, DesignSystem.Success, DesignSystem.Cyan),
                            modifier = Modifier.height(150.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("Green represents overload, red indicates deficit hours", color = DesignSystem.TextMuted, fontSize = 10.sp)
                    }
                }
            }

            item {
                Text("Department Workload Summaries", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(Modifier.height(8.dp))
                listOf(
                    Triple("CSE Department", "CSE-A, CSE-B, CSE-C", "+14.5 hours"),
                    Triple("ECE Department", "ECE-A, ECE-B", "-4.0 hours"),
                    Triple("IT Department", "IT-A", "+2.2 hours")
                ).forEach { (dept, sections, variance) ->
                    GlassmorphicCard(modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth()) {
                        Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text(dept, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(sections, color = DesignSystem.TextSecondary, fontSize = 11.sp)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (variance.startsWith("+")) DesignSystem.Success.copy(alpha = 0.1f) else DesignSystem.Danger.copy(alpha = 0.1f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    variance,
                                    fontSize = 11.sp,
                                    color = if (variance.startsWith("+")) DesignSystem.Success else DesignSystem.Danger,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
