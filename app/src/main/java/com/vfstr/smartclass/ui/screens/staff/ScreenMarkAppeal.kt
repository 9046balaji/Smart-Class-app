package com.vfstr.smartclass.ui.screens.staff

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vfstr.smartclass.ui.MainViewModel
import com.vfstr.smartclass.ui.components.GlassmorphicCard
import com.vfstr.smartclass.ui.theme.DesignSystem

data class MarkAppeal(
    val id: String,
    val studentName: String,
    val rollNo: String,
    val subject: String,
    val midMarks: String,
    val appealReason: String,
    val hoursRemaining: Int,
    val status: String
)

@Composable
fun ScreenMarkAppeal(
    vm: MainViewModel,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf("Pending") }
    val appeals = listOf(
        MarkAppeal("1", "K. Sunil Kumar", "22L11A0512", "Data Structures", "12/15", "Biometric mismatch during Mid-1 exam", 18, "Pending"),
        MarkAppeal("2", "M. Sri Lekha", "22L11A0545", "Compiler Design", "10/15", "Hospitalized on exam day, medical certificate attached", 42, "Pending"),
        MarkAppeal("3", "P. Ajay Vardhan", "22L11A05B1", "Linear Algebra", "14/15", "Re-evaluation requested for Question 4", 0, "Resolved")
    )

    val listToShow = appeals.filter { if (activeTab == "Pending") it.status == "Pending" else it.status == "Resolved" }

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
                text = "Mark Appeal & SLA Dashboard",
                style = MaterialTheme.typography.headlineSmall.copy(color = DesignSystem.TextPrimary, fontWeight = FontWeight.Bold)
            )
            Text(text = "Review student appeals within the 72-hour SLA window", color = DesignSystem.TextSecondary, style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = Modifier.height(20.dp))

            // Tab switcher
            Row(modifier = Modifier.fillMaxWidth().height(44.dp).clip(RoundedCornerShape(10.dp)).background(DesignSystem.Surface).padding(4.dp)) {
                listOf("Pending", "Resolved").forEach { tab ->
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
                if (listToShow.isEmpty()) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("No appeals in this category.", color = DesignSystem.TextMuted, fontSize = 14.sp)
                        }
                    }
                } else {
                    items(listToShow) { appeal ->
                        GlassmorphicCard {
                            Column(Modifier.padding(16.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Column {
                                        Text(appeal.studentName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Text("${appeal.rollNo} | ${appeal.subject}", color = DesignSystem.TextSecondary, fontSize = 11.sp)
                                    }
                                    if (appeal.status == "Pending") {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (appeal.hoursRemaining < 24) DesignSystem.Danger.copy(alpha = 0.1f) else DesignSystem.Warning.copy(alpha = 0.1f))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Default.HourglassEmpty,
                                                    null,
                                                    tint = if (appeal.hoursRemaining < 24) DesignSystem.Danger else DesignSystem.Warning,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(Modifier.width(4.dp))
                                                Text(
                                                    "${appeal.hoursRemaining}h SLA",
                                                    fontSize = 10.sp,
                                                    color = if (appeal.hoursRemaining < 24) DesignSystem.Danger else DesignSystem.Warning,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(DesignSystem.Success.copy(alpha = 0.1f))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text("RESOLVED", fontSize = 10.sp, color = DesignSystem.Success, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Spacer(Modifier.height(12.dp))
                                Text(appeal.appealReason, color = DesignSystem.TextPrimary, fontSize = 12.sp)

                                Spacer(Modifier.height(16.dp))
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text("Current Marks: ${appeal.midMarks}", color = DesignSystem.TextSecondary, fontSize = 11.sp)
                                    if (appeal.status == "Pending") {
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Button(
                                                onClick = { /* Reject */ },
                                                colors = ButtonDefaults.buttonColors(containerColor = DesignSystem.Danger.copy(alpha = 0.2f), contentColor = DesignSystem.Danger),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.height(32.dp),
                                                contentPadding = PaddingValues(horizontal = 12.dp)
                                            ) {
                                                Text("Reject", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Button(
                                                onClick = { /* Resolve */ },
                                                colors = ButtonDefaults.buttonColors(containerColor = DesignSystem.Success),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.height(32.dp),
                                                contentPadding = PaddingValues(horizontal = 12.dp)
                                            ) {
                                                Text("Approve", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
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
    }
}
