package com.vfstr.smartclass.ui.screens.notifications

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vfstr.smartclass.ui.MainViewModel
import com.vfstr.smartclass.ui.components.GlassmorphicCard
import com.vfstr.smartclass.ui.theme.DesignSystem

import com.vfstr.smartclass.ui.components.StickyDataTable
import androidx.compose.ui.unit.dp

@Composable
fun ScreenNotifications(
    vm: MainViewModel,
    modifier: Modifier = Modifier
) {
    val logs by vm.notificationLogs.collectAsState()
    val guardianList by vm.guardians.collectAsState()
    var activeTab by remember { mutableStateOf("Log") } // Log, Send, Guardians

    LaunchedEffect(Unit) {
        vm.loadNotificationLogs()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DesignSystem.Background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(DesignSystem.Padding)
        ) {
            Text(
                text = "Communication Center",
                style = MaterialTheme.typography.headlineSmall.copy(color = DesignSystem.TextPrimary, fontWeight = FontWeight.Bold)
            )
            Text(text = "Broadcast notifications and manage guardian contacts", color = DesignSystem.TextSecondary, style = MaterialTheme.typography.bodySmall)
            
            Spacer(modifier = Modifier.height(20.dp))

            // Navigation Tabs
            Row(modifier = Modifier.fillMaxWidth().height(44.dp).clip(RoundedCornerShape(10.dp)).background(DesignSystem.Surface).padding(4.dp)) {
                listOf("Log", "Send", "Guardians").forEach { tab ->
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
                        Text(tab, color = if (isSel) DesignSystem.Cyan else DesignSystem.TextSecondary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            when (activeTab) {
                "Log" -> NotificationLogView(logs)
                "Send" -> SendNotificationView()
                "Guardians" -> GuardiansDirectoryView(guardianList)
            }
        }
    }
}

@Composable
fun NotificationLogView(logs: List<String>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(logs) { log ->
            GlassmorphicCard {
                Row(Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(44.dp).clip(CircleShape).background(DesignSystem.Surface), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Textsms, null, tint = DesignSystem.Cyan, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Low Attendance Warning", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Sent to Guardian of 22L11A0501", fontSize = 11.sp, color = DesignSystem.TextSecondary)
                        Text("Just now", fontSize = 10.sp, color = DesignSystem.TextMuted)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(DesignSystem.Success.copy(alpha = 0.1f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("SENT", fontSize = 9.sp, color = DesignSystem.Success, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}

@Composable
fun SendNotificationView() {
    var message by remember { mutableStateOf("") }
    var selectedChannel by remember { mutableStateOf("SMS") }
    var isEmergency by remember { mutableStateOf(false) }
    
    Column(verticalArrangement = Arrangement.spacedBy(20.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Bulk Notification Composer", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Surface(color = DesignSystem.Cyan.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                Text("842 SMS Credits", color = DesignSystem.Cyan, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
            }
        }

        // Emergency Switchboard
        GlassmorphicCard(
            borderColor = if (isEmergency) DesignSystem.Danger.copy(alpha = 0.5f) else DesignSystem.Border,
            backgroundColor = if (isEmergency) DesignSystem.Danger.copy(alpha = 0.05f) else DesignSystem.CardBg
        ) {
            Row(
                modifier = Modifier.padding(DesignSystem.Padding),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        Icons.Default.Emergency,
                        null,
                        tint = if (isEmergency) DesignSystem.Danger else DesignSystem.TextMuted,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            "Emergency Broadcast Mode",
                            color = if (isEmergency) DesignSystem.Danger else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text("Bypass queues for instant high-priority alerts", color = DesignSystem.TextSecondary, fontSize = 10.sp)
                    }
                }
                Switch(
                    checked = isEmergency,
                    onCheckedChange = { isEmergency = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = DesignSystem.Danger,
                        checkedTrackColor = DesignSystem.Danger.copy(alpha = 0.3f)
                    )
                )
            }
        }

        GlassmorphicCard(backgroundColor = DesignSystem.Surface) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Recipients Target", color = DesignSystem.TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = "CSAI - III Year - Section A",
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = { Icon(Icons.Default.Groups, null, tint = DesignSystem.Cyan) },
                    shape = RoundedCornerShape(12.dp)
                )

                Text("Delivery Channels", color = DesignSystem.TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    listOf("SMS", "Email", "Portal").forEach { channel ->
                        val isSel = selectedChannel == channel
                        FilterChip(
                            selected = isSel,
                            onClick = { selectedChannel = channel },
                            label = { Text(channel) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = DesignSystem.Cyan.copy(alpha = 0.2f), selectedLabelColor = DesignSystem.Cyan)
                        )
                    }
                }
            }
        }

        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            label = { Text("Message Content") },
            modifier = Modifier.fillMaxWidth().height(160.dp),
            placeholder = { Text("Example: Dear Parent, student's attendance is below 75%...", color = DesignSystem.TextMuted) },
            shape = RoundedCornerShape(12.dp)
        )

        Button(
            onClick = { /* Send */ },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = if (isEmergency) DesignSystem.Danger else DesignSystem.Violet),
            shape = RoundedCornerShape(DesignSystem.CornerRadius)
        ) {
            Icon(if (isEmergency) Icons.Default.Campaign else Icons.Default.Send, null)
            Spacer(Modifier.width(12.dp))
            Text(if (isEmergency) "BROADCAST EMERGENCY ALERT" else "Send Message", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun GuardiansDirectoryView(guardians: List<List<String>>) {
    if (guardians.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.ContactPhone, null, tint = DesignSystem.TextMuted, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("Guardians contact directory will appear here.", color = DesignSystem.TextMuted, fontSize = 13.sp)
            }
        }
    } else {
        // Rule 15: Sticky Data Table for directory
        StickyDataTable(
            headers = listOf("Roll No", "Guardian Name", "Phone", "Relation", "Last Alert"),
            rows = guardians.map { it + "Never" },
            columnWidths = listOf(100.dp, 150.dp, 120.dp, 100.dp, 100.dp),
            stickyColumns = 1,
            modifier = Modifier.clip(RoundedCornerShape(DesignSystem.CornerRadius)).border(1.dp, DesignSystem.Border, RoundedCornerShape(DesignSystem.CornerRadius))
        )
    }
}
