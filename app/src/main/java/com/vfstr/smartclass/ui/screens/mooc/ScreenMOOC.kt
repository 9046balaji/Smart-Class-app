package com.vfstr.smartclass.ui.screens.mooc

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vfstr.smartclass.domain.models.MOOCEnrollment
import com.vfstr.smartclass.ui.MainViewModel
import com.vfstr.smartclass.ui.components.GlassmorphicCard
import com.vfstr.smartclass.ui.theme.DesignSystem

import com.vfstr.smartclass.ui.components.StickyDataTable
import androidx.compose.ui.unit.dp

@Composable
fun ScreenMOOC(
    vm: MainViewModel,
    modifier: Modifier = Modifier
) {
    val enrollments by vm.students.collectAsState() // Simplified binding
    var activeTab by remember { mutableStateOf("Enrollments") } // Enrollments, Analytics

    LaunchedEffect(Unit) {
        vm.loadStudents()
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
                text = "MOOC Tracking System",
                style = MaterialTheme.typography.headlineSmall.copy(color = DesignSystem.TextPrimary, fontWeight = FontWeight.Bold)
            )
            Text(text = "External certifications monitoring", color = DesignSystem.TextSecondary, style = MaterialTheme.typography.bodySmall)
            
            Spacer(modifier = Modifier.height(20.dp))

            // Tab Row
            Row(modifier = Modifier.fillMaxWidth().height(44.dp).clip(RoundedCornerShape(10.dp)).background(DesignSystem.Surface).padding(4.dp)) {
                listOf("Enrollments", "Analytics").forEach { tab ->
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
                        Text(tab, color = if (isSel) DesignSystem.Cyan else DesignSystem.TextSecondary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (activeTab == "Enrollments") {
                // Rule 15: Sticky Data Table for MOOC Enrollments
                StickyDataTable(
                    headers = listOf("Roll No", "Student Name", "Course", "Platform", "Credits", "Status"),
                    rows = enrollments.map { 
                        listOf(it.rollNo, it.name, "Advanced AI", "NPTEL", "4", "ENROLLED")
                    },
                    columnWidths = listOf(100.dp, 150.dp, 150.dp, 100.dp, 80.dp, 100.dp),
                    stickyColumns = 2,
                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(DesignSystem.CornerRadius)).border(1.dp, DesignSystem.Border, RoundedCornerShape(DesignSystem.CornerRadius))
                )
            } else {
                MOOCAnalytics()
            }
        }
    }
}

@Composable
fun EnrollmentsList() {
    val enrollments = (1..10).map { 
        MOOCEnrollment(it, "22L11A050$it", "Student Name $it", "CSAI", 3, "A", "Compiler Core", "swayam", 4, "completed", "2024-05-15", null, "2024-01-10")
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(enrollments) { item ->
            GlassmorphicCard {
                Row(Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    PlatformBadge(item.platform)
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text(item.courseName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("${item.studentName} (${item.studentId})", color = DesignSystem.TextSecondary, fontSize = 11.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("${item.credits} CR", color = DesignSystem.Cyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(DesignSystem.Success.copy(alpha = 0.1f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(item.status.uppercase(), fontSize = 9.sp, color = DesignSystem.Success, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlatformBadge(platform: String) {
    val color = when (platform.lowercase()) {
        "swayam" -> DesignSystem.PlatformSwayam
        "nptel" -> DesignSystem.PlatformNptel
        "coursera" -> DesignSystem.PlatformCoursera
        else -> DesignSystem.PlatformOther
    }
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.3f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Default.Book, null, tint = color, modifier = Modifier.size(20.dp))
    }
}

@Composable
fun MOOCAnalytics() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Total", "284", DesignSystem.Cyan, Modifier.weight(1f))
            StatCard("Credits", "842", DesignSystem.Success, Modifier.weight(1f))
        }
        
        GlassmorphicCard {
            Column(Modifier.padding(16.dp)) {
                Text("Platform Distribution", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(Modifier.height(16.dp))
                com.vfstr.smartclass.ui.components.AnimatedDonutChart(
                    data = listOf("Swayam" to 142f, "NPTEL" to 88f, "Coursera" to 54f),
                    colors = listOf(DesignSystem.PlatformSwayam, DesignSystem.PlatformNptel, DesignSystem.PlatformCoursera),
                    modifier = Modifier.size(140.dp).align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String, color: Color, modifier: Modifier) {
    GlassmorphicCard(modifier = modifier) {
        Column(Modifier.padding(16.dp)) {
            Text(label, color = DesignSystem.TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(value, color = color, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}
