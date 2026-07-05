package com.vfstr.smartclass.ui.screens.devices

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vfstr.smartclass.ui.MainViewModel
import com.vfstr.smartclass.ui.components.*
import com.vfstr.smartclass.ui.theme.DesignSystem

@Composable
fun ScreenDevices(
    vm: MainViewModel,
    modifier: Modifier = Modifier
) {
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
                "Hardware Infrastructure",
                style = MaterialTheme.typography.headlineSmall.copy(color = DesignSystem.TextPrimary, fontWeight = FontWeight.Bold)
            )
            Text("Manage edge devices and face scanners", color = DesignSystem.TextSecondary, style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = Modifier.height(24.dp))

            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(DesignSystem.PaddingLarge), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Online Devices", color = DesignSystem.TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("14", color = DesignSystem.Success, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
                            Text(" / 15", color = DesignSystem.TextMuted, fontSize = 16.sp, modifier = Modifier.padding(bottom = 6.dp))
                        }
                    }
                    RadialGauge(percentage = 93f, statusText = "Health", strokeColor = DesignSystem.Success, size = 70.dp, strokeWidth = 6.dp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Edge Device Status Grid", color = DesignSystem.TextPrimary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(listOf(
                    Triple("Scanner A1", "Online", "192.168.1.105"),
                    Triple("Scanner A2", "Online", "192.168.1.106"),
                    Triple("Thermal T1", "Offline", "192.168.1.201"),
                    Triple("Scanner C1", "Online", "192.168.1.42"),
                    Triple("Scanner D2", "Online", "192.168.1.88"),
                    Triple("Controller G1", "Online", "192.168.1.10")
                )) { d ->
                    DeviceStatusCard(name = d.first, status = d.second, ip = d.third)
                }
            }
        }
    }
}

@Composable
fun DeviceStatusCard(name: String, status: String, ip: String) {
    val isOnline = status == "Online"
    val color = if (isOnline) DesignSystem.Success else DesignSystem.Danger

    GlassmorphicCard {
        Column(modifier = Modifier.padding(DesignSystem.Padding)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
                Spacer(modifier = Modifier.width(8.dp))
                Text(status.uppercase(), color = color, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(ip, color = DesignSystem.TextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { if (isOnline) 0.95f else 0f },
                modifier = Modifier.fillMaxWidth().height(2.dp).clip(CircleShape),
                color = color.copy(alpha = 0.5f),
                trackColor = DesignSystem.Border
            )
        }
    }
}
