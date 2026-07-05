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
    var selectedRequest by remember { mutableStateOf<ODRequest?>(null) }
    var activeTab by rememberSaveable { mutableStateOf("OD Requests") } // OD Requests, Condonation

    LaunchedEffect(Unit) {
        vm.loadODRequests()
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
                                .clickable { activeTab = tab },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(tab, color = if (isSel) DesignSystem.Cyan else DesignSystem.TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(requests) { req ->
                        ODRequestItem(
                            request = req,
                            isSelected = selectedRequest?.id == req.id,
                            onClick = { selectedRequest = req }
                        )
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
                if (selectedRequest != null) {
                    RequestDetailView(selectedRequest!!, vm) { selectedRequest = null }
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
fun RequestDetailView(request: ODRequest, vm: MainViewModel, onProcessed: () -> Unit) {
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

        HorizontalDivider(color = DesignSystem.Border)

        Text("Approval Workflow", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
        ApprovalTimeline(steps = request.approvalSteps)

        Spacer(modifier = Modifier.height(24.dp))

        if (request.status == "PENDING") {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { onProcessed() },
                    colors = ButtonDefaults.buttonColors(containerColor = DesignSystem.Danger.copy(alpha = 0.2f)),
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(DesignSystem.CornerRadius)
                ) {
                    Text("Reject", color = DesignSystem.Danger, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { 
                        vm.approveLeaveODAction(request.id, "Validated and approved")
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
                    "This request has already been processed and is in ${request.status} state.",
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
