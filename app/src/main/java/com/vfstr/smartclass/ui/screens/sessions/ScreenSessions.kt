package com.vfstr.smartclass.ui.screens.sessions

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vfstr.smartclass.domain.models.ClassSession
import com.vfstr.smartclass.domain.models.SessionStatus
import com.vfstr.smartclass.domain.models.Student
import com.vfstr.smartclass.ui.MainViewModel
import com.vfstr.smartclass.ui.components.GlassmorphicCard
import com.vfstr.smartclass.ui.theme.DesignSystem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenSessions(
    vm: MainViewModel,
    modifier: Modifier = Modifier
) {
    val sessions by vm.classSessions.collectAsState()
    val loading by vm.sessionsLoading.collectAsState()
    val isBleActive by vm.isBleRadarActive.collectAsState()

    var showForm by remember { mutableStateOf(false) }
    var showRoster by remember { mutableStateOf<ClassSession?>(null) }

    var subject by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var room by remember { mutableStateOf("") }
    var typ by remember { mutableStateOf("L") }

    LaunchedEffect(Unit) {
        vm.loadSessions()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DesignSystem.Background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showForm = true },
                containerColor = DesignSystem.Violet,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add session")
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize().padding(DesignSystem.Padding)) {
                Text(
                    text = "Class Sessions",
                    style = MaterialTheme.typography.headlineSmall.copy(color = DesignSystem.TextPrimary, fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Manage active and upcoming academic periods",
                    style = MaterialTheme.typography.bodySmall.copy(color = DesignSystem.TextSecondary)
                )

                if (isBleActive) {
                    RadarPingIndicator()
                }

                Spacer(modifier = Modifier.height(DesignSystem.SpacingLarge))

                if (loading && sessions.isEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(DesignSystem.SpacingMedium)) {
                        repeat(3) {
                            com.vfstr.smartclass.ui.components.ShimmerSkeleton(modifier = Modifier.fillMaxWidth().height(160.dp))
                        }
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(sessions) { s ->
                            SessionCardItem(
                                session = s,
                                onEnd = { vm.endSessionDirect(s.id) },
                                onStart = { vm.startSessionDirect(s.id) },
                                onViewRoster = { showRoster = s }
                            )
                        }
                    }
                }
            }

            if (showRoster != null) {
                RosterBottomSheet(
                    session = showRoster!!,
                    vm = vm,
                    onDismiss = { showRoster = null }
                )
            }

            if (showForm) {
                CreateSessionDialog(
                    onDismiss = { showForm = false },
                    onCreate = { s, c, r, t ->
                        vm.createSession(r, "AI3A", s) // Example section ID
                        showForm = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RosterBottomSheet(
    session: ClassSession,
    vm: MainViewModel,
    onDismiss: () -> Unit
) {
    val students by vm.students.collectAsState()
    val events by vm.attendanceEvents.collectAsState()
    
    LaunchedEffect(session.id) {
        vm.loadStudents(dept = session.department, sec = session.section)
        vm.loadAttendanceEvents(dept = session.department, sec = session.section) // Should filter by session_id in real app
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = DesignSystem.Surface,
        contentColor = DesignSystem.TextPrimary
    ) {
        Column(modifier = Modifier.padding(DesignSystem.PaddingLarge).fillMaxHeight(0.8f)) {
            Text(
                text = "Attendance Roster",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "${session.subjectName} • Section ${session.section}",
                color = DesignSystem.TextSecondary,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(20.dp))
            
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(students) { st ->
                    val event = events.find { it.studentId == st.studentId || it.rollNo == st.rollNo }
                    RosterItem(student = st, event = event)
                }
            }
        }
    }
}

@Composable
fun RosterItem(student: Student, event: com.vfstr.smartclass.domain.models.AttendanceEvent?) {
    val isPresent = event != null && event.status == com.vfstr.smartclass.domain.models.AttendanceStatus.Present
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(DesignSystem.CornerRadius))
            .background(DesignSystem.CardBg)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(36.dp).clip(CircleShape).background(DesignSystem.Background),
            contentAlignment = Alignment.Center
        ) {
            Text(student.name.take(1), color = DesignSystem.Cyan, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(student.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
            Text(student.rollNo, style = MaterialTheme.typography.labelSmall.copy(color = DesignSystem.TextSecondary))
        }
        
        if (event != null) {
            Column(horizontalAlignment = Alignment.End) {
                Icon(
                    imageVector = if (isPresent) Icons.Default.CheckCircle else Icons.Default.Cancel,
                    contentDescription = null,
                    tint = if (isPresent) DesignSystem.Success else DesignSystem.Danger,
                    modifier = Modifier.size(20.dp)
                )
                if (event.confidence > 0) {
                    Text("${(event.confidence * 100).toInt()}%", fontSize = 9.sp, color = DesignSystem.TextMuted)
                }
            }
        } else {
            Text("NOT SCANNED", fontSize = 10.sp, color = DesignSystem.TextMuted, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun CreateSessionDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String, String, String) -> Unit
) {
    var subject by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var room by remember { mutableStateOf("") }
    var typ by remember { mutableStateOf("L") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DesignSystem.Surface,
        title = { Text("New Session", color = DesignSystem.TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("Subject Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("Subject Code") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = room, onValueChange = { room = it }, label = { Text("Room") }, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("L", "T", "P").forEach {
                        FilterChip(
                            selected = typ == it,
                            onClick = { typ = it },
                            label = { Text(it) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onCreate(subject, code, room, typ) }) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun SessionCardItem(
    session: ClassSession,
    onEnd: () -> Unit,
    onStart: () -> Unit,
    onViewRoster: () -> Unit
) {
    val isActive = session.status == SessionStatus.Active
    val hasEnded = session.status == SessionStatus.Ended

    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = if (isActive) DesignSystem.Success.copy(alpha = 0.3f) else DesignSystem.Border
    ) {
        Column(modifier = Modifier.padding(DesignSystem.Padding)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusBadge(status = session.status)
                if (isActive) {
                    Text(
                        text = "LIVE",
                        style = MaterialTheme.typography.labelSmall.copy(color = DesignSystem.Danger, fontWeight = FontWeight.Bold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = session.subjectName,
                style = MaterialTheme.typography.titleLarge.copy(color = DesignSystem.TextPrimary, fontWeight = FontWeight.Bold)
            )
            Text(
                text = "${session.subjectCode} • ${session.room}",
                style = MaterialTheme.typography.bodyMedium.copy(color = DesignSystem.TextSecondary)
            )

            Spacer(modifier = Modifier.height(20.dp))

            AttendanceProgress(count = session.attendanceCount, total = session.totalStudents)

            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onViewRoster,
                    colors = ButtonDefaults.buttonColors(containerColor = DesignSystem.Surface),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Roster", color = DesignSystem.TextPrimary)
                }

                if (isActive) {
                    Button(
                        onClick = onEnd,
                        colors = ButtonDefaults.buttonColors(containerColor = DesignSystem.Danger.copy(alpha = 0.2f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("End", color = DesignSystem.Danger)
                    }
                } else if (session.status == SessionStatus.Scheduled) {
                    Button(
                        onClick = onStart,
                        colors = ButtonDefaults.buttonColors(containerColor = DesignSystem.Success),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Start", color = Color.Black)
                    }
                }
            }
        }
    }
}

@Composable
fun RadarPingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Row(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(DesignSystem.Cyan.copy(alpha = 0.1f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .graphicsLayer(scaleX = scale, scaleY = scale, alpha = alpha)
                .clip(CircleShape)
                .background(DesignSystem.Cyan)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = "Radar Ping Active",
            style = MaterialTheme.typography.labelMedium.copy(
                color = DesignSystem.Cyan,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = "Broadcasting...",
            style = MaterialTheme.typography.labelSmall.copy(color = DesignSystem.TextSecondary)
        )
    }
}

@Composable
fun StatusBadge(status: SessionStatus) {
    val color = when (status) {
        SessionStatus.Active -> DesignSystem.Success
        SessionStatus.Ended -> DesignSystem.TextMuted
        SessionStatus.Scheduled -> DesignSystem.Warning
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(6.dp))
        Text(text = status.name.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
fun AttendanceProgress(count: Int, total: Int) {
    val progress = if (total > 0) count.toFloat() / total else 0f
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
            Text("Attendance", style = MaterialTheme.typography.labelSmall.copy(color = DesignSystem.TextSecondary))
            Text("${(progress * 100).toInt()}% ($count/$total)", style = MaterialTheme.typography.bodyMedium.copy(color = DesignSystem.Cyan, fontWeight = FontWeight.Bold, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace))
        }
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
            color = DesignSystem.Cyan,
            trackColor = DesignSystem.Border
        )
    }
}
