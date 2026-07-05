package com.vfstr.smartclass.ui.screens.studentanalytics

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.vfstr.smartclass.domain.models.Student
import com.vfstr.smartclass.ui.MainViewModel
import com.vfstr.smartclass.ui.components.*
import com.vfstr.smartclass.ui.theme.DesignSystem

@Composable
fun ScreenStudentAnalytics(
    vm: MainViewModel,
    modifier: Modifier = Modifier
) {
    val students by vm.students.collectAsState()
    var selectedStudent by remember { mutableStateOf<Student?>(null) }
    var searchQuery by remember { mutableStateOf("") }

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
                text = "Performance Tracking",
                style = MaterialTheme.typography.headlineSmall.copy(color = DesignSystem.TextPrimary, fontWeight = FontWeight.Bold)
            )
            Text(text = "Monitor individual student academic progression", color = DesignSystem.TextSecondary, style = MaterialTheme.typography.bodySmall)
            
            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Filter by name or roll number...", color = DesignSystem.TextMuted) },
                leadingIcon = { Icon(Icons.Default.FilterList, null, tint = DesignSystem.Cyan) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DesignSystem.Cyan, unfocusedBorderColor = DesignSystem.Border)
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                items(students.filter { it.name.contains(searchQuery, true) || it.rollNo.contains(searchQuery, true) }) { student ->
                    GlassmorphicCard(
                        modifier = Modifier.clickable { selectedStudent = student }
                    ) {
                        Row(Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(40.dp).clip(CircleShape).background(DesignSystem.Surface), contentAlignment = Alignment.Center) {
                                Text(student.name.take(1), color = DesignSystem.Cyan, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.width(16.dp))
                            Column(Modifier.weight(1f)) {
                                Text(student.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(student.rollNo, fontSize = 11.sp, color = DesignSystem.TextSecondary)
                            }
                            RadialGauge(percentage = (70..95).random().toFloat(), statusText = "", strokeColor = DesignSystem.Cyan, size = 48.dp, strokeWidth = 4.dp)
                        }
                    }
                }
            }
        }
    }

    if (selectedStudent != null) {
        StudentAnalyticsModal(student = selectedStudent!!, onDismiss = { selectedStudent = null })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentAnalyticsModal(student: Student, onDismiss: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = DesignSystem.Surface,
        contentColor = DesignSystem.TextPrimary
    ) {
        Column(modifier = Modifier.padding(DesignSystem.PaddingLarge).fillMaxHeight(0.9f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(24.dp)) {
            // Profile Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(64.dp).clip(CircleShape).background(DesignSystem.Background), contentAlignment = Alignment.Center) {
                    Text(student.name.take(1), fontSize = 24.sp, color = DesignSystem.Cyan, fontWeight = FontWeight.ExtraBold)
                }
                Spacer(Modifier.width(20.dp))
                Column {
                    Text(student.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(student.rollNo + " • " + student.department, color = DesignSystem.TextSecondary)
                }
            }

            // Key Metrics
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    RadialGauge(percentage = 84f, statusText = "Attendance", strokeColor = DesignSystem.Cyan)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    RadialGauge(percentage = 78f, statusText = "CGPA", strokeColor = DesignSystem.Violet)
                }
            }

            HorizontalDivider(color = DesignSystem.Border)

            // Subject Comparison
            Text("Subject-wise Distribution", fontWeight = FontWeight.Bold, color = Color.White)
            AnimatedBarChart(
                data = listOf(82f, 75f, 91f, 88f, 68f),
                colors = listOf(DesignSystem.Cyan, DesignSystem.Violet, DesignSystem.Success, DesignSystem.Warning, DesignSystem.Danger),
                modifier = Modifier.height(160.dp)
            )

            // Heatmap Summary
            Text("Activity Pattern (Last 30 Days)", fontWeight = FontWeight.Bold, color = Color.White)
            ActivityHeatmap()

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun ActivityHeatmap() {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(5) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(15) {
                    val alpha = (1..10).random() / 10f
                    Box(Modifier.size(14.dp).clip(RoundedCornerShape(3.dp)).background(DesignSystem.Success.copy(alpha = alpha)))
                }
            }
        }
    }
}
