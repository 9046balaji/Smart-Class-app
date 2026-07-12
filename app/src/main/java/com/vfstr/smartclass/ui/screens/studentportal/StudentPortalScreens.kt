package com.vfstr.smartclass.ui.screens.studentportal

import android.app.DatePickerDialog
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import com.vfstr.smartclass.data.remote.api.MOOCEnrollmentDto
import com.vfstr.smartclass.domain.models.Student
import com.vfstr.smartclass.ui.MainViewModel
import com.vfstr.smartclass.ui.components.AnimatedBarChart
import com.vfstr.smartclass.ui.components.GlassmorphicCard
import com.vfstr.smartclass.ui.components.RadialGauge
import com.vfstr.smartclass.ui.screens.EmptyStatePlaceholder
import com.vfstr.smartclass.ui.theme.DesignSystem
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

@Composable
fun ScreenStudentOverview(
    vm: MainViewModel,
    onNavigateToMOOCs: () -> Unit,
    onNavigateToLeave: () -> Unit,
    onNavigateToResults: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val profile by vm.studentProfile.collectAsState()
    val cgpa by vm.cgpaAnimated.collectAsState()
    val eligibility by vm.studentEligibility.collectAsState()
    val isScanning by vm.isScanningForBeacon.collectAsState()

    LaunchedEffect(Unit) {
        if (com.vfstr.smartclass.utils.PermissionUtils.hasBleScanPermissions(context)) {
            vm.startBleScanner()
        }
        vm.loadStudentEligibility()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(DesignSystem.Padding)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Welcome Card with Gradient
        GlassmorphicCard(
            modifier = Modifier.fillMaxWidth().height(140.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize().background(
                Brush.linearGradient(listOf(DesignSystem.Violet.copy(alpha = 0.2f), DesignSystem.Cyan.copy(alpha = 0.1f)))
            ))
            Column(modifier = Modifier.padding(DesignSystem.PaddingLarge)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("REGULATED STUDENT PORTAL", color = DesignSystem.Cyan, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                    if (isScanning) {
                        ScanningIndicator()
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Good Day, ${profile?.name ?: "Student"}",
                    style = MaterialTheme.typography.headlineSmall.copy(color = Color.White, fontWeight = FontWeight.Bold)
                )
                Text("Roll Number: ${profile?.rollNo ?: "N/A"}", color = DesignSystem.TextSecondary, fontSize = 12.sp)
            }
        }

        // Snapshot Stats
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            GlassmorphicCard(
                modifier = Modifier
                    .weight(1f)
                    .height(100.dp)
                    .clickable { onNavigateToResults() }
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Current CGPA", color = DesignSystem.TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = String.format(Locale.getDefault(), "%.2f", cgpa),
                        style = MaterialTheme.typography.headlineMedium.copy(color = DesignSystem.Violet, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    )
                }
            }
            GlassmorphicCard(modifier = Modifier.weight(1f).height(100.dp)) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Overall Attendance", color = DesignSystem.TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    val attendanceText = eligibility?.let { String.format(Locale.getDefault(), "%.1f%%", it.overall_percentage) } ?: "N/A"
                    val attendanceColor = eligibility?.let {
                        when {
                            it.overall_percentage >= 75.0 -> DesignSystem.Success
                            it.overall_percentage >= 65.0 -> DesignSystem.Warning
                            else -> DesignSystem.Danger
                        }
                    } ?: DesignSystem.Success
                    Text(
                        text = attendanceText,
                        style = MaterialTheme.typography.headlineMedium.copy(color = attendanceColor, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    )
                }
            }
        }

        // Concentric / Inline Subject Attendance snapshot
        Text("Recent Attendance Snapshot", color = Color.White, fontWeight = FontWeight.Bold)
        if (eligibility?.subjects.isNullOrEmpty()) {
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.Center) {
                Text("No attendance registered yet", color = DesignSystem.TextMuted, fontSize = 12.sp)
            }
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp), 
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val itemsToShow = eligibility?.subjects?.take(3) ?: emptyList()
                itemsToShow.forEachIndexed { index, sub ->
                    val color = when (index % 3) {
                        0 -> DesignSystem.Cyan
                        1 -> DesignSystem.Violet
                        else -> DesignSystem.Success
                    }
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        RadialGauge(
                            percentage = sub.percentage.toFloat(),
                            statusText = sub.subject_name.take(6).uppercase(),
                            strokeColor = color,
                            size = 80.dp
                        )
                    }
                }
            }
        }

        // GPA Progression Trend (Incorporated from old Performance Screen)
        Text("GPA Progression Trend", color = Color.White, fontWeight = FontWeight.Bold)
        val gpaData = listOf(cgpa.toFloat() * 0.95f, cgpa.toFloat() * 0.98f, cgpa.toFloat())
        AnimatedBarChart(
            data = if (cgpa == 0.0) listOf(8.2f, 8.5f, 8.4f) else gpaData,
            colors = listOf(DesignSystem.Cyan, DesignSystem.Violet, DesignSystem.Success),
            modifier = Modifier.height(140.dp)
        )
        
        HorizontalDivider(color = DesignSystem.Border)
        
        // Quick Action Shortcuts
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onNavigateToLeave,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = DesignSystem.CardBg),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DesignSystem.Border)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = DesignSystem.Cyan)
                Spacer(Modifier.width(8.dp))
                Text("Request OD/Leave", color = Color.White, fontSize = 12.sp)
            }
            Button(
                onClick = onNavigateToMOOCs,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = DesignSystem.CardBg),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DesignSystem.Border)
            ) {
                Icon(Icons.Default.Verified, contentDescription = null, tint = DesignSystem.Violet)
                Spacer(Modifier.width(8.dp))
                Text("MOOC Certs", color = Color.White, fontSize = 12.sp)
            }
        }

        // Recent Activity Feed
        Text("Recent activity feed", color = Color.White, fontWeight = FontWeight.Bold)
        val history = profile?.attendanceHistory?.take(3) ?: emptyList()
        if (history.isEmpty()) {
            ActivityItem(subject = "BLE check-in synced", time = "Scanner active in background", isPresent = true)
        } else {
            history.forEach { event ->
                ActivityItem(
                    subject = "Attendance marked: ${ (event.roomName ?: event.room).ifEmpty { "Class" } }",
                    time = event.timestamp.substringBefore("T") + " " + event.timestamp.substringAfter("T").take(5),
                    isPresent = event.status.name.uppercase() == "PRESENT"
                )
            }
        }
    }
}

@Composable
fun ScanningIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "scan")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(DesignSystem.Success.copy(alpha = alpha))
        )
        Spacer(Modifier.width(6.dp))
        Text(
            "SYNC ACTIVE",
            color = DesignSystem.Success.copy(alpha = alpha),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ActivityItem(subject: String, time: String, isPresent: Boolean) {
    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
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
            Column {
                Text(subject, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(time, color = DesignSystem.TextMuted, fontSize = 10.sp)
            }
        }
    }
}

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

        // Heatmap Matrix
        GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Activity Heatmap", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(Modifier.height(16.dp))
                repeat(4) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        repeat(7) { 
                            val alpha = (3..10).random() / 10f
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

@Composable
fun ScreenStudentOD(
    vm: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val odRequests by vm.studentODRequests.collectAsState()
    val isSubmitting by vm.isSubmittingOD.collectAsState()
    val submitSuccess by vm.odSubmitSuccess.collectAsState()

    var eventName by remember { mutableStateOf("") }
    var eventDate by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    var durationDays by remember { mutableStateOf(1) }

    LaunchedEffect(Unit) {
        vm.loadStudentODRequests()
    }

    LaunchedEffect(submitSuccess) {
        if (submitSuccess == true) {
            eventName = ""
            eventDate = ""
            reason = ""
            durationDays = 1
        }
    }

    // Calendar setup for DatePickerDialog
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, y, m, d ->
            eventDate = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(DesignSystem.Padding)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text("On-Duty (OD) Submissions", style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold))
        
        GlassmorphicCard {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Register New Request", color = DesignSystem.Cyan, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                
                OutlinedTextField(
                    value = eventName,
                    onValueChange = { eventName = it },
                    label = { Text("Event Name / Description") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DesignSystem.Cyan,
                        unfocusedBorderColor = DesignSystem.Border
                    )
                )

                OutlinedTextField(
                    value = eventDate,
                    onValueChange = {},
                    label = { Text("Event Date (Tap to select)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { datePickerDialog.show() },
                    enabled = false,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = DesignSystem.Border,
                        disabledTextColor = Color.White,
                        disabledLabelColor = DesignSystem.TextSecondary
                    ),
                    trailingIcon = {
                        IconButton(onClick = { datePickerDialog.show() }) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = DesignSystem.Cyan)
                        }
                    }
                )
                
                Column {
                    Text(
                        text = "Duration: $durationDays Day(s)",
                        color = DesignSystem.TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Slider(
                        value = durationDays.toFloat(),
                        onValueChange = { durationDays = it.toInt() },
                        valueRange = 1f..7f,
                        steps = 5,
                        colors = SliderDefaults.colors(
                            thumbColor = DesignSystem.Cyan,
                            activeTrackColor = DesignSystem.Cyan
                        )
                    )
                }

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason commentary") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DesignSystem.Cyan,
                        unfocusedBorderColor = DesignSystem.Border
                    )
                )

                if (submitSuccess == false) {
                    Text("Failed to submit request. Please try again.", color = DesignSystem.Danger, fontSize = 12.sp)
                } else if (submitSuccess == true) {
                    Text("Application submitted successfully!", color = DesignSystem.Success, fontSize = 12.sp)
                }

                Button(
                    onClick = { 
                        if (eventName.isNotEmpty() && eventDate.isNotEmpty() && reason.isNotEmpty()) {
                            vm.submitStudentODRequest("College Event", eventName, eventDate, reason)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DesignSystem.Cyan, contentColor = Color.Black),
                    shape = RoundedCornerShape(DesignSystem.CornerRadius),
                    enabled = !isSubmitting && eventName.isNotEmpty() && eventDate.isNotEmpty()
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black)
                    } else {
                        Text("Submit Application", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Text("OD Request History", color = Color.White, fontWeight = FontWeight.Bold)

        if (odRequests.isEmpty()) {
            EmptyStatePlaceholder(msg = "No submitted OD applications found.")
        } else {
            odRequests.forEach { req ->
                val badgeColor = when (req.status.lowercase()) {
                    "approved" -> DesignSystem.Success
                    "rejected" -> DesignSystem.Danger
                    else -> DesignSystem.Warning
                }
                GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(req.event_name ?: "OD Request", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Applied: ${(req.requested_at ?: req.applied_on ?: "").substringBefore("T")}", color = DesignSystem.TextSecondary, fontSize = 11.sp)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(badgeColor.copy(alpha = 0.1f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = req.status.uppercase(),
                                color = badgeColor,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScreenStudentCertificates(
    vm: MainViewModel,
    modifier: Modifier = Modifier,
    moocVm: MoocViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val context = LocalContext.current
    val enrollments by moocVm.enrollments.collectAsState()
    val isLoading by moocVm.isLoading.collectAsState()
    val isEnrolling by moocVm.isEnrolling.collectAsState()

    var showEnrollDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        moocVm.loadMOOCs()
    }

    Column(modifier = modifier.fillMaxSize().padding(DesignSystem.Padding)) {
        Text("Academic Certificates", style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold))
        Spacer(Modifier.height(8.dp))
        Text("R22 Regulation requires 9 elective credits via MOOCs", color = DesignSystem.TextSecondary, fontSize = 12.sp)
        
        Spacer(Modifier.height(16.dp))

        // Progress bar for R22 requirement
        val completedCredits = enrollments.filter { it.completion_status?.lowercase() == "completed" || it.status?.lowercase() == "completed" }.sumOf { it.credits ?: 0 }
        GlassmorphicCard(modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("MOOC Degree Progress", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LinearProgressIndicator(
                        progress = { completedCredits.toFloat() / 9f },
                        modifier = Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = DesignSystem.Violet,
                        trackColor = DesignSystem.Border
                    )
                    Spacer(Modifier.width(16.dp))
                    Text("$completedCredits / 9 Credits", color = DesignSystem.Violet, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                }
            }
        }

        if (isLoading) {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
                items(3) {
                    com.vfstr.smartclass.ui.components.ShimmerSkeleton(modifier = Modifier.fillMaxWidth().height(80.dp).clip(RoundedCornerShape(12.dp)))
                }
            }
        } else if (enrollments.isEmpty()) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                EmptyStatePlaceholder(msg = "Upload and manage your external MOOC certificates.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(enrollments) { item ->
                    GlassmorphicCard {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(DesignSystem.Violet.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.School, null, tint = DesignSystem.Violet, modifier = Modifier.size(20.dp))
                            }
                            Spacer(Modifier.width(16.dp))
                            Column(Modifier.weight(1f)) {
                                Text(item.course_name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("${item.platform ?: "MOOC"} • ${item.credits ?: 0} Credits", color = DesignSystem.TextSecondary, fontSize = 11.sp)
                            }
                            val statusStr = item.completion_status ?: item.status ?: "enrolled"
                            val statusColor = if (statusStr.equals("completed", true)) DesignSystem.Success else DesignSystem.Cyan
                            Text(statusStr.uppercase(), color = statusColor, fontWeight = FontWeight.ExtraBold, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = { showEnrollDialog = true },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DesignSystem.Cyan, contentColor = Color.Black),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.CloudUpload, null)
            Spacer(Modifier.width(8.dp))
            Text("Register New MOOC", fontWeight = FontWeight.ExtraBold)
        }
    }

    if (showEnrollDialog) {
        var courseName by remember { mutableStateOf("") }
        var platform by remember { mutableStateOf("Coursera") }
        var courseCode by remember { mutableStateOf("") }
        var credits by remember { mutableStateOf(3) }
        var enrollDate by remember { mutableStateOf("") }
        var semester by remember { mutableStateOf(5) }
        var academicYear by remember { mutableStateOf("2025-2026") }

        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            context,
            { _, y, m, d -> enrollDate = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d) },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        AlertDialog(
            onDismissRequest = { showEnrollDialog = false },
            title = { Text("Register MOOC Elective") },
            containerColor = DesignSystem.Surface,
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = courseName,
                        onValueChange = { courseName = it },
                        label = { Text("Course Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // Platform dropdown options
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Swayam", "NPTEL", "Coursera", "edX").forEach { p ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (platform == p) DesignSystem.Cyan.copy(alpha = 0.15f) else Color.Transparent)
                                    .border(1.dp, if (platform == p) DesignSystem.Cyan else DesignSystem.Border, RoundedCornerShape(8.dp))
                                    .clickable { platform = p }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(p, color = if (platform == p) DesignSystem.Cyan else Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = courseCode,
                        onValueChange = { courseCode = it },
                        label = { Text("Course Code (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(
                            value = credits.toString(),
                            onValueChange = { credits = it.toIntOrNull() ?: 1 },
                            label = { Text("Credits") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = semester.toString(),
                            onValueChange = { semester = it.toIntOrNull() ?: 5 },
                            label = { Text("Semester") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = enrollDate,
                        onValueChange = {},
                        label = { Text("Enrollment Date") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { datePicker.show() },
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = Color.White,
                            disabledBorderColor = DesignSystem.Border,
                            disabledLabelColor = DesignSystem.TextSecondary
                        ),
                        trailingIcon = {
                            IconButton(onClick = { datePicker.show() }) {
                                Icon(Icons.Default.CalendarToday, null, tint = DesignSystem.Cyan)
                            }
                        }
                    )

                    OutlinedTextField(
                        value = academicYear,
                        onValueChange = { academicYear = it },
                        label = { Text("Academic Year") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (courseName.isNotEmpty() && enrollDate.isNotEmpty()) {
                            moocVm.enrollMOOC(
                                courseName = courseName,
                                platform = platform,
                                courseCode = courseCode.ifEmpty { null },
                                credits = credits,
                                enrollmentDate = enrollDate,
                                semester = semester,
                                academicYear = academicYear,
                                onSuccess = { showEnrollDialog = false }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DesignSystem.Cyan, contentColor = Color.Black),
                    enabled = !isEnrolling && courseName.isNotEmpty() && enrollDate.isNotEmpty()
                ) {
                    if (isEnrolling) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.Black)
                    } else {
                        Text("Register", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showEnrollDialog = false }) {
                    Text("Cancel", color = DesignSystem.TextMuted)
                }
            }
        )
    }
}

@dagger.hilt.android.lifecycle.HiltViewModel
class MoocViewModel @javax.inject.Inject constructor(
    private val repository: com.vfstr.smartclass.data.repositories.AppRepository
) : androidx.lifecycle.ViewModel() {
    private val _enrollments = kotlinx.coroutines.flow.MutableStateFlow<List<com.vfstr.smartclass.data.remote.api.MOOCEnrollmentDto>>(emptyList())
    val enrollments: kotlinx.coroutines.flow.StateFlow<List<com.vfstr.smartclass.data.remote.api.MOOCEnrollmentDto>> = _enrollments
    
    private val _isLoading = kotlinx.coroutines.flow.MutableStateFlow(true)
    val isLoading: kotlinx.coroutines.flow.StateFlow<Boolean> = _isLoading
    
    private val _isEnrolling = kotlinx.coroutines.flow.MutableStateFlow(false)
    val isEnrolling: kotlinx.coroutines.flow.StateFlow<Boolean> = _isEnrolling
    
    fun loadMOOCs() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _enrollments.value = repository.getStudentMOOCs()
            } catch (e: Exception) {
                // Return empty on failure for now
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun enrollMOOC(
        courseName: String,
        platform: String,
        courseCode: String?,
        credits: Int?,
        enrollmentDate: String?,
        semester: Int?,
        academicYear: String?,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isEnrolling.value = true
            try {
                val payload = com.vfstr.smartclass.data.remote.api.StudentMOOCEnrollPayload(
                    course_name = courseName,
                    platform = platform,
                    course_code = courseCode,
                    credits = credits,
                    enrollment_date = enrollmentDate,
                    semester = semester,
                    academic_year = academicYear
                )
                val result = repository.enrollStudentMOOC(payload)
                if (result != null) {
                    loadMOOCs()
                    onSuccess()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isEnrolling.value = false
            }
        }
    }
}

@Composable
fun ScreenStudentMarks(
    vm: MainViewModel,
    modifier: Modifier = Modifier
) {
    val marks by vm.studentMarks.collectAsState()
    val selectedSemester by vm.selectedMarksSemester.collectAsState()

    LaunchedEffect(selectedSemester) {
        vm.loadStudentMarks(selectedSemester)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(DesignSystem.Padding)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Internal Marks", style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold))

        // Semester selector tabs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("SEM-5", "SEM-4", "SEM-3").forEach { sem ->
                val isSelected = selectedSemester == sem
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) DesignSystem.Cyan.copy(alpha = 0.15f) else Color.Transparent)
                        .border(androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) DesignSystem.Cyan else DesignSystem.Border), RoundedCornerShape(8.dp))
                        .clickable { vm.selectedMarksSemester.value = sem }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(sem, color = if (isSelected) DesignSystem.Cyan else Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (marks.isEmpty()) {
            EmptyStatePlaceholder(msg = "No marks registered for this semester.")
        } else {
            marks.forEach { item ->
                var expanded by remember { mutableStateOf(false) }
                
                GlassmorphicCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = !expanded }
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.subject_name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(item.subject_code, color = DesignSystem.TextSecondary, fontSize = 11.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${item.total_obtained ?: 0.0f} / ${item.total_max}",
                                    color = DesignSystem.Cyan,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text("Click to view details", color = DesignSystem.TextMuted, fontSize = 9.sp)
                            }
                        }

                        if (expanded) {
                            Spacer(Modifier.height(16.dp))
                            HorizontalDivider(color = DesignSystem.Border)
                            Spacer(Modifier.height(12.dp))

                            ComponentMarksBar("Mid-1 Exams", item.mid1 ?: 0f, 10f, DesignSystem.Cyan)
                            Spacer(Modifier.height(8.dp))
                            ComponentMarksBar("Mid-2 Exams", item.mid2 ?: 0f, 10f, DesignSystem.Violet)
                            Spacer(Modifier.height(8.dp))
                            ComponentMarksBar("Assignments", item.assignment ?: 0f, 5f, DesignSystem.Success)
                            Spacer(Modifier.height(8.dp))
                            ComponentMarksBar("Attendance score", item.attendance ?: 0f, 5f, DesignSystem.Warning)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ComponentMarksBar(name: String, obtained: Float, max: Float, color: Color) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(name, color = DesignSystem.TextSecondary, fontSize = 11.sp)
            Text("$obtained / $max", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { obtained / max },
            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
            color = color,
            trackColor = DesignSystem.Border
        )
    }
}

@Composable
fun ScreenStudentResults(
    vm: MainViewModel,
    modifier: Modifier = Modifier
) {
    val results by vm.semesterResults.collectAsState()

    LaunchedEffect(Unit) {
        vm.loadSemesterResults()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(DesignSystem.Padding)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Academic Grade Sheets", style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold))

        if (results.isEmpty()) {
            EmptyStatePlaceholder(msg = "Loading academic records and grade templates...")
        } else {
            results.forEach { res ->
                var expanded by remember { mutableStateOf(false) }

                GlassmorphicCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = !expanded }
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(res.semester, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("GPA: ${res.sgpa} • Cumulative: ${res.cgpa}", color = DesignSystem.Cyan, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Text(
                                text = if (expanded) "COLLAPSE" else "EXPAND",
                                color = DesignSystem.TextSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (expanded) {
                            Spacer(Modifier.height(12.dp))
                            HorizontalDivider(color = DesignSystem.Border)
                            Spacer(Modifier.height(8.dp))

                            res.subjects.forEach { sub ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(sub.subject_name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text("${sub.subject_code} • Credits: ${sub.credits}", color = DesignSystem.TextMuted, fontSize = 10.sp)
                                    }
                                    
                                    val badgeColor = when (sub.grade.uppercase()) {
                                        "O", "A+", "A" -> DesignSystem.Success
                                        "B+", "B" -> DesignSystem.Cyan
                                        "C" -> DesignSystem.Warning
                                        else -> DesignSystem.Danger
                                    }
                                    
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(badgeColor.copy(alpha = 0.1f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(sub.grade, color = badgeColor, fontSize = 10.sp, fontWeight = FontWeight.Black)
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
