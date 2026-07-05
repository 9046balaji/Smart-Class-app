package com.vfstr.smartclass.ui.screens.overview

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vfstr.smartclass.data.local.db.CachedStatsEntity
import com.vfstr.smartclass.domain.models.AttendanceEvent
import com.vfstr.smartclass.ui.MainViewModel
import com.vfstr.smartclass.ui.components.*
import com.vfstr.smartclass.ui.navigation.Navigation
import com.vfstr.smartclass.ui.screens.EmptyStatePlaceholder
import com.vfstr.smartclass.ui.theme.DesignSystem
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun ScreenOverview(
    vm: MainViewModel,
    onNav: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val stats by vm.dashboardStats.collectAsState()
    val statsLoadingVal by vm.statsLoading.collectAsState()
    val errVal by vm.statsError.collectAsState()
    val liveEvents by vm.attendanceEvents.collectAsState()
    val userName by vm.currentUserName.collectAsState()
    
    val geoTier1 by vm.geoTier1.collectAsState()
    val geoTier2 by vm.geoTier2.collectAsState()
    val geoTier3 by vm.geoTier3.collectAsState()
    val geoMessage by vm.geoMessage.collectAsState()

    val currentDate = remember {
        LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy"))
    }
    
    val tilt = rememberTiltState()

    LaunchedEffect(Unit) {
        vm.loadDashboardStats()
        vm.loadAttendanceEvents()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DesignSystem.Background
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            // Combined Mesh Background
            MeshBackground(modifier = Modifier.fillMaxSize())

            if (statsLoadingVal && stats == null) {
                OverviewShimmer()
            } else if (errVal != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    ErrorCard(message = errVal ?: "Error", onRetry = { vm.loadDashboardStats() })
                }
            } else {
                val resolvedStats = stats ?: CachedStatsEntity(
                    presentCount = 0, totalCount = 0, activeSessions = 0, eventsCount = 0,
                    onlineDevices = 0, totalDevices = 0, offlineAlerts = 0, reviewWeek = 1,
                    todayPercentage = 0f, weeklyPercentage = 0f, monthlyPercentage = 0f
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(DesignSystem.PaddingLarge),
                    verticalArrangement = Arrangement.spacedBy(DesignSystem.SpacingLarge)
                ) {
                    item {
                        HeaderSection(currentDate, userName, resolvedStats.reviewWeek, geoTier1, geoTier2, geoTier3, geoMessage)
                    }

                    item {
                        MetricsSection(resolvedStats, tilt)
                    }

                    item {
                        GaugesSection(resolvedStats)
                    }

                    item {
                        ActivityFeedSection(liveEvents, onNav)
                    }
                }
            }
        }
    }
}

@Composable
fun HeaderSection(date: String, userName: String, week: Int, tier1: Boolean, tier2: Boolean, tier3: Boolean, message: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = date, style = MaterialTheme.typography.bodySmall.copy(color = DesignSystem.TextSecondary, fontWeight = FontWeight.Bold))
            Text(
                text = "Welcome back, $userName",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = DesignSystem.TextPrimary,
                    fontWeight = FontWeight.ExtraBold
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            ReviewWeekBadge(week = week, isActive = true)
        }
        
        GeofenceRadar(
            tier1 = tier1,
            tier2 = tier2,
            tier3 = tier3,
            message = message,
            size = 80.dp
        )
    }
}

@Composable
fun ReviewWeekBadge(week: Int, isActive: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(DesignSystem.CornerRadius))
            .background(if (isActive) DesignSystem.Warning.copy(alpha = 0.15f) else DesignSystem.Surface)
            .border(
                1.dp,
                if (isActive) DesignSystem.Warning.copy(alpha = 0.4f) else DesignSystem.Border,
                RoundedCornerShape(DesignSystem.CornerRadius)
            )
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Week $week",
            style = MaterialTheme.typography.labelMedium.copy(
                color = if (isActive) DesignSystem.Warning else DesignSystem.TextSecondary,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
            )
        )
    }
}

@Composable
fun MetricsSection(stats: CachedStatsEntity, tilt: Pair<Float, Float>) {
    Column(verticalArrangement = Arrangement.spacedBy(DesignSystem.SpacingLarge)) {
        // Row 1
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(DesignSystem.SpacingLarge)) {
            MetricCard(
                title = "Present Today",
                value = "${stats.presentCount}",
                icon = Icons.Default.Groups,
                tint = DesignSystem.Cyan,
                glowColor = DesignSystem.Cyan.copy(alpha = 0.15f),
                tilt = tilt,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Events Today",
                value = "${stats.eventsCount}",
                icon = Icons.Default.History,
                tint = DesignSystem.Violet,
                glowColor = DesignSystem.Violet.copy(alpha = 0.15f),
                tilt = tilt,
                modifier = Modifier.weight(1f)
            )
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(DesignSystem.SpacingLarge)) {
            MetricCard(
                title = "Active Sessions",
                value = "${stats.activeSessions}",
                icon = Icons.Default.CastConnected,
                tint = DesignSystem.Success,
                glowColor = DesignSystem.Success.copy(alpha = 0.15f),
                tilt = tilt,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Online Devices",
                value = "${stats.onlineDevices}/${stats.totalDevices}",
                icon = Icons.Default.Sensors,
                tint = DesignSystem.Cyan,
                glowColor = DesignSystem.Cyan.copy(alpha = 0.15f),
                tilt = tilt,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun GaugesSection(stats: CachedStatsEntity) {
    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(DesignSystem.PaddingLarge).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Attendance Overview",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = DesignSystem.TextPrimary,
                        fontWeight = FontWeight.ExtraBold
                    )
                )
                Text(
                    text = "Real-time rates across VFSTR",
                    style = MaterialTheme.typography.bodySmall.copy(color = DesignSystem.TextSecondary, fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(20.dp))
                GaugeLegend("Today", DesignSystem.Cyan, stats.todayPercentage)
                GaugeLegend("Weekly", DesignSystem.Violet, stats.weeklyPercentage)
                GaugeLegend("Monthly", DesignSystem.Success, stats.monthlyPercentage)
            }
            
            ConcentricRadialGauges(
                today = stats.todayPercentage,
                weekly = stats.weeklyPercentage,
                monthly = stats.monthlyPercentage,
                size = 140.dp
            )
        }
    }
}

@Composable
fun GaugeLegend(label: String, color: Color, percentage: Float) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color).shadow(elevation = 4.dp, shape = CircleShape, spotColor = color))
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = label, style = MaterialTheme.typography.bodySmall.copy(color = DesignSystem.TextSecondary, fontWeight = FontWeight.Bold), modifier = Modifier.width(70.dp))
        Text(text = "${percentage.toInt()}%", style = MaterialTheme.typography.bodySmall.copy(color = DesignSystem.TextPrimary, fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Monospace))
    }
}

@Composable
fun ActivityFeedSection(events: List<AttendanceEvent>, onNav: (String) -> Unit) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = DesignSystem.SpacingMedium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(DesignSystem.Danger).shadow(elevation = 8.dp, shape = CircleShape, spotColor = DesignSystem.Danger))
                Spacer(modifier = Modifier.width(DesignSystem.SpacingSmall))
                Text(
                    text = "LIVE Activity Feed",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = DesignSystem.TextPrimary,
                        fontWeight = FontWeight.ExtraBold
                    )
                )
            }
            Text(
                text = "View All",
                style = MaterialTheme.typography.labelLarge.copy(color = DesignSystem.Cyan, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp),
                modifier = Modifier.clickable { onNav(Navigation.ROUTE_ATTENDANCE) }
            )
        }

        if (events.isEmpty()) {
            EmptyStatePlaceholder(msg = "Listening to live incoming face scans...")
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                events.take(5).forEachIndexed { index, ev ->
                    StaggeredSpringEntry(index = index) {
                        ActivityFeedRow(event = ev)
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
    glowColor: Color = Color.Transparent,
    tilt: Pair<Float, Float> = 0f to 0f
) {
    GlassmorphicCard(
        modifier = modifier
            .height(110.dp)
            .tiltEffect(tilt.first, tilt.second),
        borderColor = if (glowColor != Color.Transparent) tint.copy(alpha = 0.4f) else DesignSystem.Border,
        glowColor = glowColor
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(DesignSystem.Padding),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = title.uppercase(), style = MaterialTheme.typography.labelSmall.copy(color = DesignSystem.TextSecondary, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp))
                Box(
                    modifier = Modifier.size(28.dp).clip(RoundedCornerShape(8.dp)).background(tint.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
                }
            }
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = DesignSystem.TextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = (-1).sp
                )
            )
        }
    }
}

@Composable
fun ActivityFeedRow(event: AttendanceEvent) {
    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth().height(68.dp),
        shape = RoundedCornerShape(DesignSystem.CornerRadius)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = DesignSystem.Padding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(DesignSystem.Surface),
                contentAlignment = Alignment.Center
            ) {
                val initials = (event.studentName ?: "").split(" ").mapNotNull { it.firstOrNull() }.joinToString("")
                Text(
                    text = initials.take(2).ifEmpty { "ST" },
                    style = MaterialTheme.typography.labelMedium.copy(color = DesignSystem.Cyan, fontWeight = FontWeight.ExtraBold)
                )
            }

            Spacer(modifier = Modifier.width(DesignSystem.SpacingLarge))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = event.studentName ?: event.rollNo, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold, color = DesignSystem.TextPrimary))
                Text(text = "${event.room} • ${event.section}", style = MaterialTheme.typography.labelSmall.copy(color = DesignSystem.TextSecondary, fontWeight = FontWeight.Bold))
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${(event.confidence * 100).toInt()}% Match",
                    style = MaterialTheme.typography.labelSmall.copy(color = DesignSystem.Success, fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Monospace)
                )
                Text(text = event.timestamp, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, color = DesignSystem.TextMuted, fontWeight = FontWeight.Bold))
            }
        }
    }
}

@Composable
fun OverviewShimmer() {
    Column(modifier = Modifier.fillMaxSize().padding(DesignSystem.PaddingLarge), verticalArrangement = Arrangement.spacedBy(DesignSystem.SpacingLarge)) {
        ShimmerSkeleton(modifier = Modifier.fillMaxWidth().height(64.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(DesignSystem.SpacingLarge)) {
            ShimmerSkeleton(modifier = Modifier.weight(1f).height(110.dp))
            ShimmerSkeleton(modifier = Modifier.weight(1f).height(110.dp))
        }
        ShimmerSkeleton(modifier = Modifier.fillMaxWidth().height(160.dp))
        Column(verticalArrangement = Arrangement.spacedBy(DesignSystem.SpacingMedium)) {
            repeat(4) { ShimmerSkeleton(modifier = Modifier.fillMaxWidth().height(68.dp)) }
        }
    }
}
