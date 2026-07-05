package com.vfstr.smartclass.ui.screens.studentreports

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vfstr.smartclass.ui.MainViewModel
import com.vfstr.smartclass.ui.components.GlassmorphicCard
import com.vfstr.smartclass.ui.theme.DesignSystem

data class ReportType(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
fun ScreenStudentReports(
    vm: MainViewModel,
    modifier: Modifier = Modifier
) {
    val reports = listOf(
        ReportType("enrollment", "Enrollment", "Biometric and enrollment details", Icons.Default.Fingerprint, Color(0xFF3B82F6)),
        ReportType("branch", "Branch-wise", "Breakdown by department", Icons.Default.AccountTree, DesignSystem.Success),
        ReportType("year", "Year-wise", "Academic distribution", Icons.Default.Timeline, DesignSystem.Violet),
        ReportType("section", "Section-wise", "Section capacity analysis", Icons.Default.PieChart, Color(0xFFF97316)),
        ReportType("summary", "Summary", "Comprehensive overview", Icons.Default.Description, DesignSystem.Cyan),
        ReportType("attendance", "Attendance", "Patterns and statistics", Icons.Default.EventAvailable, DesignSystem.Danger)
    )

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
                text = "Academic Reports",
                style = MaterialTheme.typography.headlineSmall.copy(color = DesignSystem.TextPrimary, fontWeight = FontWeight.Bold)
            )
            Text(text = "Choose a report type to generate and export", color = DesignSystem.TextSecondary, style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = Modifier.height(24.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(reports) { report ->
                    ReportCard(report)
                }
            }
        }
    }
}

@Composable
fun ReportCard(report: ReportType) {
    GlassmorphicCard(
        modifier = Modifier
            .height(180.dp)
            .clickable { /* Detail View */ }
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(report.color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(report.icon, contentDescription = null, tint = report.color, modifier = Modifier.size(24.dp))
            }
            
            Column {
                Text(text = report.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(Modifier.height(4.dp))
                Text(text = report.description, color = DesignSystem.TextSecondary, fontSize = 11.sp, maxLines = 2, lineHeight = 14.sp)
            }
        }
    }
}
