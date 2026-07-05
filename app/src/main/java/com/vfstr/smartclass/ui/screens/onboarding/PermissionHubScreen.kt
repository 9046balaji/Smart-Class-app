package com.vfstr.smartclass.ui.screens.onboarding

import android.Manifest
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import com.vfstr.smartclass.ui.theme.DesignSystem

data class PermissionRationale(
    val id: String,
    val permission: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: Color,
)

val rationales = listOf(
    PermissionRationale(
        "location",
        Manifest.permission.ACCESS_FINE_LOCATION,
        "Location Access",
        "Used to verify you are physically present within the VFSTR campus boundaries before recording attendance.",
        Icons.Default.LocationOn,
        DesignSystem.Cyan
    ),
    PermissionRationale(
        "camera",
        Manifest.permission.CAMERA,
        "Camera Access",
        "Required for secure full-frame face identification scanning and document verification.",
        Icons.Default.CameraAlt,
        DesignSystem.Violet
    ),
    PermissionRationale(
        "wifi",
        Manifest.permission.ACCESS_WIFI_STATE,
        "WiFi State",
        "Cross-references classroom-level wireless access points (BSSID) for high-trust Tier 3 verification.",
        Icons.Default.Wifi,
        DesignSystem.Success
    ),
    PermissionRationale(
        "notifications",
        if (android.os.Build.VERSION.SDK_INT >= 33) Manifest.permission.POST_NOTIFICATIONS else "android.permission.POST_NOTIFICATIONS",
        "Notifications",
        "VFSTR SmartClass requires notification access to instantly alert you when attendance thresholds drop below 85% or 75%, or when critical review cycles close.",
        Icons.Default.Notifications,
        DesignSystem.Warning
    ),
    PermissionRationale(
        "dnd",
        "android.settings.NOTIFICATION_POLICY_ACCESS_SETTINGS",
        "DND Automation",
        "Automatically toggles system Do Not Disturb mode during active classroom sessions and scans for a zero-distraction experience.",
        Icons.Default.Lock,
        DesignSystem.Danger
    ),
    PermissionRationale(
        "bluetooth",
        if (android.os.Build.VERSION.SDK_INT >= 31) Manifest.permission.BLUETOOTH_SCAN else Manifest.permission.BLUETOOTH,
        "Bluetooth Sync",
        "Used for classroom beacon detection and peer-to-peer attendance verification using Low Energy (BLE) technology.",
        Icons.Default.Bluetooth,
        DesignSystem.Cyan
    )
)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionHubScreen(
    onPermissionsGranted: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val dndManager = remember { com.vfstr.smartclass.utils.DndManager(context) }
    
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val wifiPermissionState = rememberPermissionState(Manifest.permission.ACCESS_WIFI_STATE)
    val notificationPermissionState = if (android.os.Build.VERSION.SDK_INT >= 33) {
        rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
    } else {
        null
    }

    val bluetoothPermissionsState = if (android.os.Build.VERSION.SDK_INT >= 31) {
        rememberMultiplePermissionsState(
            listOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        )
    } else {
        null
    }

    val dndGranted = dndManager.isPermissionGranted()
    
    val bluetoothGranted = if (android.os.Build.VERSION.SDK_INT >= 31) {
        bluetoothPermissionsState?.allPermissionsGranted ?: true
    } else {
        true // Granted at install time for older versions
    }

    val explicitlyDenied = remember { mutableStateMapOf<String, Boolean>() }

    val allGranted = locationPermissionState.status.isGranted && 
                     cameraPermissionState.status.isGranted && 
                     wifiPermissionState.status.isGranted &&
                     (notificationPermissionState?.status?.isGranted ?: true) &&
                     bluetoothGranted &&
                     dndGranted

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DesignSystem.Background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        
        Text(
            text = "Prerequisites",
            color = DesignSystem.TextPrimary,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "SmartClass works best with these permissions. You can grant them now or manage them later in settings.",
            color = DesignSystem.TextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(rationales) { rationale ->
                val isGranted = when(rationale.id) {
                    "location" -> locationPermissionState.status.isGranted
                    "camera" -> cameraPermissionState.status.isGranted
                    "notifications" -> notificationPermissionState?.status?.isGranted ?: true
                    "dnd" -> dndGranted
                    "bluetooth" -> bluetoothGranted
                    else -> wifiPermissionState.status.isGranted
                }

                PermissionCard(
                    rationale = rationale,
                    isGranted = isGranted,
                    isDenied = explicitlyDenied[rationale.id] ?: false,
                    onGrantClick = {
                        explicitlyDenied[rationale.id] = false
                        when(rationale.id) {
                            "location" -> locationPermissionState.launchPermissionRequest()
                            "camera" -> cameraPermissionState.launchPermissionRequest()
                            "notifications" -> notificationPermissionState?.launchPermissionRequest()
                            "dnd" -> context.startActivity(android.content.Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
                            "bluetooth" -> if (android.os.Build.VERSION.SDK_INT >= 31) bluetoothPermissionsState?.launchMultiplePermissionRequest()
                            else -> wifiPermissionState.launchPermissionRequest()
                        }
                    },
                    onDenyClick = {
                        explicitlyDenied[rationale.id] = true
                    }
                )
            }
        }

        Button(
            onClick = { onPermissionsGranted() },
            enabled = true,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = DesignSystem.Cyan,
            ),
            shape = RoundedCornerShape(DesignSystem.CornerRadius)
        ) {
            Text(
                text = if (allGranted) "Finish Setup" else "Continue to App",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun PermissionCard(
    rationale: PermissionRationale, 
    isGranted: Boolean,
    isDenied: Boolean,
    onGrantClick: () -> Unit,
    onDenyClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(DesignSystem.CornerRadius))
            .background(DesignSystem.CardBg)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(rationale.color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = rationale.icon,
                contentDescription = null,
                tint = rationale.color,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = rationale.title,
                color = DesignSystem.TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                text = rationale.description,
                color = DesignSystem.TextSecondary,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        if (isGranted) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Granted",
                tint = DesignSystem.Success,
                modifier = Modifier.size(32.dp)
            )
        } else if (isDenied) {
            IconButton(onClick = onGrantClick) {
                Icon(
                    imageVector = Icons.Default.Cancel,
                    contentDescription = "Denied",
                    tint = DesignSystem.Danger,
                    modifier = Modifier.size(32.dp)
                )
            }
        } else {
            Column(horizontalAlignment = Alignment.End) {
                Button(
                    onClick = onGrantClick,
                    colors = ButtonDefaults.buttonColors(containerColor = rationale.color),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp).fillMaxWidth(0.3f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Allow", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(4.dp))
                OutlinedButton(
                    onClick = onDenyClick,
                    border = BorderStroke(1.dp, DesignSystem.TextMuted),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp).fillMaxWidth(0.3f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Deny", color = DesignSystem.TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
