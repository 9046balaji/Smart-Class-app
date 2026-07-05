package com.vfstr.smartclass.ui.screens.enrollment

import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vfstr.smartclass.domain.models.*
import com.vfstr.smartclass.ui.components.GlassmorphicCard
import com.vfstr.smartclass.ui.components.MeshBackground
import com.vfstr.smartclass.ui.theme.DesignSystem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenEnrollmentStatus(
    vm: EnrollmentViewModel,
    studentId: String,
    onNavigateToEdit: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val requestState by vm.requestState.collectAsState()
    val isLoading by vm.isLoading.collectAsState()
    val notifications by vm.notifications.collectAsState()

    LaunchedEffect(studentId) {
        vm.loadRequest(studentId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Enrollment Status", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = DesignSystem.Background
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            MeshBackground()

            if (isLoading && requestState == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = DesignSystem.Cyan)
                }
            } else if (requestState != null) {
                val req = requestState!!
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(DesignSystem.Padding),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { StatusHeroCard(req) }
                    item { ApplicationSummaryCard(req) }
                    
                    if (req.status == EnrollmentStatus.REJECTED || req.status == EnrollmentStatus.NEEDS_REVISION) {
                        item { AdminCommentsCard(req) }
                    }

                    if (notifications.isNotEmpty()) {
                        item { NotificationsSection(notifications) { vm.markNotificationsRead() } }
                    }
                }
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No enrollment request found.", color = DesignSystem.TextSecondary)
                }
            }

            // F9: Action buttons fixed in bottom area
            requestState?.let { req ->
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(DesignSystem.Padding)
                ) {
                    ActionButtons(req, onNavigateToEdit, onNavigateToProfile)
                }
            }
        }
    }
}

@Composable
fun StatusHeroCard(req: EnrollmentRequest) {
    val statusColor = when (req.status) {
        EnrollmentStatus.APPROVED -> DesignSystem.Success
        EnrollmentStatus.REJECTED -> DesignSystem.Danger
        EnrollmentStatus.PENDING -> DesignSystem.Warning
        EnrollmentStatus.NEEDS_REVISION -> Color(0xFFFB923C) // Orange
        else -> DesignSystem.TextSecondary
    }

    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        glowColor = statusColor.copy(alpha = 0.1f),
        borderColor = statusColor.copy(alpha = 0.3f)
    ) {
        Column(
            modifier = Modifier.padding(24.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = when (req.status) {
                    EnrollmentStatus.APPROVED -> Icons.Default.CheckCircle
                    EnrollmentStatus.REJECTED -> Icons.Default.Cancel
                    EnrollmentStatus.NEEDS_REVISION -> Icons.Default.EditNote
                    else -> Icons.Default.Schedule
                },
                contentDescription = null,
                tint = statusColor,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = req.status.name.replace("_", " "),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = statusColor)
            )
            Text(
                text = when (req.status) {
                    EnrollmentStatus.PENDING -> "Your application is under review"
                    EnrollmentStatus.APPROVED -> "You're officially enrolled! 🎉"
                    EnrollmentStatus.REJECTED -> "Application was not approved"
                    EnrollmentStatus.NEEDS_REVISION -> "Please update your details"
                    else -> ""
                },
                style = MaterialTheme.typography.bodyMedium,
                color = DesignSystem.TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ApplicationSummaryCard(req: EnrollmentRequest) {
    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Application Summary", fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth()) {
                SummaryItem("Name", req.name, Modifier.weight(1f))
                SummaryItem("Roll No", req.rollNumber, Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth()) {
                SummaryItem("Branch", req.branch, Modifier.weight(1f))
                SummaryItem("Section", "${req.year} - ${req.section}", Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun SummaryItem(label: String, value: String, modifier: Modifier) {
    Column(modifier) {
        Text(label, fontSize = 10.sp, color = DesignSystem.TextSecondary)
        Text(value, fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun AdminCommentsCard(req: EnrollmentRequest) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DesignSystem.Danger.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ErrorOutline, null, tint = DesignSystem.Danger, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("Admin Feedback", fontWeight = FontWeight.Bold, color = DesignSystem.Danger, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(req.reviewComments ?: "No comments provided.", color = DesignSystem.TextPrimary, fontSize = 13.sp)
        }
    }
}

@Composable
fun NotificationsSection(notifs: List<EnrollmentNotification>, onRead: () -> Unit) {
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Notifications", fontWeight = FontWeight.Bold, color = Color.White)
            TextButton(onClick = onRead) { Text("Mark all as read", fontSize = 12.sp, color = DesignSystem.Cyan) }
        }
        notifs.forEach { notif ->
            ListItem(
                headlineContent = { Text(notif.message, fontSize = 13.sp) },
                supportingContent = { Text(notif.timestamp, fontSize = 10.sp) },
                leadingContent = { 
                    Box(Modifier.size(8.dp).clip(CircleShape).background(if (notif.isRead) Color.Transparent else DesignSystem.Cyan)) 
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )
        }
    }
}

@Composable
fun ActionButtons(req: EnrollmentRequest, onEdit: () -> Unit, onProfile: () -> Unit) {
    when (req.status) {
        EnrollmentStatus.APPROVED -> {
            Button(onClick = onProfile, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = DesignSystem.Cyan)) {
                Text("View Student Profile")
            }
        }
        EnrollmentStatus.REJECTED, EnrollmentStatus.NEEDS_REVISION -> {
            Button(onClick = onEdit, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = DesignSystem.Warning)) {
                Text("Update Application")
            }
        }
        else -> {}
    }
}
