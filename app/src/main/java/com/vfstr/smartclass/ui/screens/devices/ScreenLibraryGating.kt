package com.vfstr.smartclass.ui.screens.devices

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

data class LibraryAccessConfig(
    val studentName: String,
    val rollNo: String,
    val reasonBlocked: String,
    val isBlocked: Boolean
)

@Composable
fun ScreenLibraryGating(
    vm: MainViewModel,
    modifier: Modifier = Modifier
) {
    val configs = listOf(
        LibraryAccessConfig("G. Tarun Reddy", "22L11A0518", "Tuition Fee Dues Outstanding", true),
        LibraryAccessConfig("K. Sunil Kumar", "22L11A0512", "Attendance defaulter (< 65%)", true),
        LibraryAccessConfig("P. Ajay", "22L11A05B1", "Active profile - no blocks", false)
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
                text = "Library Gating & Access",
                style = MaterialTheme.typography.headlineSmall.copy(color = DesignSystem.TextPrimary, fontWeight = FontWeight.Bold)
            )
            Text(text = "Gate card reader access control block list", color = DesignSystem.TextSecondary, style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = Modifier.height(20.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
                items(configs) { item ->
                    GlassmorphicCard {
                        Row(Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(item.studentName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text(item.rollNo, color = DesignSystem.TextSecondary, fontSize = 11.sp)
                                if (item.isBlocked) {
                                    Text("Reason: ${item.reasonBlocked}", color = DesignSystem.Danger, fontSize = 11.sp)
                                } else {
                                    Text(item.reasonBlocked, color = DesignSystem.Success, fontSize = 11.sp)
                                }
                            }
                            Button(
                                onClick = { /* Toggle */ },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (item.isBlocked) DesignSystem.Success else DesignSystem.Danger
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text(
                                    if (item.isBlocked) "Unblock" else "Block",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
