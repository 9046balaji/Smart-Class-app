package com.vfstr.smartclass.ui.screens.audit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.border
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vfstr.smartclass.domain.models.AuditLog
import com.vfstr.smartclass.ui.MainViewModel
import com.vfstr.smartclass.ui.components.GlassmorphicCard
import com.vfstr.smartclass.ui.theme.DesignSystem

import com.vfstr.smartclass.ui.components.StickyDataTable
import androidx.compose.foundation.border
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.ui.unit.dp

@Composable
fun ScreenAudit(
    vm: MainViewModel,
    modifier: Modifier = Modifier
) {
    val logs by vm.auditLogs.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var viewMode by remember { mutableStateOf("List") } // List or Table
    
    LaunchedEffect(Unit) {
        vm.loadAuditLogs()
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
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(
                        text = "Security Audit Trail",
                        style = MaterialTheme.typography.headlineSmall.copy(color = DesignSystem.TextPrimary, fontWeight = FontWeight.Bold)
                    )
                    Text("Complete history of administrative actions", color = DesignSystem.TextSecondary, style = MaterialTheme.typography.bodySmall)
                }
                
                Row {
                    IconButton(onClick = { viewMode = if (viewMode == "List") "Table" else "List" }) {
                        Icon(
                            imageVector = if (viewMode == "List") Icons.Default.TableChart else Icons.AutoMirrored.Filled.List,
                            contentDescription = "Toggle View",
                            tint = DesignSystem.Cyan
                        )
                    }
                    Button(onClick = { /* Export */ }, colors = ButtonDefaults.buttonColors(containerColor = DesignSystem.Surface)) {
                        Icon(Icons.Default.FileDownload, null, modifier = Modifier.size(16.dp), tint = DesignSystem.Cyan)
                        Spacer(Modifier.width(8.dp))
                        Text("Export", color = DesignSystem.TextPrimary)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Filter logs by user or resource...", color = DesignSystem.TextMuted) },
                leadingIcon = { Icon(Icons.Default.FilterList, null, tint = DesignSystem.TextMuted) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DesignSystem.Cyan,
                    unfocusedBorderColor = DesignSystem.Border
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            val filteredLogs = logs.filter { 
                it.userName.contains(searchQuery, ignoreCase = true) || 
                it.resource.contains(searchQuery, ignoreCase = true) ||
                it.action.contains(searchQuery, ignoreCase = true)
            }

            if (viewMode == "List") {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(filteredLogs) { log ->
                        AuditLogItem(log)
                    }
                }
            } else {
                // Rule 15: Sticky Data Table
                StickyDataTable(
                    headers = listOf("Timestamp", "Actor", "Action", "Resource", "IP Address"),
                    rows = filteredLogs.map { 
                        listOf(
                            it.timestamp.replace("T", " ").take(16),
                            it.userName,
                            it.action,
                            it.resource,
                            it.ipAddress ?: "N/A"
                        )
                    },
                    columnWidths = listOf(140.dp, 100.dp, 80.dp, 200.dp, 120.dp),
                    stickyColumns = 2,
                    modifier = Modifier.clip(RoundedCornerShape(DesignSystem.CornerRadius)).border(1.dp, DesignSystem.Border, RoundedCornerShape(DesignSystem.CornerRadius))
                )
            }
        }
    }
}

@Composable
fun AuditLogItem(log: AuditLog) {
    var expanded by remember { mutableStateOf(false) }
    val color = when (log.action) {
        "CREATE" -> DesignSystem.Success
        "UPDATE" -> DesignSystem.Warning
        "DELETE" -> DesignSystem.Danger
        "LOGIN" -> DesignSystem.Cyan
        "SYSTEM_CONFIG" -> DesignSystem.Danger
        else -> DesignSystem.TextMuted
    }

    GlassmorphicCard(
        modifier = Modifier.clickable { expanded = !expanded }
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(60.dp)) {
                    Text(text = log.timestamp.split("T")[1].take(5), fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                    Text(text = log.timestamp.split("T")[0].split("-").last() + " May", fontSize = 10.sp, color = DesignSystem.TextMuted)
                }
                
                Spacer(Modifier.width(12.dp))
                
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(color.copy(alpha = 0.15f)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                            Text(log.action, color = color, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(log.resource, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
                    }
                    Text("Actor: ${log.userName} • IP: ${log.ipAddress ?: "N/A"}", fontSize = 11.sp, color = DesignSystem.TextSecondary)
                }
                
                Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null, tint = DesignSystem.TextMuted)
            }
            
            if (expanded) {
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.3f))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "{\"action\": \"${log.action}\", \"resource\": \"${log.resource}\", \"user\": \"${log.userName}\", \"details\": \"Full operation payload logged securely\"}",
                        color = DesignSystem.Success,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}
