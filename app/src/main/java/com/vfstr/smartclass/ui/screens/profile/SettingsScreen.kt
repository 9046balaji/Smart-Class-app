package com.vfstr.smartclass.ui.screens.profile

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.vfstr.smartclass.BuildConfig
import com.vfstr.smartclass.domain.models.UserRole
import com.vfstr.smartclass.ui.MainViewModel
import com.vfstr.smartclass.ui.components.GlassmorphicCard
import com.vfstr.smartclass.ui.components.MeshBackground
import com.vfstr.smartclass.ui.theme.DesignSystem

@Composable
fun SettingsScreen(vm: MainViewModel) {
    val currentRole by vm.currentRole.collectAsState()
    val isDarkTheme by vm.isDarkTheme.collectAsState()
    
    val notifAttendance by vm.notifAttendance.collectAsState()
    val notifDefaulter by vm.notifDefaulter.collectAsState()
    val notifCompliance by vm.notifCompliance.collectAsState()
    val biometricLockEnabled by vm.biometricLockEnabled.collectAsState()
    val isDataSaverEnabled by vm.isDataSaverEnabled.collectAsState()
    val isDndAutomationEnabled by vm.isDndAutomationEnabled.collectAsState()
    val syncQueueCount by vm.syncQueueCount.collectAsState()
    val localDbSize by vm.localDbSize.collectAsState()
    val isMockLocation by vm.isMockLocation.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        MeshBackground()
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(DesignSystem.Padding),
            verticalArrangement = Arrangement.spacedBy(DesignSystem.SpacingLarge)
        ) {
            item {
                Text(
                    text = "APPLICATION SETTINGS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = DesignSystem.Cyan,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                )
            }

            // Block 1: Appearance & Theme
            item {
                AppearanceBlock(
                    role = currentRole,
                    isDark = isDarkTheme,
                    onThemeChange = { vm.updateTheme(it) }
                )
            }

            // Block 2: Notification Configuration
            if (currentRole != UserRole.viewer) {
                item {
                    NotificationsBlock(
                        role = currentRole,
                        attendance = notifAttendance,
                        defaulter = notifDefaulter,
                        compliance = notifCompliance,
                        onUpdate = { key, enabled -> vm.updateNotificationSetting(key, enabled) }
                    )
                }
            }

            // Block 3: Media & Storage Hub (NEW)
            item {
                MediaStorageBlock(
                    syncQueueCount = syncQueueCount,
                    localDbSize = localDbSize,
                    isDataSaverEnabled = isDataSaverEnabled,
                    onToggleDataSaver = { vm.updateDataSaver(it) },
                    onClearCache = { 
                        vm.clearCache()
                        vm.updateStorageMetrics()
                    }
                )
            }

            // Block 4: Permission & Integrity Dashboard (NEW)
            item {
                IntegrityDashboardBlock(
                    isMocked = isMockLocation,
                    dndAutomationEnabled = isDndAutomationEnabled,
                    onToggleDndAutomation = { vm.updateDndAutomation(it) }
                )
            }

            // Block 5: Legal Information Directory (NEW)
            item {
                LegalInfoBlock()
            }

            // Block 6: Dev Mode / Bypass (Debug Builds Only)
            if (BuildConfig.DEBUG) {
                item {
                    DevModeBlock(vm)
                }
            }

            // Block 7: Security & Logout
            item {
                SecurityBlock(
                    role = currentRole,
                    biometricLockEnabled = biometricLockEnabled,
                    onToggleBiometricLock = { vm.updateBiometricLock(it) },
                    onLogout = { vm.logout() },
                    vm = vm
                )
            }
            
            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }
}

@Composable
fun AppearanceBlock(role: UserRole?, isDark: Boolean, onThemeChange: (Boolean) -> Unit) {
    SettingsSection(title = "APPEARANCE", icon = Icons.Default.Palette, tint = DesignSystem.Cyan) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Dark Mode", color = DesignSystem.TextPrimary, fontWeight = FontWeight.Bold)
                Text(
                    text = if (role == UserRole.student) "Toggle between light and dark themes" else "Fixed to dark aesthetic for enterprise security",
                    color = DesignSystem.TextSecondary,
                    fontSize = 12.sp
                )
            }
            Switch(
                checked = isDark,
                onCheckedChange = onThemeChange,
                enabled = role == UserRole.student,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = DesignSystem.Cyan,
                    checkedTrackColor = DesignSystem.Cyan.copy(alpha = 0.5f)
                )
            )
        }
    }
}

@Composable
fun NotificationsBlock(
    role: UserRole?,
    attendance: Boolean,
    defaulter: Boolean,
    compliance: Boolean,
    onUpdate: (String, Boolean) -> Unit
) {
    SettingsSection(title = "NOTIFICATIONS", icon = Icons.Default.Notifications, tint = DesignSystem.Warning) {
        val labelSuffix = if (role == UserRole.student) "Warnings" else "Alerts"
        
        NotificationToggle(
            label = "Low Attendance $labelSuffix",
            description = "Alert when thresholds drop below 85% or 75%",
            checked = attendance,
            onCheckedChange = { onUpdate("attendance", it) }
        )
        
        HorizontalDivider(modifier = Modifier.padding(vertical = DesignSystem.SpacingSmall), color = DesignSystem.Border)
        
        NotificationToggle(
            label = "Defaulter Status Reports",
            description = "Daily compliance and eligibility updates",
            checked = defaulter,
            onCheckedChange = { onUpdate("defaulter", it) }
        )
        
        HorizontalDivider(modifier = Modifier.padding(vertical = DesignSystem.SpacingSmall), color = DesignSystem.Border)
        
        NotificationToggle(
            label = "Compliance Processing",
            description = "Edge device telemetry and review cycles",
            checked = compliance,
            onCheckedChange = { onUpdate("compliance", it) }
        )
    }
}

@Composable
fun NotificationToggle(label: String, description: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, color = DesignSystem.TextPrimary, fontWeight = FontWeight.Bold)
            Text(text = description, color = DesignSystem.TextSecondary, fontSize = 12.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = DesignSystem.Warning,
                checkedTrackColor = DesignSystem.Warning.copy(alpha = 0.5f)
            )
        )
    }
}

@Composable
fun DevModeBlock(vm: MainViewModel) {
    SettingsSection(title = "DEVELOPER PARAMETERS", icon = Icons.Default.BugReport, tint = DesignSystem.Violet) {
        Text(text = "DEBUG MODE ACTIVE", color = DesignSystem.Violet, fontWeight = FontWeight.Black, fontSize = 10.sp)
        Spacer(modifier = Modifier.height(DesignSystem.SpacingSmall))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Bypass Login", color = DesignSystem.TextPrimary, fontWeight = FontWeight.Bold)
                Text(text = "Simulate high-clearance UI flows", color = DesignSystem.TextSecondary, fontSize = 12.sp)
            }
            Switch(
                checked = false, 
                onCheckedChange = {},
                colors = SwitchDefaults.colors(
                    checkedThumbColor = DesignSystem.Violet,
                    checkedTrackColor = DesignSystem.Violet.copy(alpha = 0.5f)
                )
            )
        }
        
        Spacer(modifier = Modifier.height(DesignSystem.SpacingMedium))
        
        Button(
            onClick = { /* Simulate Role Pickers */ },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = DesignSystem.Violet.copy(alpha = 0.1f)),
            border = androidx.compose.foundation.BorderStroke(1.dp, DesignSystem.Violet.copy(alpha = 0.3f))
        ) {
            Text("Simulate Role Picker", color = DesignSystem.Violet, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun MediaStorageBlock(
    syncQueueCount: Int,
    localDbSize: Long,
    isDataSaverEnabled: Boolean,
    onToggleDataSaver: (Boolean) -> Unit,
    onClearCache: () -> Unit
) {
    SettingsSection(title = "MEDIA & STORAGE", icon = Icons.Default.Storage, tint = DesignSystem.Cyan) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Smart Network Throttler", color = DesignSystem.TextPrimary, fontWeight = FontWeight.Bold)
                    Text(text = "Data Saver: Restrict background sync and compress images on cellular.", color = DesignSystem.TextSecondary, fontSize = 12.sp)
                }
                Switch(
                    checked = isDataSaverEnabled,
                    onCheckedChange = onToggleDataSaver,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = DesignSystem.Cyan,
                        checkedTrackColor = DesignSystem.Cyan.copy(alpha = 0.5f)
                    )
                )
            }

            HorizontalDivider(color = DesignSystem.Border)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "File Upload Specs", color = DesignSystem.TextPrimary, fontWeight = FontWeight.Bold)
                    Text(
                        text = if (isDataSaverEnabled) "JPEG (Max 10MB) @ 70% Quality (Saver Active)" else "JPEG/PNG (Max 10MB) @ 85% Quality",
                        color = DesignSystem.TextSecondary, 
                        fontSize = 12.sp
                    )
                }
                Icon(Icons.Default.Info, null, tint = DesignSystem.Cyan, modifier = Modifier.size(20.dp))
            }

            HorizontalDivider(color = DesignSystem.Border)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Sync Queue Monitor", color = DesignSystem.TextPrimary, fontWeight = FontWeight.Bold)
                    Text(
                        text = if (syncQueueCount > 0) "$syncQueueCount pending mutations" else "All local records synchronized",
                        color = if (syncQueueCount > 0) DesignSystem.Warning else DesignSystem.TextSecondary,
                        fontSize = 12.sp
                    )
                }
                if (syncQueueCount > 0) {
                    Icon(Icons.Default.Sync, null, tint = DesignSystem.Warning, modifier = Modifier.size(20.dp))
                } else {
                    Icon(Icons.Default.CloudDone, null, tint = DesignSystem.Success, modifier = Modifier.size(20.dp))
                }
            }

            HorizontalDivider(color = DesignSystem.Border)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Local Cache Allocation", color = DesignSystem.TextPrimary, fontWeight = FontWeight.Bold)
                    val sizeMb = localDbSize / (1024.0 * 1024.0)
                    Text(text = String.format("%.2f MB occupied in Room DB", sizeMb), color = DesignSystem.TextSecondary, fontSize = 12.sp)
                }
                TextButton(onClick = onClearCache) {
                    Text("PURGE CACHE", color = DesignSystem.Danger, fontWeight = FontWeight.Black, fontSize = 11.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun IntegrityDashboardBlock(
    isMocked: Boolean,
    dndAutomationEnabled: Boolean,
    onToggleDndAutomation: (Boolean) -> Unit
) {
    val context = LocalContext.current
    SettingsSection(title = "SYSTEM INTEGRITY & PERMISSIONS", icon = Icons.Default.VpnKey, tint = DesignSystem.Violet) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // DND Automation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Attendance Session DND", color = DesignSystem.TextPrimary, fontWeight = FontWeight.Bold)
                    Text(text = "Auto-toggle Do Not Disturb during active sessions/scans.", color = DesignSystem.TextSecondary, fontSize = 12.sp)
                }
                Switch(
                    checked = dndAutomationEnabled,
                    onCheckedChange = onToggleDndAutomation,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = DesignSystem.Violet,
                        checkedTrackColor = DesignSystem.Violet.copy(alpha = 0.5f)
                    )
                )
            }

            HorizontalDivider(color = DesignSystem.Border)

            // Integrity Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Hardware Attestation", color = DesignSystem.TextPrimary, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isMocked) "SPOOFING DETECTED" else "TRUSTED DEVICE",
                        color = if (isMocked) DesignSystem.Danger else DesignSystem.Success,
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        if (isMocked) Icons.Default.Warning else Icons.Default.VerifiedUser,
                        null,
                        tint = if (isMocked) DesignSystem.Danger else DesignSystem.Success,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            HorizontalDivider(color = DesignSystem.Border)

            PermissionStatusRow(
                label = "Location Access",
                isGranted = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION).status.isGranted,
                onClick = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                }
            )
            PermissionStatusRow(
                label = "Camera Recognition",
                isGranted = rememberPermissionState(Manifest.permission.CAMERA).status.isGranted,
                onClick = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                }
            )
            PermissionStatusRow(
                label = "Notification Channel",
                isGranted = if (android.os.Build.VERSION.SDK_INT >= 33) {
                    rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS).status.isGranted
                } else true,
                onClick = {
                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    }
                    context.startActivity(intent)
                }
            )
            
            PermissionStatusRow(
                label = "DND Policy Access",
                isGranted = (context.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager).isNotificationPolicyAccessGranted,
                onClick = {
                    val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                    context.startActivity(intent)
                }
            )
        }
    }
}

@Composable
fun PermissionStatusRow(label: String, isGranted: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = DesignSystem.TextPrimary, fontWeight = FontWeight.Bold)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (isGranted) "AUTHORIZED" else "DISABLED",
                color = if (isGranted) DesignSystem.Success else DesignSystem.Danger,
                fontWeight = FontWeight.Black,
                fontSize = 10.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.OpenInNew, null, tint = DesignSystem.TextMuted, modifier = Modifier.size(14.dp))
        }
    }
}

@Composable
fun LegalInfoBlock() {
    SettingsSection(title = "LEGAL & POLICIES", icon = Icons.Default.Description, tint = DesignSystem.TextSecondary) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            LegalPolicyRow("Terms of Service", "SmartClass Academic Agreement v2.1")
            LegalPolicyRow("Privacy Policy", "DPDP Act Compliance & Data Handling")
            LegalPolicyRow("Biometric Disclosure", "Facial Hashing & Privacy Information")
        }
    }
}

@Composable
fun LegalPolicyRow(title: String, version: String) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = title, color = DesignSystem.TextPrimary, fontWeight = FontWeight.Bold)
                Text(text = version, color = DesignSystem.TextMuted, fontSize = 10.sp)
            }
            Icon(
                if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                null,
                tint = DesignSystem.TextMuted
            )
        }
        AnimatedVisibility(visible = expanded, enter = expandVertically(), exit = shrinkVertically()) {
            Text(
                text = "Full policy text available at vfstr.edu/privacy. This app adheres to strict academic integrity protocols.",
                color = DesignSystem.TextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun SecurityBlock(
    role: UserRole?,
    biometricLockEnabled: Boolean,
    onToggleBiometricLock: (Boolean) -> Unit,
    onLogout: () -> Unit,
    vm: MainViewModel
) {
    SettingsSection(title = "SECURITY & ACCESS", icon = Icons.Default.Security, tint = DesignSystem.Danger) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Biometric App Lock", color = DesignSystem.TextPrimary, fontWeight = FontWeight.Bold)
                Text(
                    text = "Secure the app with your device's biometric authentication",
                    color = DesignSystem.TextSecondary,
                    fontSize = 12.sp
                )
            }
            Switch(
                checked = biometricLockEnabled,
                onCheckedChange = onToggleBiometricLock,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = DesignSystem.Danger,
                    checkedTrackColor = DesignSystem.Danger.copy(alpha = 0.5f)
                )
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = DesignSystem.SpacingSmall), color = DesignSystem.Border)

        if (role == UserRole.student) {
            var showPasswordDialog by remember { mutableStateOf(false) }
            
            Button(
                onClick = { showPasswordDialog = true },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DesignSystem.Surface),
                shape = RoundedCornerShape(DesignSystem.CornerRadius),
                border = androidx.compose.foundation.BorderStroke(1.dp, DesignSystem.Border)
            ) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = DesignSystem.Cyan)
                Spacer(modifier = Modifier.width(DesignSystem.SpacingMedium))
                Text("Change Portal Password", color = Color.White, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(DesignSystem.SpacingMedium))

            if (showPasswordDialog) {
                var currentPass by remember { mutableStateOf("") }
                var newPass by remember { mutableStateOf("") }
                val isChanging by vm.isPasswordChanging.collectAsState()
                val changeSuccess by vm.passwordChangeSuccess.collectAsState()

                AlertDialog(
                    onDismissRequest = { showPasswordDialog = false; vm.passwordChangeSuccess.value = null },
                    title = { Text("Change Password") },
                    containerColor = DesignSystem.Surface,
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = currentPass,
                                onValueChange = { currentPass = it },
                                label = { Text("Current Password") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = newPass,
                                onValueChange = { newPass = it },
                                label = { Text("New Password") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            if (changeSuccess == false) {
                                Text("Incorrect password or update failed.", color = DesignSystem.Danger, fontSize = 12.sp)
                            } else if (changeSuccess == true) {
                                Text("Password changed successfully!", color = DesignSystem.Success, fontSize = 12.sp)
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (currentPass.isNotEmpty() && newPass.isNotEmpty()) {
                                    vm.changeStudentPassword(currentPass, newPass)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DesignSystem.Cyan, contentColor = Color.Black),
                            enabled = !isChanging && currentPass.isNotEmpty() && newPass.isNotEmpty()
                        ) {
                            if (isChanging) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.Black)
                            } else {
                                Text("Save", fontWeight = FontWeight.Bold)
                            }
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showPasswordDialog = false; vm.passwordChangeSuccess.value = null }) {
                            Text("Cancel", color = DesignSystem.TextMuted)
                        }
                    }
                )
            }
        }

        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DesignSystem.Danger.copy(alpha = 0.1f)),
            shape = RoundedCornerShape(DesignSystem.CornerRadius),
            border = androidx.compose.foundation.BorderStroke(1.dp, DesignSystem.Danger.copy(alpha = 0.3f))
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = DesignSystem.Danger)
            Spacer(modifier = Modifier.width(DesignSystem.SpacingMedium))
            Text("Sign Out of Session", color = DesignSystem.Danger, fontWeight = FontWeight.ExtraBold)
        }
        
        Spacer(modifier = Modifier.height(DesignSystem.SpacingMedium))
        Text(
            text = "Tapping Sign Out will purge all access tokens, clear local caches, and invalidate active session keys.",
            color = DesignSystem.TextSecondary,
            fontSize = 11.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun SettingsSection(title: String, icon: ImageVector, tint: Color, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(DesignSystem.SpacingSmall))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = DesignSystem.TextSecondary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            )
        }
        Spacer(modifier = Modifier.height(DesignSystem.SpacingSmall))
        GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(DesignSystem.Padding)) {
                content()
            }
        }
    }
}
