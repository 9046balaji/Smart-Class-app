package com.vfstr.smartclass.ui.screens.students

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

data class HostelOutingAnomaly(
    val studentName: String,
    val rollNo: String,
    val outingTime: String,
    val classroomSession: String,
    val resolutionStatus: String
)

@Composable
fun ScreenHostelSync(
    vm: MainViewModel,
    modifier: Modifier = Modifier
) {
    val anomalies = listOf(
        HostelOutingAnomaly("A. Kiran Kumar", "22L11A0502", "10:30 AM - Outpass", "Compiler Design (10:30 AM)", "Unresolved Anomaly"),
        HostelOutingAnomaly("M. Sneha", "22L11A0532", "02:15 PM - Local Outing", "ML Lab (01:10 PM)", "Resolved (Duty Leave Approved)"),
        HostelOutingAnomaly("S. Rohit", "22L11A05F4", "11:20 AM - Health Emergency", "Data Structures (11:20 AM)", "Resolved (Medical)")
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
                text = "Hostel Outing Cross-Reference",
                style = MaterialTheme.typography.headlineSmall.copy(color = DesignSystem.TextPrimary, fontWeight = FontWeight.Bold)
            )
            Text(text = "Map warden out-passes with classroom session records", color = DesignSystem.TextSecondary, style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = Modifier.height(20.dp))

            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Gate Biometrics Bridge", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Online - 3 active gate readers linked", color = DesignSystem.Success, fontSize = 11.sp)
                    }
                    Button(
                        onClick = { /* Audit log */ },
                        colors = ButtonDefaults.buttonColors(containerColor = DesignSystem.Violet),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Run Audit", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text("Outpass vs Session Anomalies", color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                items(anomalies) { item ->
                    GlassmorphicCard {
                        Row(Modifier.padding(14.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(item.studentName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(item.rollNo, color = DesignSystem.TextSecondary, fontSize = 11.sp)
                                Text("Gate Outpass: ${item.outingTime}", color = DesignSystem.Warning, fontSize = 11.sp)
                                Text("Conflict Session: ${item.classroomSession}", color = DesignSystem.TextMuted, fontSize = 10.sp)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (item.resolutionStatus.startsWith("Unresolved")) DesignSystem.Danger.copy(alpha = 0.1f) else DesignSystem.Success.copy(alpha = 0.1f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    item.resolutionStatus.uppercase(),
                                    fontSize = 8.sp,
                                    color = if (item.resolutionStatus.startsWith("Unresolved")) DesignSystem.Danger else DesignSystem.Success,
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
