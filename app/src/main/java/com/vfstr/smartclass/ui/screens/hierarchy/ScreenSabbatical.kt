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

data class SabbaticalRecord(
    val studentName: String,
    val rollNo: String,
    val type: String,
    val duration: String,
    val creditTransferStatus: String
)

@Composable
fun ScreenSabbatical(
    vm: MainViewModel,
    modifier: Modifier = Modifier
) {
    val records = listOf(
        SabbaticalRecord("J. Vamsi Krishna", "21L11A0512", "Sabbatical (Industry)", "Jan 2026 - Jun 2026", "Audited & Approved"),
        SabbaticalRecord("K. Divyansh", "22L11A0536", "DROP Semester (Medical)", "Dec 2025 - May 2026", "Pending Audit"),
        SabbaticalRecord("P. Harsha", "22L11A0589", "Sabbatical (Startup)", "Jul 2025 - Dec 2025", "Credits Transferred")
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
                text = "Sabbatical & DROP Registry",
                style = MaterialTheme.typography.headlineSmall.copy(color = DesignSystem.TextPrimary, fontWeight = FontWeight.Bold)
            )
            Text(text = "Track sabbatical semesters, drop logs, and credit compliance", color = DesignSystem.TextSecondary, style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = Modifier.height(20.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
                items(records) { item ->
                    GlassmorphicCard {
                        Column(Modifier.padding(16.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text(item.studentName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text(item.rollNo, color = DesignSystem.TextSecondary, fontSize = 11.sp)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            when (item.creditTransferStatus) {
                                                "Pending Audit" -> DesignSystem.Warning.copy(alpha = 0.1f)
                                                else -> DesignSystem.Success.copy(alpha = 0.1f)
                                            }
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        item.creditTransferStatus.uppercase(),
                                        fontSize = 9.sp,
                                        color = if (item.creditTransferStatus == "Pending Audit") DesignSystem.Warning else DesignSystem.Success,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(Modifier.height(12.dp))
                            Text("Sabbatical Type: ${item.type}", color = Color.White, fontSize = 12.sp)
                            Text("Duration: ${item.duration}", color = DesignSystem.TextSecondary, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}
