package com.vfstr.smartclass.ui.screens.attendance

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vfstr.smartclass.domain.models.AttendanceEvent
import com.vfstr.smartclass.domain.models.AttendanceSource
import com.vfstr.smartclass.domain.models.AttendanceStatus
import com.vfstr.smartclass.ui.MainViewModel
import com.vfstr.smartclass.ui.components.GlassmorphicCard
import com.vfstr.smartclass.ui.theme.DesignSystem

import com.vfstr.smartclass.ui.components.StickyDataTable
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceEventViewer(
    vm: MainViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val events by vm.attendanceEvents.collectAsState()
    val loading by vm.eventsLoading.collectAsState()

    var activeFilt by remember { mutableStateOf("All") }
    var viewMode by remember { mutableStateOf("Table") } // Table or Cards

    LaunchedEffect(Unit) {
        vm.loadAttendanceEvents()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DesignSystem.Background,
        topBar = {
            TopAppBar(
                title = { Text("Attendance Records", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { viewMode = if (viewMode == "Table") "Cards" else "Table" }) {
                        Icon(if (viewMode == "Table") Icons.Default.Dashboard else Icons.Default.TableChart, null, tint = DesignSystem.Cyan)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DesignSystem.Surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val chips = listOf("All", "Present", "Absent", "Late", "Override")
                items(chips) { chip ->
                    val isSel = chip == activeFilt
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSel) DesignSystem.Cyan.copy(alpha = 0.15f) else DesignSystem.CardBg)
                            .border(1.dp, if (isSel) DesignSystem.Cyan else DesignSystem.Border, RoundedCornerShape(12.dp))
                            .clickable {
                                activeFilt = chip
                                vm.loadAttendanceEvents(null, null, if (chip == "All") null else chip)
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = chip,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSel) DesignSystem.Cyan else DesignSystem.TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (loading) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(8) {
                        com.vfstr.smartclass.ui.components.ShimmerSkeleton(modifier = Modifier.fillMaxWidth().height(80.dp))
                    }
                }
            } else if (viewMode == "Cards") {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(events) { ev ->
                        AttendanceEventViewerRow(ev)
                    }
                }
            } else {
                // Rule 15: Sticky Data Table for Attendance Logs
                StickyDataTable(
                    headers = listOf("Roll No", "Student Name", "Room", "Status", "Confidence", "Timestamp"),
                    rows = events.map { ev ->
                        listOf(
                            ev.rollNo,
                            ev.studentName ?: "Unknown",
                            ev.room,
                            ev.status.name,
                            "${(ev.confidence * 100).toInt()}%",
                            ev.timestamp.takeLast(8)
                        )
                    },
                    columnWidths = listOf(100.dp, 150.dp, 80.dp, 100.dp, 100.dp, 100.dp),
                    stickyColumns = 2,
                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).border(1.dp, DesignSystem.Border, RoundedCornerShape(12.dp))
                )
            }
        }
    }
}

@Composable
fun AttendanceEventViewerRow(ev: AttendanceEvent) {
    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = ev.studentName ?: ev.rollNo, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    SourceBadge(ev.source)
                }
                Text(text = "Roll: ${ev.rollNo} • Room: ${ev.room}", fontSize = 11.sp, color = DesignSystem.TextSecondary)
                
                Spacer(modifier = Modifier.height(10.dp))
                
                ConfidenceBar(ev.confidence)
            }

            Column(horizontalAlignment = Alignment.End) {
                val color = when (ev.status) {
                    AttendanceStatus.Present -> DesignSystem.Success
                    AttendanceStatus.Absent -> DesignSystem.Danger
                    AttendanceStatus.Late -> DesignSystem.Warning
                    else -> DesignSystem.Violet
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(color.copy(alpha = 0.1f))
                        .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(text = ev.status.name.uppercase(), fontSize = 10.sp, color = color, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = ev.timestamp, fontSize = 9.sp, color = DesignSystem.TextMuted)
            }
        }
    }
}

@Composable
fun SourceBadge(source: AttendanceSource) {
    val (icon, color) = when (source) {
        AttendanceSource.Camera -> Icons.Default.CameraAlt to DesignSystem.Cyan
        AttendanceSource.Pencil -> Icons.Default.Edit to Color(0xFF3B82F6) // blue
        AttendanceSource.Override -> Icons.Default.Shield to DesignSystem.Violet
        AttendanceSource.OD -> Icons.Default.Description to DesignSystem.Warning
    }

    Box(
        modifier = Modifier
            .size(20.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(12.dp))
    }
}

@Composable
fun ConfidenceBar(confidence: Float) {
    val color = when {
        confidence < 0.65f -> DesignSystem.Danger
        confidence < 0.85f -> DesignSystem.Warning
        else -> DesignSystem.Success
    }

    Column {
        Row(modifier = Modifier.fillMaxWidth(0.6f), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "Match confidence", fontSize = 9.sp, color = DesignSystem.TextMuted)
            Text(text = "${(confidence * 100).toInt()}%", fontSize = 9.sp, color = color, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(2.dp))
        Box(
            modifier = Modifier
                .width(120.dp)
                .height(4.dp)
                .clip(CircleShape)
                .background(DesignSystem.Border)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(confidence)
                    .background(color)
            )
        }
    }
}
