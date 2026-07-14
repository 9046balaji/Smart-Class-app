package com.vfstr.smartclass.ui.screens.audit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.vfstr.smartclass.ui.components.GlassmorphicCard
import com.vfstr.smartclass.ui.theme.DesignSystem

data class GrievanceTicket(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val submittedBy: String,
    val date: String,
    val status: String
)

@Composable
fun ScreenGrievance(
    vm: MainViewModel,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf("Open") }
    val tickets = listOf(
        GrievanceTicket("G-102", "Frequent Face Detection Failures in LH-204", "The CameraX edge node frequently fails to register student biometrics in poor lighting.", "Hardware/Camera", "22L11A0512", "2026-07-13", "Open"),
        GrievanceTicket("G-98", "Incorrect Attendance Calculation", "My attendance for the ML class on July 10th shows absent, but I was present.", "Incorrect Marking", "22L11A0545", "2026-07-12", "Open"),
        GrievanceTicket("G-85", "SLA delay on Mark Re-evaluation", "Mid term marks appeal not resolved within 72h window.", "Process/SLA", "22L11A05B1", "2026-07-10", "Resolved")
    )

    val listToShow = tickets.filter { if (activeTab == "Open") it.status == "Open" else it.status == "Resolved" }

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
                text = "Grievance Desk Manager",
                style = MaterialTheme.typography.headlineSmall.copy(color = DesignSystem.TextPrimary, fontWeight = FontWeight.Bold)
            )
            Text(text = "Manage and address student/parent compliance complaints", color = DesignSystem.TextSecondary, style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = Modifier.height(20.dp))

            // Tab switcher
            Row(modifier = Modifier.fillMaxWidth().height(44.dp).clip(RoundedCornerShape(10.dp)).background(DesignSystem.Surface).padding(4.dp)) {
                listOf("Open", "Resolved").forEach { tab ->
                    val isSel = activeTab == tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSel) DesignSystem.Cyan.copy(alpha = 0.1f) else Color.Transparent)
                            .clickable { activeTab = tab },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(tab, color = if (isSel) DesignSystem.Cyan else DesignSystem.TextSecondary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
                items(listToShow) { ticket ->
                    GlassmorphicCard {
                        Column(Modifier.padding(16.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text("${ticket.id}: ${ticket.title}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text("By ${ticket.submittedBy} | ${ticket.category}", color = DesignSystem.TextSecondary, fontSize = 11.sp)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (ticket.status == "Open") DesignSystem.Danger.copy(alpha = 0.1f) else DesignSystem.Success.copy(alpha = 0.1f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        ticket.status.uppercase(),
                                        fontSize = 9.sp,
                                        color = if (ticket.status == "Open") DesignSystem.Danger else DesignSystem.Success,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(Modifier.height(10.dp))
                            Text(ticket.description, color = DesignSystem.TextPrimary, fontSize = 12.sp)
                            Spacer(Modifier.height(12.dp))

                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Date: ${ticket.date}", color = DesignSystem.TextMuted, fontSize = 11.sp)
                                if (ticket.status == "Open") {
                                    Button(
                                        onClick = { /* Mark Resolved */ },
                                        colors = ButtonDefaults.buttonColors(containerColor = DesignSystem.Success),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(32.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp)
                                    ) {
                                        Text("Mark Resolved", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
