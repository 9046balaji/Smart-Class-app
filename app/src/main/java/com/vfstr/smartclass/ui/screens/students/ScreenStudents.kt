package com.vfstr.smartclass.ui.screens.students

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
import com.vfstr.smartclass.domain.models.Student
import com.vfstr.smartclass.ui.MainViewModel
import com.vfstr.smartclass.ui.components.GlassmorphicCard
import com.vfstr.smartclass.ui.theme.DesignSystem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenStudents(
    vm: MainViewModel,
    modifier: Modifier = Modifier
) {
    val studentList by vm.students.collectAsState()
    val listLoading by vm.studentsLoading.collectAsState()
    val previewList by vm.studentPreviewList.collectAsState()
    
    var searchKey by remember { mutableStateOf("") }
    var showImportSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        vm.loadStudents()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DesignSystem.Background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showImportSheet = true },
                containerColor = DesignSystem.Cyan,
                contentColor = Color.Black
            ) {
                Icon(Icons.Default.FileUpload, contentDescription = "Import")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(DesignSystem.Padding)
        ) {
            Text(
                "Students Directory",
                style = MaterialTheme.typography.titleLarge.copy(color = DesignSystem.TextPrimary, fontWeight = FontWeight.Bold)
            )
            Text("Manage enrollment and biometric status", color = DesignSystem.TextSecondary, style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = searchKey,
                onValueChange = { searchKey = it },
                label = { Text("Search by name/roll") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = DesignSystem.TextSecondary) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DesignSystem.Cyan,
                    unfocusedBorderColor = DesignSystem.Border
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (listLoading && studentList.isEmpty()) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(5) {
                        com.vfstr.smartclass.ui.components.ShimmerSkeleton(modifier = Modifier.fillMaxWidth().height(80.dp))
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    val filtered = studentList.filter {
                        it.name.contains(searchKey, ignoreCase = true) || it.rollNo.contains(searchKey, ignoreCase = true)
                    }

                    items(filtered) { st ->
                        StudentItemRow(st, onEnroll = { vm.faceEnrollmentAction(st.rollNo) })
                    }
                }
            }
        }

        if (showImportSheet) {
            ModalBottomSheet(
                onDismissRequest = { showImportSheet = false },
                containerColor = DesignSystem.Surface
            ) {
                Column(modifier = Modifier.padding(DesignSystem.PaddingLarge).fillMaxHeight(0.7f)) {
                    Text("Import Students from Excel", style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold))
                    Text("Select a compatible spreadsheet (.xlsx)", color = DesignSystem.TextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    if (previewList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .border(2.dp, DesignSystem.Border, RoundedCornerShape(DesignSystem.CornerRadius))
                                .clickable { 
                                    // Simulated picking
                                    vm.studentPreviewList.value = listOf(
                                        Student("", "22L11A0511", "Abhishek Naidu", "22L11A0511", "CSAI", "1", "Sec A", null, null, false, null, 0, true, null, false, true, null, 0.0),
                                        Student("", "22L11A0512", "Bhavana Rao", "22L11A0512", "CSAI", "1", "Sec A", null, null, false, null, 0, true, null, false, true, null, 0.0)
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Description, contentDescription = null, tint = DesignSystem.TextMuted, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Tap to select Excel file", color = DesignSystem.TextSecondary)
                            }
                        }
                    } else {
                        Text("Preview Records (${previewList.size})", color = DesignSystem.Cyan, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(10.dp))
                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(previewList) { p ->
                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(32.dp).clip(CircleShape).background(DesignSystem.CardBg), contentAlignment = Alignment.Center) {
                                        Text(p.name.take(1), color = DesignSystem.Cyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(p.name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        Text(p.rollNo, color = DesignSystem.TextSecondary, fontSize = 11.sp)
                                    }
                                }
                                HorizontalDivider(color = DesignSystem.Border)
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = {
                                vm.confirmImportedStudents()
                                showImportSheet = false
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DesignSystem.Success),
                            shape = RoundedCornerShape(DesignSystem.CornerRadius)
                        ) {
                            Text("Confirm & Register All", fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StudentItemRow(st: Student, onEnroll: () -> Unit) {
    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(DesignSystem.Padding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(DesignSystem.Violet.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(st.name.take(2).uppercase(), color = DesignSystem.Violet, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(st.name, fontWeight = FontWeight.Bold, color = DesignSystem.TextPrimary, fontSize = 14.sp)
                Text("Roll: ${st.rollNo} • ${st.department}", fontSize = 11.sp, color = DesignSystem.TextSecondary)
            }

            if (st.faceEnrolled) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(DesignSystem.Success.copy(alpha = 0.1f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("ENROLLED", color = DesignSystem.Success, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                }
            } else {
                Button(
                    onClick = onEnroll,
                    colors = ButtonDefaults.buttonColors(containerColor = DesignSystem.Danger.copy(alpha = 0.15f), contentColor = DesignSystem.Danger),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    modifier = Modifier.height(32.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("ENROLL", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}
