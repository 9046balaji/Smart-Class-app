package com.vfstr.smartclass.ui.screens.studentportal

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.vfstr.smartclass.ui.components.GlassmorphicCard
import com.vfstr.smartclass.ui.screens.EmptyStatePlaceholder
import com.vfstr.smartclass.ui.theme.DesignSystem
import java.util.Locale

@Composable
fun ScreenStudentMarks(
    vm: MainViewModel,
    modifier: Modifier = Modifier
) {
    val marks by vm.studentMarks.collectAsState()
    val selectedSemester by vm.selectedMarksSemester.collectAsState()

    LaunchedEffect(selectedSemester) {
        vm.loadStudentMarks(selectedSemester)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(DesignSystem.Padding)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Internal Marks", style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold))

        // Semester selector tabs (dynamic SEM-1 through SEM-8)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            (8 downTo 1).map { "SEM-$it" }.forEach { sem ->
                val isSelected = selectedSemester == sem
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) DesignSystem.Cyan.copy(alpha = 0.15f) else Color.Transparent)
                        .border(androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) DesignSystem.Cyan else DesignSystem.Border), RoundedCornerShape(8.dp))
                        .clickable { vm.selectedMarksSemester.value = sem }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(sem, color = if (isSelected) DesignSystem.Cyan else Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (marks.isEmpty()) {
            EmptyStatePlaceholder(msg = "No marks registered for this semester.")
        } else {
            marks.forEach { item ->
                var expanded by remember { mutableStateOf(false) }
                
                GlassmorphicCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = !expanded }
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.subject_name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(item.subject_code, color = DesignSystem.TextSecondary, fontSize = 11.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${item.total_obtained ?: 0.0f} / ${item.total_max}",
                                    color = DesignSystem.Cyan,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text("Click to view details", color = DesignSystem.TextMuted, fontSize = 9.sp)
                            }
                        }

                        if (expanded) {
                            Spacer(Modifier.height(16.dp))
                            HorizontalDivider(color = DesignSystem.Border)
                            Spacer(Modifier.height(12.dp))

                            ComponentMarksBar("Mid-1 Exams", item.mid1, 10f, DesignSystem.Cyan)
                            Spacer(Modifier.height(8.dp))
                            ComponentMarksBar("Mid-2 Exams", item.mid2, 10f, DesignSystem.Violet)
                            Spacer(Modifier.height(8.dp))
                            ComponentMarksBar("Assignments", item.assignment, 5f, DesignSystem.Success)
                            Spacer(Modifier.height(8.dp))
                            ComponentMarksBar("Attendance score", item.attendance, 5f, DesignSystem.Warning)
                        }
                    }
                }
            }
        }

        // Total marks summary
        if (marks.isNotEmpty()) {
            val totalObtained = marks.sumOf { (it.total_obtained ?: 0f).toDouble() }
            val totalMax = marks.sumOf { it.total_max.toDouble() }
            val pct = if (totalMax > 0) (totalObtained / totalMax * 100) else 0.0
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total Internal Marks", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(
                         text = String.format(Locale.US, "%.1f / %.0f (%.1f%%)", totalObtained, totalMax, pct),
                         color = DesignSystem.Cyan,
                         fontWeight = FontWeight.Black,
                         fontSize = 14.sp,
                         fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
fun ComponentMarksBar(name: String, obtained: Float?, max: Float, color: Color) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(name, color = DesignSystem.TextSecondary, fontSize = 11.sp)
            val scoreText = if (obtained != null) "$obtained / $max" else "- / $max"
            Text(scoreText, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { if (obtained != null) (obtained / max) else 0.0f },
            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
            color = if (obtained != null) color else Color.Gray.copy(alpha = 0.3f),
            trackColor = DesignSystem.Border
        )
    }
}
