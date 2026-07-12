package com.vfstr.smartclass.ui.screens.studentportal

import androidx.compose.animation.core.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vfstr.smartclass.ui.MainViewModel
import com.vfstr.smartclass.ui.components.AnimatedBarChart
import com.vfstr.smartclass.ui.components.GlassmorphicCard
import com.vfstr.smartclass.ui.components.RadialGauge
import com.vfstr.smartclass.ui.theme.DesignSystem
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
    var showNotificationsDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (com.vfstr.smartclass.utils.PermissionUtils.hasBleScanPermissions(context)) {
            vm.startBleScanner()
        }
        vm.loadStudentEligibility()
        vm.loadStudentBacklogs()
        vm.loadSemesterResults()
        vm.loadStudentFees()
        vm.loadStudentNotifications()
    }

    if (showNotificationsDialog) {
        NotificationsDialog(vm) { showNotificationsDialog = false }
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { showNotificationsDialog = true },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        if (isScanning) {
                            Spacer(Modifier.width(8.dp))
                            Column(horizontalAlignment = Alignment.End) {
                                ScanningIndicator()
                                Text(
                                    text = "Last: ${java.text.SimpleDateFormat("HH:mm", Locale.US).format(java.util.Date())}",
                                    color = DesignSystem.TextMuted,
                                    fontSize = 8.sp
                                )
                            }
                        }
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
        val backlogsSummary by vm.studentBacklogs.collectAsState()
        val activeCount = backlogsSummary?.active_backlogs?.size ?: 0

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GlassmorphicCard(
                modifier = Modifier
                    .weight(1f)
                    .height(90.dp)
                    .clickable { onNavigateToResults() }
            ) {
                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Current CGPA", color = DesignSystem.TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = String.format(Locale.getDefault(), "%.2f", cgpa),
                        style = MaterialTheme.typography.titleLarge.copy(color = DesignSystem.Violet, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    )
                }
            }
            GlassmorphicCard(modifier = Modifier.weight(1f).height(90.dp)) {
                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Attendance", color = DesignSystem.TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
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
                        style = MaterialTheme.typography.titleLarge.copy(color = attendanceColor, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    )
                }
            }
            var showBacklogDialog by remember { mutableStateOf(false) }
            if (showBacklogDialog) {
                BacklogsDialog(vm) { showBacklogDialog = false }
            }
            GlassmorphicCard(
                modifier = Modifier
                    .weight(1f)
                    .height(90.dp)
                    .clickable { showBacklogDialog = true }
            ) {
                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Backlogs", color = DesignSystem.TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = activeCount.toString(),
                        style = MaterialTheme.typography.titleLarge.copy(color = if (activeCount > 0) DesignSystem.Danger else DesignSystem.Success, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
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

        // GPA Progression Trend (from actual semester results)
        Text("GPA Progression Trend", color = Color.White, fontWeight = FontWeight.Bold)
        val semResults by vm.semesterResults.collectAsState()
        val gpaData = if (semResults.isNotEmpty()) {
            semResults.sortedBy { it.semester }.map { it.sgpa }
        } else {
            listOf(8.2f, 8.5f, 8.4f)
        }
        val gpaColors = gpaData.mapIndexed { idx, _ ->
            when (idx % 3) {
                0 -> DesignSystem.Cyan
                1 -> DesignSystem.Violet
                else -> DesignSystem.Success
            }
        }
        AnimatedBarChart(
            data = gpaData,
            colors = gpaColors,
            modifier = Modifier.height(140.dp)
        )
        
        HorizontalDivider(color = DesignSystem.Border)
        
        // Quick Action Shortcuts
        var showHallTicketDialog by remember { mutableStateOf(false) }
        var showFeeDialog by remember { mutableStateOf(false) }
        
        if (showHallTicketDialog) {
            HallTicketDialog(vm) { showHallTicketDialog = false }
        }
        if (showFeeDialog) {
            FeePaymentDialog(vm) { showFeeDialog = false }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                    Text("Request OD/Leave", color = Color.White, fontSize = 11.sp)
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
                    Text("MOOC Certs", color = Color.White, fontSize = 11.sp)
                }
            }
            
            val isEligibleForExam = eligibility?.let { it.overall_percentage >= 65.0 } ?: true
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { showHallTicketDialog = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = DesignSystem.CardBg),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isEligibleForExam) DesignSystem.Border else DesignSystem.Danger.copy(alpha = 0.5f)),
                    enabled = isEligibleForExam
                ) {
                    Icon(Icons.Default.ConfirmationNumber, contentDescription = null, tint = if (isEligibleForExam) DesignSystem.Success else DesignSystem.Danger)
                    Spacer(Modifier.width(8.dp))
                    Text(if (isEligibleForExam) "Hall Ticket" else "Ticket Barred", color = if (isEligibleForExam) Color.White else DesignSystem.Danger, fontSize = 11.sp)
                }
                Button(
                    onClick = { showFeeDialog = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = DesignSystem.CardBg),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DesignSystem.Border)
                ) {
                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = DesignSystem.Cyan)
                    Spacer(Modifier.width(8.dp))
                    Text("Fees & Ledger", color = Color.White, fontSize = 11.sp)
                }
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

        HorizontalDivider(color = DesignSystem.Border)

        // Announcements Board
        Text("Announcements & Circulars Board", color = Color.White, fontWeight = FontWeight.Bold)
        val circulars by vm.studentCirculars.collectAsState()
        var selectedCircularCategory by remember { mutableStateOf("All") }
        
        LaunchedEffect(Unit) {
            vm.loadStudentCirculars()
        }

        // Category filter chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("All", "Exams", "Placements", "General").forEach { cat ->
                val isSelected = selectedCircularCategory == cat
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) DesignSystem.Cyan.copy(alpha = 0.15f) else Color.Transparent)
                        .border(androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) DesignSystem.Cyan else DesignSystem.Border), RoundedCornerShape(8.dp))
                        .clickable { selectedCircularCategory = cat }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(cat, color = if (isSelected) DesignSystem.Cyan else Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        val filteredCirculars = if (selectedCircularCategory == "All") circulars
            else circulars.filter { it.category.equals(selectedCircularCategory, ignoreCase = true) }

        if (filteredCirculars.isEmpty()) {
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.Center) {
                Text("No announcements in this category", color = DesignSystem.TextMuted, fontSize = 12.sp)
            }
        } else {
            filteredCirculars.forEach { circ ->
                var isExpanded by remember { mutableStateOf(false) }
                GlassmorphicCard(modifier = Modifier.fillMaxWidth().clickable { isExpanded = !isExpanded }) {
                    Column(Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val badgeColor = when (circ.category.lowercase()) {
                                "exams" -> DesignSystem.Danger
                                "placements" -> DesignSystem.Success
                                else -> DesignSystem.Cyan
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(badgeColor.copy(alpha = 0.1f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(circ.category.uppercase(), color = badgeColor, fontSize = 8.sp, fontWeight = FontWeight.Black)
                            }
                            Text(circ.publish_date, color = DesignSystem.TextMuted, fontSize = 10.sp)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(circ.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            circ.description,
                            color = DesignSystem.TextSecondary,
                            fontSize = 11.sp,
                            maxLines = if (isExpanded) Int.MAX_VALUE else 2
                        )
                        if (!isExpanded && circ.description.length > 100) {
                            Text("Read more", color = DesignSystem.Cyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
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
