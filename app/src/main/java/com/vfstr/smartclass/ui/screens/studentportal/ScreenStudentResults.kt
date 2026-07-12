package com.vfstr.smartclass.ui.screens.studentportal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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

@Composable
fun ScreenStudentResults(
    vm: MainViewModel,
    modifier: Modifier = Modifier
) {
    val results by vm.semesterResults.collectAsState()

    LaunchedEffect(Unit) {
        vm.loadSemesterResults()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(DesignSystem.Padding)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Academic Grade Sheets", style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold))

        if (results.isEmpty()) {
            EmptyStatePlaceholder(msg = "Loading academic records and grade templates...")
        } else {
            // Total credits earned summary
            val totalCredits = results.flatMap { it.subjects }.sumOf { it.credits }
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Total Credits Earned", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("R22 Regulation Requirement: 160 Credits", color = DesignSystem.TextMuted, fontSize = 10.sp)
                    }
                    Text(
                        text = "$totalCredits / 160",
                        color = if (totalCredits >= 160) DesignSystem.Success else DesignSystem.Cyan,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
            results.forEach { res ->
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
                                Text(res.semester, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("GPA: ${res.sgpa} • Cumulative: ${res.cgpa}", color = DesignSystem.Cyan, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Text(
                                text = if (expanded) "COLLAPSE" else "EXPAND",
                                color = DesignSystem.TextSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (expanded) {
                            Spacer(Modifier.height(12.dp))
                            HorizontalDivider(color = DesignSystem.Border)
                            Spacer(Modifier.height(8.dp))

                            res.subjects.forEach { sub ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(sub.subject_name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text("${sub.subject_code} • Credits: ${sub.credits}", color = DesignSystem.TextMuted, fontSize = 10.sp)
                                    }
                                    
                                    val badgeColor = when (sub.grade.uppercase()) {
                                        "O", "A+", "A" -> DesignSystem.Success
                                        "B+", "B" -> DesignSystem.Cyan
                                        "C" -> DesignSystem.Warning
                                        else -> DesignSystem.Danger
                                    }
                                    
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(badgeColor.copy(alpha = 0.1f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(sub.grade, color = badgeColor, fontSize = 10.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
