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

data class PlacementRosterMatch(
    val studentName: String,
    val rollNo: String,
    val company: String,
    val date: String,
    val dutyLeaveStatus: String
)

@Composable
fun ScreenPlacementSync(
    vm: MainViewModel,
    modifier: Modifier = Modifier
) {
    val matches = listOf(
        PlacementRosterMatch("G. Tarun Reddy", "22L11A0518", "TCS Digital", "2026-07-13", "Pending Approval"),
        PlacementRosterMatch("K. Divya", "22L11A0524", "Amazon AWS", "2026-07-13", "Approved"),
        PlacementRosterMatch("P. Rakesh", "22L11A05A6", "Cognizant GenC", "2026-07-12", "Auto-Synced")
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
                text = "Placement Activity Roster Sync",
                style = MaterialTheme.typography.headlineSmall.copy(color = DesignSystem.TextPrimary, fontWeight = FontWeight.Bold)
            )
            Text(text = "Verify official placement attendance lists and sync duty leaves", color = DesignSystem.TextSecondary, style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = Modifier.height(20.dp))

            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Placement Portal Sync Status", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Last synced: 2 hours ago", color = DesignSystem.TextSecondary, fontSize = 11.sp)
                    }
                    Button(
                        onClick = { /* Force sync */ },
                        colors = ButtonDefaults.buttonColors(containerColor = DesignSystem.Cyan),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Sync Now", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text("Verify Student Attendance Exclusions", color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                items(matches) { match ->
                    GlassmorphicCard {
                        Row(Modifier.padding(14.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(match.studentName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("${match.rollNo} | ${match.company}", color = DesignSystem.TextSecondary, fontSize = 11.sp)
                                Text("Drive Date: ${match.date}", color = DesignSystem.TextMuted, fontSize = 10.sp)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        when (match.dutyLeaveStatus) {
                                            "Approved" -> DesignSystem.Success.copy(alpha = 0.1f)
                                            "Auto-Synced" -> DesignSystem.Cyan.copy(alpha = 0.1f)
                                            else -> DesignSystem.Warning.copy(alpha = 0.1f)
                                        }
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    match.dutyLeaveStatus.uppercase(),
                                    fontSize = 9.sp,
                                    color = when (match.dutyLeaveStatus) {
                                        "Approved" -> DesignSystem.Success
                                        "Auto-Synced" -> DesignSystem.Cyan
                                        else -> DesignSystem.Warning
                                    },
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
