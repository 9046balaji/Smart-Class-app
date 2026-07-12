package com.vfstr.smartclass.ui.screens.studentportal

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vfstr.smartclass.ui.MainViewModel
import com.vfstr.smartclass.ui.components.GlassmorphicCard
import com.vfstr.smartclass.ui.screens.EmptyStatePlaceholder
import com.vfstr.smartclass.ui.theme.DesignSystem
import java.util.Calendar
import java.util.Locale

@Composable
fun ScreenStudentAttendance(
    vm: MainViewModel,
    modifier: Modifier = Modifier
) {
    val eligibility by vm.studentEligibility.collectAsState()
    val reportLogs by vm.studentAttendanceReport.collectAsState()
    val filterFrom by vm.attendanceFilterFrom.collectAsState()
    val filterTo by vm.attendanceFilterTo.collectAsState()
    val context = LocalContext.current

    val calendar = Calendar.getInstance()
    
    val fromDatePicker = DatePickerDialog(
        context,
        { _, y, m, d ->
            val dateStr = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d)
            vm.attendanceFilterFrom.value = dateStr
            vm.loadStudentEligibility(dateStr, vm.attendanceFilterTo.value)
            vm.loadStudentAttendanceReport(dateStr, vm.attendanceFilterTo.value)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val toDatePicker = DatePickerDialog(
        context,
        { _, y, m, d ->
            val dateStr = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d)
            vm.attendanceFilterTo.value = dateStr
            vm.loadStudentEligibility(vm.attendanceFilterFrom.value, dateStr)
            vm.loadStudentAttendanceReport(vm.attendanceFilterFrom.value, dateStr)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )
    
    LaunchedEffect(Unit) {
        vm.loadStudentEligibility(vm.attendanceFilterFrom.value, vm.attendanceFilterTo.value)
        vm.loadStudentAttendanceReport(vm.attendanceFilterFrom.value, vm.attendanceFilterTo.value)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(DesignSystem.Padding)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Attendance Ledger", style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold))

        // Date range query filters card
        GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Date-Range Query Filter", color = DesignSystem.Cyan, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // From Date Selector
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(DesignSystem.CardBg)
                            .border(androidx.compose.foundation.BorderStroke(1.dp, DesignSystem.Border))
                            .clickable { fromDatePicker.show() }
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarToday, null, tint = DesignSystem.Cyan, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text("From Date", color = DesignSystem.TextMuted, fontSize = 9.sp)
                                Text(filterFrom ?: "Select", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // To Date Selector
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(DesignSystem.CardBg)
                            .border(androidx.compose.foundation.BorderStroke(1.dp, DesignSystem.Border))
                            .clickable { toDatePicker.show() }
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarToday, null, tint = DesignSystem.Cyan, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text("To Date", color = DesignSystem.TextMuted, fontSize = 9.sp)
                                Text(filterTo ?: "Select", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                if (filterFrom != null || filterTo != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Reset Filters",
                        color = DesignSystem.Danger,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable {
                                vm.attendanceFilterFrom.value = null
                                vm.attendanceFilterTo.value = null
                                vm.loadStudentEligibility(null, null)
                                vm.loadStudentAttendanceReport(null, null)
                            }
                            .align(Alignment.End)
                            .padding(4.dp)
                    )
                }
            }
        }

        // Heatmap Matrix (driven by real attendance data)
        GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Activity Heatmap (Last 28 Days)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(Modifier.height(16.dp))

                // Build a 28-day attendance density map from report logs
                val dayMap = remember(reportLogs) {
                    val map = mutableMapOf<String, Int>()
                    reportLogs.forEach { log ->
                        val dayKey = log.start_time.substringBefore("T")
                        val isPresent = log.status.lowercase() in listOf("present", "late", "od_present")
                        if (isPresent) map[dayKey] = (map[dayKey] ?: 0) + 1
                    }
                    map
                }
                val maxSessions = (dayMap.values.maxOrNull() ?: 1).coerceAtLeast(1)

                repeat(4) { week ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        repeat(7) { day ->
                            val idx = week * 7 + day
                            val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -(27 - idx)) }
                            val key = String.format(Locale.US, "%04d-%02d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))
                            val count = dayMap[key] ?: 0
                            val alpha = if (count == 0) 0.08f else (0.3f + 0.7f * count.toFloat() / maxSessions)
                            Box(Modifier.size(32.dp).clip(RoundedCornerShape(6.dp)).background(DesignSystem.Success.copy(alpha = alpha)))
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                }
            }
        }

        Text("Subject-wise Attendance Breakdown", color = Color.White, fontWeight = FontWeight.Bold)

        if (eligibility?.subjects.isNullOrEmpty()) {
            EmptyStatePlaceholder(msg = "Loading syllabus and registration records...")
        } else {
            eligibility?.subjects?.forEach { sub ->
                GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(sub.subject_name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Attended ${sub.attended} of ${sub.total} hours", color = DesignSystem.TextSecondary, fontSize = 11.sp)
                            }
                            
                            val (badgeColor, text) = when (sub.eligibility.lowercase()) {
                                "eligible" -> DesignSystem.Success to "ELIGIBLE"
                                "conditional" -> DesignSystem.Warning to "CONDITIONAL"
                                else -> DesignSystem.Danger to "BARRED"
                            }
                            
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(badgeColor.copy(alpha = 0.1f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(text, color = badgeColor, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Custom Linear Progress bar
                        val pct = sub.percentage.toFloat()
                        val color = when {
                            pct >= 75f -> DesignSystem.Success
                            pct >= 65f -> DesignSystem.Warning
                            else -> DesignSystem.Danger
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            LinearProgressIndicator(
                                progress = { pct / 100f },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = color,
                                trackColor = DesignSystem.Border
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = String.format(Locale.US, "%.1f%%", pct),
                                color = color,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        Text("Chronological Attendance Logs", color = Color.White, fontWeight = FontWeight.Bold)

        if (reportLogs.isEmpty()) {
            EmptyStatePlaceholder(msg = "No attendance logs found for this date range.")
        } else {
            reportLogs.forEach { log ->
                val isPresent = log.status.lowercase() in listOf("present", "late", "od_present")
                GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(if (isPresent) DesignSystem.Success.copy(alpha = 0.1f) else DesignSystem.Danger.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isPresent) Icons.Default.Check else Icons.Default.Close,
                                contentDescription = null,
                                tint = if (isPresent) DesignSystem.Success else DesignSystem.Danger,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(log.subject_name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = "Time: " + log.start_time.substringBefore("T") + " " + log.start_time.substringAfter("T").take(5),
                                color = DesignSystem.TextMuted,
                                fontSize = 10.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(DesignSystem.CardBg)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = log.marked_via.uppercase(),
                                color = DesignSystem.Cyan,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }
    }
}
