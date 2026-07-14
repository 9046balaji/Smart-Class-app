package com.vfstr.smartclass.ui.screens.hierarchy

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.vfstr.smartclass.ui.theme.DesignSystem

data class CurriculumRegulation(
    val regulation: String,
    val totalCreditsRequired: Int,
    val minimumAttendanceFloor: String,
    val condonableBand: String,
    val status: String
)

@Composable
fun ScreenCurriculum(
    vm: MainViewModel,
    modifier: Modifier = Modifier
) {
    val regulations = listOf(
        CurriculumRegulation("R22 Regulation", 160, "75%", "65% - 75%", "Active"),
        CurriculumRegulation("R18 Regulation", 160, "75%", "65% - 75%", "Archived"),
        CurriculumRegulation("R25 Proposed", 164, "80%", "70% - 80%", "Draft")
    )

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
                text = "Curriculum & Regulations Map",
                style = MaterialTheme.typography.headlineSmall.copy(color = DesignSystem.TextPrimary, fontWeight = FontWeight.Bold)
            )
            Text(text = "Manage university credit structures and compliance thresholds", color = DesignSystem.TextSecondary, style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = Modifier.height(20.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
                items(regulations) { item ->
                    GlassmorphicCard {
                        Column(Modifier.padding(16.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(item.regulation, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            when (item.status) {
                                                "Active" -> DesignSystem.Success.copy(alpha = 0.1f)
                                                "Draft" -> DesignSystem.Cyan.copy(alpha = 0.1f)
                                                else -> DesignSystem.TextMuted.copy(alpha = 0.1f)
                                            }
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        item.status.uppercase(),
                                        fontSize = 9.sp,
                                        color = when (item.status) {
                                            "Active" -> DesignSystem.Success
                                            "Draft" -> DesignSystem.Cyan
                                            else -> DesignSystem.TextSecondary
                                        },
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(Modifier.height(12.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("Credits Requirement", color = DesignSystem.TextSecondary, fontSize = 11.sp)
                                    Text("${item.totalCreditsRequired} Credits", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Attendance Floor", color = DesignSystem.TextSecondary, fontSize = 11.sp)
                                    Text(item.minimumAttendanceFloor, color = DesignSystem.Cyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Text("Condonation Allowed Band: ${item.condonableBand}", color = DesignSystem.TextMuted, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}
