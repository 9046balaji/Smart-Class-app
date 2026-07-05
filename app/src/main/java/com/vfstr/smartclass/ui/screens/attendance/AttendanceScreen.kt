package com.vfstr.smartclass.ui.screens.attendance

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vfstr.smartclass.ui.theme.DesignSystem
import com.vfstr.smartclass.ui.components.*
import com.vfstr.smartclass.utils.getYearLabel
import com.vfstr.smartclass.utils.toScannerSectionId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    onNavigateToScanner: (String) -> Unit,
    onNavigateToLogs: () -> Unit,
    viewModel: AttendanceViewModel = hiltViewModel(),
) {
    val years by viewModel.years.collectAsState()
    val depts by viewModel.departments.collectAsState()
    val branches by viewModel.branches.collectAsState()
    val sections by viewModel.sections.collectAsState()
    val students by viewModel.filteredStudents.collectAsState()
    val selection by viewModel.selection.collectAsState()
    
    val selectedYear by viewModel.selectedYear.collectAsState()
    val selectedDept by viewModel.selectedDepartment.collectAsState()
    val selectedBranch by viewModel.selectedBranch.collectAsState()
    val selectedSection by viewModel.selectedSection.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val manualSessionId by viewModel.manualSessionId.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showExitConfirmation by remember { mutableStateOf(false) }
    var filtersExpanded by remember { mutableStateOf(true) }
    
    val hasModifiedSelection = selection.values.any { !it } // Default is true (Present)

    BackHandler(enabled = hasModifiedSelection) {
        showExitConfirmation = true
    }

    if (showExitConfirmation) {
        AlertDialog(
            onDismissRequest = { showExitConfirmation = false },
            containerColor = DesignSystem.Surface,
            title = { Text("Discard attendance changes?", color = Color.White) },
            text = { Text("You have modified the roster selection. Exiting will reset these changes.", color = DesignSystem.TextSecondary) },
            confirmButton = {
                TextButton(onClick = { 
                    showExitConfirmation = false
                    viewModel.markAll(present = true) // Reset
                }) {
                    Text("Discard", color = DesignSystem.Danger)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitConfirmation = false }) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }

    Scaffold(
        containerColor = DesignSystem.Background,
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Attendance Hub", color = DesignSystem.TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                        Text(
                            text = if (selectedBranch.isNotEmpty()) "Section: $selectedBranch-$selectedSection" else "Select Section",
                            color = DesignSystem.Cyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { filtersExpanded = !filtersExpanded }) {
                        Icon(
                            if (filtersExpanded) Icons.Default.Close else Icons.Default.FilterList,
                            contentDescription = "Toggle Filters",
                            tint = if (filtersExpanded) DesignSystem.Cyan else Color.White
                        )
                    }
                    IconButton(onClick = onNavigateToLogs) {
                        Icon(Icons.Default.History, contentDescription = null, tint = DesignSystem.Cyan)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DesignSystem.Surface)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            MeshBackground(modifier = Modifier.fillMaxSize())

            Column(modifier = Modifier.fillMaxSize()) {
                // Collapsible Selection Menu
                androidx.compose.animation.AnimatedVisibility(
                    visible = filtersExpanded,
                    enter = androidx.compose.animation.expandVertically() + androidx.compose.animation.fadeIn(),
                    exit = androidx.compose.animation.shrinkVertically() + androidx.compose.animation.fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DesignSystem.Surface.copy(alpha = 0.9f))
                            .border(width = 1.dp, color = DesignSystem.Border, shape = RectangleShape)
                            .padding(DesignSystem.Padding),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(modifier = Modifier.weight(1f)) {
                                SelectorDropdown("Admission Year", selectedYear, years.map { it to getYearLabel(it) }) { viewModel.selectYear(it) }
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                SelectorDropdown("Department", selectedDept, depts.map { it to (it ?: "N/A") }) { viewModel.selectDepartment(it) }
                            }
                        }
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(modifier = Modifier.weight(1f)) {
                                SelectorDropdown("Branch Code", selectedBranch, branches.map { it to it }) { viewModel.selectBranch(it) }
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                SelectorDropdown("Section Unit", selectedSection, sections.map { it to it }) { viewModel.selectSection(it) }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val sessions by viewModel.sessions.collectAsState()
                            val selectedSessionId by viewModel.selectedSessionId.collectAsState()
                            Box(modifier = Modifier.weight(1.5f)) {
                                SelectorDropdown("Active Session", selectedSessionId, sessions.map { it.id to (it.subject_name ?: it.subject ?: "Unknown") }) { viewModel.selectSession(it) }
                            }
                            
                            OutlinedTextField(
                                value = manualSessionId,
                                onValueChange = { viewModel.setManualSessionId(it) },
                                label = { Text("Manual ID", fontSize = 10.sp) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = DesignSystem.Cyan,
                                    unfocusedBorderColor = DesignSystem.Border
                                )
                            )
                        }
                        
                        Button(
                            onClick = { filtersExpanded = false },
                            modifier = Modifier.fillMaxWidth().height(40.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DesignSystem.Cyan.copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("DONE SELECTING", color = DesignSystem.Cyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                // Main Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = DesignSystem.PaddingLarge)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Stats Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        AttendanceStatCard("Roster", students.size.toString(), Modifier.weight(1f), DesignSystem.TextSecondary)
                        AttendanceStatCard("Present", "${selection.values.count { it }}", Modifier.weight(1f), DesignSystem.Success)
                        AttendanceStatCard("Absent", "${selection.values.count { !it }}", Modifier.weight(1f), DesignSystem.Danger)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Search + Mark All
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            placeholder = { Text("Filter...", color = DesignSystem.TextMuted, fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = DesignSystem.Cyan, modifier = Modifier.size(18.dp)) },
                            shape = RoundedCornerShape(DesignSystem.CornerRadius),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = DesignSystem.Cyan,
                                unfocusedBorderColor = DesignSystem.Border
                            )
                        )
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(
                                onClick = { viewModel.markAll(present = true) },
                                modifier = Modifier.size(40.dp).background(DesignSystem.Success.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            ) {
                                Icon(Icons.Default.CheckCircle, null, tint = DesignSystem.Success, modifier = Modifier.size(20.dp))
                            }
                            IconButton(
                                onClick = { viewModel.markAll(present = false) },
                                modifier = Modifier.size(40.dp).background(DesignSystem.Danger.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            ) {
                                Icon(Icons.Default.Cancel, null, tint = DesignSystem.Danger, modifier = Modifier.size(20.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Student Grid
                    if (isLoading) {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 100.dp),
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(12) {
                                ShimmerSkeleton(modifier = Modifier.fillMaxWidth().height(50.dp))
                            }
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 110.dp),
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            items(students, key = { it.student_id }) { student ->
                                val isPresent = selection[student.student_id] ?: true
                                StudentAttendanceCard(
                                    rollNo = student.roll_number,
                                    name = student.name,
                                    isPresent = isPresent
                                ) { viewModel.toggleStudent(student.student_id) }
                            }
                        }
                    }
                }
            }

            // Floating Action Buttons for Submit/Scan
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        val sId = toScannerSectionId(selectedBranch, selectedYear, selectedSection)
                        onNavigateToScanner(sId)
                    },
                    modifier = Modifier.height(50.dp).weight(0.4f),
                    colors = ButtonDefaults.buttonColors(containerColor = DesignSystem.Violet),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                ) {
                    Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(18.dp))
                }

                Button(
                    onClick = { viewModel.submitAttendance({}, {}) },
                    modifier = Modifier.height(50.dp).weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = DesignSystem.Cyan, contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isSubmitting,
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                ) {
                    if (isSubmitting) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black, strokeWidth = 2.dp)
                    else Text("SUBMIT ATTENDANCE", fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun AttendanceStatCard(label: String, value: String, modifier: Modifier = Modifier, color: Color) {
    GlassmorphicCard(
        modifier = modifier,
        borderColor = color.copy(alpha = 0.2f),
        backgroundColor = color.copy(alpha = 0.03f)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(label.uppercase(), color = DesignSystem.TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp)
            Text(value, color = color, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
fun SelectorDropdown(label: String, selectedValue: String, options: List<Pair<String, String>>, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Text(label.uppercase(), color = DesignSystem.TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(DesignSystem.Surface.copy(alpha = 0.5f))
                .border(1.dp, DesignSystem.Border, RoundedCornerShape(10.dp))
                .clickable { expanded = true }
                .padding(10.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = options.find { it.first == selectedValue }?.second ?: "Select...", 
                    color = if (selectedValue.isNotEmpty()) Color.White else DesignSystem.TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Icon(Icons.Default.ArrowDropDown, null, tint = DesignSystem.Cyan, modifier = Modifier.size(16.dp))
            }
            DropdownMenu(
                expanded = expanded, 
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(DesignSystem.Surface).border(1.dp, DesignSystem.Border)
            ) {
                options.forEach { (code, name) ->
                    DropdownMenuItem(
                        text = { Text(name, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp) },
                        onClick = {
                            onSelect(code)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun StudentAttendanceCard(rollNo: String, name: String, isPresent: Boolean, onToggle: () -> Unit) {
    val color = if (isPresent) DesignSystem.Success else DesignSystem.Danger
    
    GlassmorphicCard(
        modifier = Modifier
            .clickable { onToggle() }
            .height(52.dp), // Slightly shorter
        borderColor = color.copy(alpha = 0.4f),
        backgroundColor = color.copy(alpha = 0.08f)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = rollNo,
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 12.sp, // Slightly smaller to ensure fit
                fontFamily = FontFamily.Monospace,
                maxLines = 1
            )
            Text(
                text = name,
                color = if (isPresent) DesignSystem.TextSecondary else DesignSystem.Danger.copy(alpha = 0.8f),
                fontSize = 8.sp,
                maxLines = 1,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            // Minimalist indicator line at the bottom
            Spacer(Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(2.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}
