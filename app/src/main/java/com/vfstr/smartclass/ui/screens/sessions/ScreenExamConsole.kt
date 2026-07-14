package com.vfstr.smartclass.ui.screens.sessions

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

data class ExamAllocation(
    val id: String,
    val roomName: String,
    val capacity: String,
    val date: String,
    val invigilator: String,
    val status: String
)

@Composable
fun ScreenExamConsole(
    vm: MainViewModel,
    modifier: Modifier = Modifier
) {
    val allocations = listOf(
        ExamAllocation("1", "Block-A Room 101", "30/30 Seats", "2026-07-15 09:30 AM", "Dr. K. Raghavan", "Scheduled"),
        ExamAllocation("2", "Block-A Room 102", "30/30 Seats", "2026-07-15 09:30 AM", "Mrs. Lakshmi", "Scheduled"),
        ExamAllocation("3", "Block-B Room 204", "45/45 Seats", "2026-07-15 02:00 PM", "Mr. Srinivasa Rao", "Scheduled")
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
                text = "Exam Logistics & Seating",
                style = MaterialTheme.typography.headlineSmall.copy(color = DesignSystem.TextPrimary, fontWeight = FontWeight.Bold)
            )
            Text(text = "Manage exam hall seating allocation and invigilators roster", color = DesignSystem.TextSecondary, style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = Modifier.height(20.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
                items(allocations) { item ->
                    GlassmorphicCard {
                        Column(Modifier.padding(16.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(item.roomName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(DesignSystem.Cyan.copy(alpha = 0.1f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(item.status.uppercase(), fontSize = 9.sp, color = DesignSystem.Cyan, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(Modifier.height(10.dp))
                            Text("Invigilator: ${item.invigilator}", color = DesignSystem.TextPrimary, fontSize = 12.sp)
                            Text("Date & Time: ${item.date}", color = DesignSystem.TextSecondary, fontSize = 11.sp)
                            Text("Allocated Capacity: ${item.capacity}", color = DesignSystem.TextMuted, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}
