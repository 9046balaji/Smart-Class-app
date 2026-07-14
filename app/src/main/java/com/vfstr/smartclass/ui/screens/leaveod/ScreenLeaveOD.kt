package com.vfstr.smartclass.ui.screens.leaveod

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vfstr.smartclass.domain.models.ODRequest
import com.vfstr.smartclass.domain.models.CondonationRequest
import com.vfstr.smartclass.ui.MainViewModel
import com.vfstr.smartclass.ui.components.ApprovalTimeline
import com.vfstr.smartclass.ui.components.GlassmorphicCard
import com.vfstr.smartclass.ui.theme.DesignSystem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenLeaveOD(
    vm: MainViewModel,
    modifier: Modifier = Modifier
) {
    val requests by vm.odRequests.collectAsState()
    val condonations by vm.condonationRequests.collectAsState()
    var selectedRequest by remember { mutableStateOf<ODRequest?>(null) }
    var selectedCondonation by remember { mutableStateOf<CondonationRequest?>(null) }
    var activeTab by rememberSaveable { mutableStateOf("OD Requests") } // OD Requests, Condonation

    LaunchedEffect(Unit) {
        vm.loadODRequests()
        vm.loadCondonationRequests()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DesignSystem.Background
    ) { innerPadding ->
        Row(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            // Left List Column
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(DesignSystem.Padding)
            ) {
                Text(
                    text = "Approvals & Requests",
                    style = MaterialTheme.typography.headlineSmall.copy(color = DesignSystem.TextPrimary, fontWeight = FontWeight.Bold)
                )
                Text(text = "Review and process on-duty and leave applications", color = DesignSystem.TextSecondary, style = MaterialTheme.typography.bodySmall)
                
                Spacer(modifier = Modifier.height(20.dp))

                Row(modifier = Modifier.fillMaxWidth().height(44.dp).clip(RoundedCornerShape(10.dp)).background(DesignSystem.Surface).padding(4.dp)) {
                    listOf("OD Requests", "Condonation").forEach { tab ->
                        val isSel = activeTab == tab
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) DesignSystem.Cyan.copy(alpha = 0.1f) else Color.Transparent)
                                .clickable {
                                    activeTab = tab
                                    selectedRequest = null
                                    selectedCondonation = null
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(tab, color = if (isSel) DesignSystem.Cyan else DesignSystem.TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (activeTab == "OD Requests") {
                        items(requests) { req ->
                            ODRequestItem(
                                request = req,
                                isSelected = selectedRequest?.id == req.id,
                                onClick = { selectedRequest = req }
                            )
                        }
                    } else {
                        items(condonations) { cond ->
                            CondonationRequestItem(
                                request = cond,
                                isSelected = selectedCondonation?.id == cond.id,
                                onClick = { selectedCondonation = cond }
                            )
                        }
                    }
                }
            }

            // Right Detail Column (Responsive Feel)
            Box(
                modifier = Modifier
                    .width(420.dp)
                    .fillMaxHeight()
                    .background(DesignSystem.Surface)
                    .border(1.dp, DesignSystem.Border, RectangleShape)
                    .padding(DesignSystem.PaddingLarge)
            ) {
                if (activeTab == "OD Requests" && selectedRequest != null) {
                    RequestDetailView(selectedRequest!!, vm) { selectedRequest = null }
                } else if (activeTab == "Condonation" && selectedCondonation != null) {
                    CondonationDetailView(selectedCondonation!!, vm) { selectedCondonation = null }
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.TouchApp, null, tint = DesignSystem.TextMuted, modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(16.dp))
                            Text("Select an application from the list to review details.", color = DesignSystem.TextMuted, textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ODRequestItem(request: ODRequest, isSelected: Boolean, onClick: () -> Unit) {
    GlassmorphicCard(
        modifier = Modifier.clickable { onClick() },
        borderColor = if (isSelected) DesignSystem.Cyan else DesignSystem.Border,
        backgroundColor = if (isSelected) DesignSystem.Cyan.copy(alpha = 0.05f) else DesignSystem.CardBg
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(request.eventName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
                StatusChip(request.status)
            }
            Text(text = request.studentName ?: request.rollNo, color = DesignSystem.TextSecondary, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DateRange, null, tint = DesignSystem.TextMuted, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text(request.eventDate, color = DesignSystem.TextMuted, fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun CondonationRequestItem(request: CondonationRequest, isSelected: Boolean, onClick: () -> Unit) {
    GlassmorphicCard(
        modifier = Modifier.clickable { onClick() },
        borderColor = if (isSelected) DesignSystem.Cyan else DesignSystem.Border,
        backgroundColor = if (isSelected) DesignSystem.Cyan.copy(alpha = 0.05f) else DesignSystem.CardBg
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(request.subjectName ?: request.subjectCode ?: "Condonation", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
                StatusChip(request.status ?: request.hodDecision ?: "PENDING")
            }
            Text(text = request.studentName ?: request.rollNo, color = DesignSystem.TextSecondary, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DateRange, null, tint = DesignSystem.TextMuted, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text(request.appliedOn ?: "N/A", color = DesignSystem.TextMuted, fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun RequestDetailView(request: ODRequest, vm: MainViewModel, onProcessed: () -> Unit) {
    var remarks by remember { mutableStateOf("") }
    val status = request.status

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Text("Application Review", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
        
        GlassmorphicCard(backgroundColor = DesignSystem.Background) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                DetailRow("Student Name", request.studentName ?: "Unknown")
                DetailRow("Roll Number", request.rollNo)
                DetailRow("Event Name", request.eventName)
                DetailRow("Type", request.eventType ?: "Academic OD")
                DetailRow("Applied On", request.appliedOn ?: "N/A")
            }
        }

        Column {
            Text("Justification Reason", color = DesignSystem.TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(
                text = request.reason ?: "No justification provided.",
                color = Color.White,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }

        if (status.uppercase() == "PENDING" || status.uppercase() == "UNDER_REVIEW") {
            OutlinedTextField(
                value = remarks,
                onValueChange = { remarks = it },
                label = { Text("Decision Remarks") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )
        }

        HorizontalDivider(color = DesignSystem.Border)

        Text("Approval Workflow", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
        ApprovalTimeline(steps = request.approvalSteps)

        Spacer(modifier = Modifier.height(24.dp))

        if (status.uppercase() == "PENDING" || status.uppercase() == "UNDER_REVIEW") {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { 
                        vm.rejectLeaveODAction(request.id, remarks.ifEmpty { "Rejected by HOD" })
                        onProcessed()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DesignSystem.Danger.copy(alpha = 0.2f)),
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(DesignSystem.CornerRadius)
                ) {
                    Text("Reject", color = DesignSystem.Danger, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { 
                        vm.approveLeaveODAction(request.id, remarks.ifEmpty { "Approved by HOD" })
                        onProcessed()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DesignSystem.Success),
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(DesignSystem.CornerRadius)
                ) {
                    Text("Approve", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            Surface(
                color = DesignSystem.CardBg,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "This request has already been processed and is in $status state.",
                    color = DesignSystem.TextMuted,
                    modifier = Modifier.padding(16.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun CondonationDetailView(request: CondonationRequest, vm: MainViewModel, onProcessed: () -> Unit) {
    var remarks by remember { mutableStateOf("") }
    val status = request.status ?: request.hodDecision ?: "PENDING"
    
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Text("Condonation Review", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
        
        GlassmorphicCard(backgroundColor = DesignSystem.Background) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                DetailRow("Student Name", request.studentName ?: "Unknown")
                DetailRow("Roll Number", request.rollNo)
                DetailRow("Subject", request.subjectName ?: request.subjectCode ?: "N/A")
                DetailRow("Attendance Rate", "${(request.attendanceRate ?: 0.0)}%")
                DetailRow("Min Required", "${(request.requiredRate ?: 75.0)}%")
                DetailRow("Applied On", request.appliedOn ?: "N/A")
            }
        }

        Column {
            Text("Reason for Condonation", color = DesignSystem.TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(
                text = request.reason,
                color = Color.White,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }

        if (status.uppercase() == "PENDING" || status.uppercase() == "UNDER_REVIEW") {
            OutlinedTextField(
                value = remarks,
                onValueChange = { remarks = it },
                label = { Text("Decision Remarks") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )
        }

        HorizontalDivider(color = DesignSystem.Border)

        Text("Approval Workflow", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
        ApprovalTimeline(steps = request.approvalHistory)

        Spacer(modifier = Modifier.height(24.dp))

        if (status.uppercase() == "PENDING" || status.uppercase() == "UNDER_REVIEW") {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { 
                        vm.rejectCondonationAction(request.id, remarks.ifEmpty { "Rejected by Committee" })
                        onProcessed()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DesignSystem.Danger.copy(alpha = 0.2f)),
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(DesignSystem.CornerRadius)
                ) {
                    Text("Reject", color = DesignSystem.Danger, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { 
                        vm.approveCondonationAction(request.id, remarks.ifEmpty { "Approved by Condonation Committee" })
                        onProcessed()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DesignSystem.Success),
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(DesignSystem.CornerRadius)
                ) {
                    Text("Approve", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            Surface(
                color = DesignSystem.CardBg,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "This request has already been processed and is in $status state.",
                    color = DesignSystem.TextMuted,
                    modifier = Modifier.padding(16.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = DesignSystem.TextSecondary, fontSize = 12.sp)
        Text(value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun StatusChip(status: String) {
    val color = when (status.uppercase()) {
        "APPROVED" -> DesignSystem.Success
        "PENDING" -> DesignSystem.Warning
        "REJECTED" -> DesignSystem.Danger
        else -> DesignSystem.Violet
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(status, color = color, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
    }
}
