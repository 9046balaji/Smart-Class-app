package com.vfstr.smartclass.ui.screens.timetable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vfstr.smartclass.domain.models.TimetableSlot
import com.vfstr.smartclass.ui.MainViewModel
import com.vfstr.smartclass.ui.components.GlassmorphicCard
import com.vfstr.smartclass.ui.theme.DesignSystem

import com.vfstr.smartclass.ui.components.StickyDataTable
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenTimetable(
    vm: MainViewModel,
    modifier: Modifier = Modifier
) {
    val list by vm.timetableSlots.collectAsState()
    val previewList by vm.timetablePreviewList.collectAsState()
    var selectedDay by remember { mutableStateOf("Monday") }
    var showUploadSheet by remember { mutableStateOf(false) }
    
    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
    val loading by vm.timetableLoading.collectAsState()

    LaunchedEffect(Unit) {
        vm.loadTimetable()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DesignSystem.Background,
        floatingActionButton = {
            FloatingActionButton(onClick = { showUploadSheet = true }, containerColor = DesignSystem.Violet) {
                Icon(Icons.Default.FileUpload, null, tint = Color.White)
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
                text = "Academic Timetable",
                style = MaterialTheme.typography.headlineSmall.copy(color = DesignSystem.TextPrimary, fontWeight = FontWeight.Bold)
            )
            Text(text = "R22 Regulation - 2024 Semester", color = DesignSystem.TextSecondary, style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = Modifier.height(20.dp))

            // Day Selector
            ScrollableTabRow(
                selectedTabIndex = days.indexOf(selectedDay),
                containerColor = Color.Transparent,
                contentColor = DesignSystem.Cyan,
                edgePadding = 0.dp,
                divider = {}
            ) {
                days.forEach { day ->
                    Tab(
                        selected = selectedDay == day,
                        onClick = { selectedDay = day },
                        text = { Text(day.take(3), fontWeight = FontWeight.Bold) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (loading && list.isEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    repeat(5) { com.vfstr.smartclass.ui.components.ShimmerSkeleton(modifier = Modifier.fillMaxWidth().height(80.dp)) }
                }
            } else {
                val filteredSlots = list.filter { it.dayOfWeek == selectedDay || it.dayOfWeek == (days.indexOf(selectedDay) + 1).toString() }
                    .sortedBy { it.period }

                if (filteredSlots.isEmpty()) {
                    Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No classes scheduled for $selectedDay", color = DesignSystem.TextMuted)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredSlots) { slot ->
                            TimetableSlotItem(slot)
                        }
                    }
                }
            }
        }

        if (showUploadSheet) {
            ModalBottomSheet(
                onDismissRequest = { showUploadSheet = false },
                containerColor = DesignSystem.Surface
            ) {
                Column(modifier = Modifier.padding(DesignSystem.PaddingLarge).fillMaxHeight(0.8f)) {
                    Text("Upload Master Timetable", style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold))
                    Text("Review parsed rows before applying", color = DesignSystem.TextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    if (previewList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .border(2.dp, DesignSystem.Border, RoundedCornerShape(DesignSystem.CornerRadius))
                                .clickable { 
                                    // Simulation (Rule 17)
                                    vm.timetablePreviewList.value = listOf(
                                        TimetableSlot("tmp1", "Monday", 1, "09:00", "09:50", "CS501L", "AI", "L", "A-402", null, null, null, "Dr Rao", "CSAI", "III", "A", false),
                                        TimetableSlot("tmp2", "Monday", 2, "10:00", "10:50", "CS503P", "ML Lab", "P", "A-401", null, null, null, "Mrs Vani", "CSAI", "III", "A", true),
                                        TimetableSlot("tmp3", "Monday", 3, "11:00", "11:50", "CS504T", "Compiler", "L", "A-402", null, null, null, "Mrs Vani", "CSAI", "III", "A", false)
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.UploadFile, contentDescription = null, tint = DesignSystem.TextMuted, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Select Timetable Excel (.xlsx)", color = DesignSystem.TextSecondary)
                            }
                        }
                    } else {
                        // Rule 15 & 17: Sticky Data Table for Preview
                        StickyDataTable(
                            headers = listOf("P", "Subject", "Code", "Room", "Faculty"),
                            rows = previewList.map { 
                                listOf(it.period.toString(), it.subjectName, it.subjectCode, it.room, it.facultyName)
                            },
                            columnWidths = listOf(50.dp, 150.dp, 80.dp, 80.dp, 120.dp),
                            stickyColumns = 2,
                            modifier = Modifier.weight(1f).clip(RoundedCornerShape(DesignSystem.CornerRadius)).border(1.dp, DesignSystem.Border, RoundedCornerShape(DesignSystem.CornerRadius))
                        )

                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = {
                                vm.confirmImportedSlots()
                                showUploadSheet = false
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DesignSystem.Success),
                            shape = RoundedCornerShape(DesignSystem.CornerRadius)
                        ) {
                            Text("Confirm & Apply Timetable", fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimetableSlotItem(slot: TimetableSlot) {
    GlassmorphicCard {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(DesignSystem.Surface)
                    .border(1.dp, DesignSystem.Border, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = slot.period.toString(), color = DesignSystem.Cyan, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = slot.subjectName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "${slot.startTime} - ${slot.endTime}", color = DesignSystem.TextSecondary, fontSize = 11.sp)
                    Spacer(Modifier.width(8.dp))
                    Box(Modifier.size(4.dp).clip(CircleShape).background(DesignSystem.TextMuted))
                    Spacer(Modifier.width(8.dp))
                    Text(text = slot.room, color = DesignSystem.TextSecondary, fontSize = 11.sp)
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (slot.isLab) DesignSystem.Success.copy(alpha = 0.1f) else DesignSystem.Violet.copy(alpha = 0.1f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(text = if (slot.isLab) "LAB" else "LEC", fontSize = 9.sp, color = if (slot.isLab) DesignSystem.Success else DesignSystem.Violet, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(4.dp))
                Text(text = slot.facultyName, color = DesignSystem.TextMuted, fontSize = 10.sp)
            }
        }
    }
}
