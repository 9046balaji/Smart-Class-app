package com.vfstr.smartclass.ui.screens.onboarding

import android.Manifest
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.*
import com.vfstr.smartclass.ui.components.GlassmorphicCard
import com.vfstr.smartclass.ui.theme.DesignSystem
import kotlinx.coroutines.launch

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

@OptIn(ExperimentalPermissionsApi::class, ExperimentalFoundationApi::class)
@Composable
fun PermissionHubScreen(
    onPermissionsGranted: () -> Unit
) {
    val context = LocalContext.current
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

    val pagerState = rememberPagerState(pageCount = { rationales.size })
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DesignSystem.Background)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val rationale = rationales[page]
            
            val isGranted = when(rationale.id) {
                "location" -> locationPermissionState.status.isGranted
                "camera" -> cameraPermissionState.status.isGranted
                "notifications" -> notificationPermissionState?.status?.isGranted ?: true
                "dnd" -> dndGranted
                "bluetooth" -> bluetoothGranted
                else -> wifiPermissionState.status.isGranted
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp, vertical = 64.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape)
                        .background(rationale.color.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = rationale.icon,
                        contentDescription = null,
                        tint = rationale.color,
                        modifier = Modifier.size(72.dp)
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))
                
                Text(
                    text = rationale.title,
                    color = DesignSystem.TextPrimary,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = rationale.description,
                    color = DesignSystem.TextSecondary,
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp,
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(64.dp))

                if (isGranted) {
                    GlassmorphicCard(
                        borderColor = DesignSystem.Success.copy(alpha = 0.5f),
                        backgroundColor = DesignSystem.Success.copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, null, tint = DesignSystem.Success)
                            Spacer(Modifier.width(8.dp))
                            Text("Permission Granted", color = DesignSystem.Success, fontWeight = FontWeight.Bold)
                        }
                    }
                } else if (explicitlyDenied[rationale.id] == true) {
                    GlassmorphicCard(
                        borderColor = DesignSystem.Danger.copy(alpha = 0.5f),
                        backgroundColor = DesignSystem.Danger.copy(alpha = 0.1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Cancel, null, tint = DesignSystem.Danger)
                                Spacer(Modifier.width(8.dp))
                                Text("Permission Denied", color = DesignSystem.Danger, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    explicitlyDenied[rationale.id] = false
                                    requestPermission(rationale.id, locationPermissionState, cameraPermissionState, notificationPermissionState, wifiPermissionState, bluetoothPermissionsState, context)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DesignSystem.Danger)
                            ) {
                                Text("Retry", color = Color.White)
                            }
                        }
                    }
                } else {
                    Button(
                        onClick = {
                            requestPermission(rationale.id, locationPermissionState, cameraPermissionState, notificationPermissionState, wifiPermissionState, bluetoothPermissionsState, context)
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = rationale.color),
                        shape = RoundedCornerShape(DesignSystem.CornerRadius)
                    ) {
                        Text("Allow Access", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(onClick = { explicitlyDenied[rationale.id] = true }) {
                        Text("Not Now", color = DesignSystem.TextMuted)
                    }
                }
            }
        }

        // Pager Indicators & Navigation
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(rationales.size) { iteration ->
                    val color = if (pagerState.currentPage == iteration) DesignSystem.Cyan else DesignSystem.Border
                    val width = if (pagerState.currentPage == iteration) 24.dp else 8.dp
                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(width)
                            .clip(CircleShape)
                            .background(color)
                    )
                }
            }

            if (pagerState.currentPage < rationales.size - 1) {
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    },
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(DesignSystem.Surface)
                        .border(1.dp, DesignSystem.Border, CircleShape)
                ) {
                    Icon(Icons.Default.ArrowForward, contentDescription = "Next", tint = Color.White)
                }
            } else {
                Button(
                    onClick = { onPermissionsGranted() },
                    modifier = Modifier.height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (allGranted) DesignSystem.Cyan else DesignSystem.Surface,
                        contentColor = if (allGranted) Color.Black else Color.White
                    ),
                    shape = RoundedCornerShape(28.dp),
                    border = if (!allGranted) BorderStroke(1.dp, DesignSystem.Border) else null
                ) {
                    Text(
                        text = if (allGranted) "Finish Setup" else "Skip & Finish",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
private fun requestPermission(
    id: String,
    locationPermissionState: PermissionState,
    cameraPermissionState: PermissionState,
    notificationPermissionState: PermissionState?,
    wifiPermissionState: PermissionState,
    bluetoothPermissionsState: MultiplePermissionsState?,
    context: android.content.Context
) {
    when(id) {
        "location" -> locationPermissionState.launchPermissionRequest()
        "camera" -> cameraPermissionState.launchPermissionRequest()
        "notifications" -> notificationPermissionState?.launchPermissionRequest()
        "dnd" -> context.startActivity(android.content.Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
        "bluetooth" -> if (android.os.Build.VERSION.SDK_INT >= 31) bluetoothPermissionsState?.launchMultiplePermissionRequest()
        else -> wifiPermissionState.launchPermissionRequest()
    }
}
