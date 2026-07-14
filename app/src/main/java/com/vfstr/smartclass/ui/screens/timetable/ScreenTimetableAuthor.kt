package com.vfstr.smartclass.ui.screens.timetable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
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

data class AuthoringSlot(
    val id: String,
    val period: Int,
    val subject: String,
    val room: String,
    val faculty: String
)

@Composable
fun ScreenTimetableAuthor(
    vm: MainViewModel,
    modifier: Modifier = Modifier
) {
    var selectedDay by remember { mutableStateOf("Mon") }
    val slots = listOf(
        AuthoringSlot("1", 1, "Compiler Design", "LH-101", "Dr. Rama Krishna"),
        AuthoringSlot("2", 2, "Database Systems", "LH-101", "Mrs. Anuradha"),
        AuthoringSlot("3", 4, "Computer Networks", "LH-102", "Mr. Prasad"),
        AuthoringSlot("4", 5, "Machine Learning", "Lab-2", "Dr. Venkatesh")
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DesignSystem.Background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Add slot */ },
                containerColor = DesignSystem.Cyan,
                contentColor = Color.Black
            ) {
                Icon(Icons.Default.Add, "Add Slot")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(DesignSystem.Padding)
        ) {
            Text(
                text = "Timetable Authoring Matrix",
                style = MaterialTheme.typography.headlineSmall.copy(color = DesignSystem.TextPrimary, fontWeight = FontWeight.Bold)
            )
            Text(text = "Modify schedules and override slots on-the-fly", color = DesignSystem.TextSecondary, style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = Modifier.height(20.dp))

            // Day selector row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(DesignSystem.Surface)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                    val isSel = selectedDay == day
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSel) DesignSystem.Cyan.copy(alpha = 0.1f) else Color.Transparent)
                            .clickable { selectedDay = day },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(day, color = if (isSel) DesignSystem.Cyan else DesignSystem.TextSecondary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text("Assigned Period Slots", color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                items(slots) { slot ->
                    GlassmorphicCard {
                        Row(Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DesignSystem.Surface),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("P${slot.period}", color = DesignSystem.Cyan, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Spacer(Modifier.width(16.dp))
                            Column(Modifier.weight(1f)) {
                                Text(slot.subject, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("${slot.faculty} | ${slot.room}", color = DesignSystem.TextSecondary, fontSize = 11.sp)
                            }
                            IconButton(onClick = { /* Edit slot */ }) {
                                Icon(Icons.Default.Edit, "Edit", tint = DesignSystem.TextSecondary, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
