package com.vfstr.smartclass.ui.screens.override

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vfstr.smartclass.ui.MainViewModel
import com.vfstr.smartclass.ui.components.GlassmorphicCard
import com.vfstr.smartclass.ui.theme.DesignSystem

@Composable
fun ScreenOverride(
    vm: MainViewModel,
    modifier: Modifier = Modifier
) {
    var rollNo by rememberSaveable { mutableStateOf("") }
    var sessionId by rememberSaveable { mutableStateOf("") }
    var status by rememberSaveable { mutableStateOf("Present") }
    var reason by rememberSaveable { mutableStateOf("") }
    
    var showConfirm by rememberSaveable { mutableStateOf(false) }
    var showExitConfirmation by rememberSaveable { mutableStateOf(false) }

    val hasUnsavedChanges = rollNo.isNotBlank() || sessionId.isNotBlank() || reason.isNotBlank()

    BackHandler(enabled = hasUnsavedChanges) {
        showExitConfirmation = true
    }

    if (showExitConfirmation) {
        AlertDialog(
            onDismissRequest = { showExitConfirmation = false },
            containerColor = DesignSystem.Surface,
            title = { Text("Discard changes?", color = Color.White) },
            text = { Text("You have unsaved manual override data. Exiting will clear this form.", color = DesignSystem.TextSecondary) },
            confirmButton = {
                TextButton(onClick = { 
                    showExitConfirmation = false
                    vm.currentRoute.value = com.vfstr.smartclass.ui.navigation.Navigation.ROUTE_OVERVIEW 
                }) {
                    Text("Discard", color = DesignSystem.Danger)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitConfirmation = false }) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }

    val isValid = rollNo.isNotEmpty() && sessionId.isNotEmpty() && reason.length >= 10 && reason.length <= 200

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DesignSystem.Background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(DesignSystem.PaddingLarge),
            verticalArrangement = Arrangement.spacedBy(DesignSystem.SpacingLarge)
        ) {
            Text(
                text = "Manual Attendance Override",
                style = MaterialTheme.typography.headlineSmall.copy(color = DesignSystem.TextPrimary, fontWeight = FontWeight.Bold)
            )
            Text(text = "Correct attendance records with justification", color = DesignSystem.TextSecondary, style = MaterialTheme.typography.bodySmall)

            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(DesignSystem.PaddingLarge), verticalArrangement = Arrangement.spacedBy(DesignSystem.SpacingMedium)) {
                    OutlinedTextField(
                        value = rollNo,
                        onValueChange = { rollNo = it.uppercase() },
                        label = { Text("Student Roll Number") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.School, null, tint = DesignSystem.Cyan) },
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = sessionId,
                        onValueChange = { sessionId = it },
                        label = { Text("Session Identifier (UUID)") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Schedule, null, tint = DesignSystem.Cyan) },
                        shape = RoundedCornerShape(12.dp)
                    )

                    Text("New Attendance Status", style = MaterialTheme.typography.labelSmall, color = DesignSystem.TextSecondary, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(DesignSystem.SpacingMedium)) {
                        listOf("Present", "Absent").forEach { opt ->
                            val isSel = status == opt
                            FilterChip(
                                selected = isSel,
                                onClick = { status = opt },
                                label = { Text(opt) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = if (opt == "Present") DesignSystem.Success.copy(alpha = 0.2f) else DesignSystem.Danger.copy(alpha = 0.2f),
                                    selectedLabelColor = if (opt == "Present") DesignSystem.Success else DesignSystem.Danger,
                                    containerColor = DesignSystem.Surface,
                                    labelColor = DesignSystem.TextSecondary
                                )
                            )
                        }
                    }

                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Justification Reason") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        supportingText = {
                            Text("${reason.length}/200 chars (Min 10)", color = if (reason.length < 10 && reason.isNotEmpty()) DesignSystem.Danger else DesignSystem.TextMuted)
                        },
                        isError = reason.length > 200 || (reason.isNotEmpty() && reason.length < 10),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { showConfirm = true },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        enabled = isValid,
                        colors = ButtonDefaults.buttonColors(containerColor = DesignSystem.Violet),
                        shape = RoundedCornerShape(DesignSystem.CornerRadius)
                    ) {
                        Text("Review & Authorize Override", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // History Section (Correction 5)
            Text(text = "Recent Override History", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
            
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    EmptyHistoryPlaceholder()
                }
            }
        }
    }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            containerColor = DesignSystem.Surface,
            title = { Text("Confirm Override", color = DesignSystem.TextPrimary) },
            text = {
                Text("Are you sure you want to mark $rollNo as $status for session $sessionId? This action will be permanently logged in the security audit trail.", color = DesignSystem.TextSecondary)
            },
            confirmButton = {
                Button(
                    onClick = {
                        vm.submitOverride(rollNo, sessionId, status, reason)
                        showConfirm = false
                        rollNo = ""; sessionId = ""; reason = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DesignSystem.Success),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Authorize", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun EmptyHistoryPlaceholder() {
    Box(modifier = Modifier.fillMaxWidth().padding(DesignSystem.PaddingLarge), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.History, null, tint = DesignSystem.TextMuted, modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text("No recent overrides found for your account.", color = DesignSystem.TextMuted, fontSize = 12.sp)
        }
    }
}
