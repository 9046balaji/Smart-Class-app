package com.vfstr.smartclass.ui.screens.enrollment

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vfstr.smartclass.domain.models.*
import com.vfstr.smartclass.ui.components.GlassmorphicCard
import com.vfstr.smartclass.ui.components.MeshBackground
import com.vfstr.smartclass.ui.components.*
import com.vfstr.smartclass.ui.theme.DesignSystem

@Composable
fun ScreenEnrollmentAdminDashboard(
    vm: EnrollmentAdminViewModel,
    onNavigateToReview: (String) -> Unit,
    onNavigateToBulk: () -> Unit
) {
    val stats by vm.stats.collectAsState()
    val pendingRequests by vm.pendingRequests.collectAsState()
    val isLoading by vm.isLoading.collectAsState()
    
    val tilt = rememberTiltState()

    LaunchedEffect(Unit) {
        vm.loadDashboard()
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToBulk, containerColor = DesignSystem.Cyan) {
                Icon(Icons.Default.CloudUpload, null)
            }
        },
        containerColor = DesignSystem.Background
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            MeshBackground()

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(DesignSystem.Padding),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    Text(
                        "Enrollment Overview",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                    )
                }

                item {
                    stats?.let { AdminStatsGrid(it, tilt) } ?: Box(Modifier.height(100.dp).fillMaxWidth())
                }

                item {
                    Text("Recent Pending Applications", fontWeight = FontWeight.Bold, color = DesignSystem.TextSecondary)
                }

                if (pendingRequests.isEmpty() && !isLoading) {
                    item {
                        Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                            Text("All caught up! No pending applications.", color = DesignSystem.TextMuted)
                        }
                    }
                }

                items(pendingRequests.size) { index ->
                    val item = pendingRequests[index]
                    StaggeredSpringEntry(index = index) {
                        ApplicationCard(item, onNavigateToReview)
                    }
                }
            }

            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = DesignSystem.Cyan)
            }
        }
    }
}

@Composable
fun AdminStatsGrid(stats: DashboardStats, tilt: Pair<Float, Float>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Total App", stats.totalStudents, DesignSystem.Cyan, tilt, Modifier.weight(1f))
            StatCard("Pending", stats.branches.sumOf { it.count }, DesignSystem.Warning, tilt, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Approved", stats.activeStudents, DesignSystem.Success, tilt, Modifier.weight(1f))
            StatCard("Rejected", stats.inactiveStudents, DesignSystem.Danger, tilt, Modifier.weight(1f))
        }
    }
}

@Composable
fun StatCard(label: String, value: Int, color: Color, tilt: Pair<Float, Float>, modifier: Modifier) {
    GlassmorphicCard(
        modifier = modifier.tiltEffect(tilt.first, tilt.second),
        glowColor = color.copy(alpha = 0.1f)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(label, color = DesignSystem.TextSecondary, fontSize = 11.sp)
            AnimateNumberCounter(
                targetValue = value,
                style = MaterialTheme.typography.headlineMedium.copy(color = color, fontWeight = FontWeight.ExtraBold)
            )
        }
    }
}

@Composable
fun ApplicationCard(item: StudentListItem, onReview: (String) -> Unit) {
    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).clip(CircleShape).background(DesignSystem.Surface), contentAlignment = Alignment.Center) {
                Text(item.name.take(1), color = DesignSystem.Cyan, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(item.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text("${item.branch} • ${item.year}", color = DesignSystem.TextSecondary, fontSize = 12.sp)
                Text("Submitted: ${item.submittedAt}", color = DesignSystem.TextMuted, fontSize = 10.sp)
            }
            Button(
                onClick = { onReview(item.id) },
                colors = ButtonDefaults.buttonColors(containerColor = DesignSystem.Cyan.copy(alpha = 0.2f), contentColor = DesignSystem.Cyan),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Review", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
