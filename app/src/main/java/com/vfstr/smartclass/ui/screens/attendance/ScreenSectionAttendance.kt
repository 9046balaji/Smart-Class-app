package com.vfstr.smartclass.ui.screens.attendance

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vfstr.smartclass.ui.MainViewModel
import com.vfstr.smartclass.ui.components.StickyDataTable
import com.vfstr.smartclass.ui.theme.DesignSystem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenSectionAttendance(
    vm: MainViewModel,
    sectionId: String,
    onNavigateBack: () -> Unit
) {
    val students by vm.students.collectAsState()
    
    LaunchedEffect(sectionId) {
        // Parse sectionId if needed or use directly
        vm.loadStudents(sec = sectionId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Section Attendance: $sectionId", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DesignSystem.Surface)
            )
        },
        containerColor = DesignSystem.Background
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            // Summary Cards
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                AttendanceStatItem("Avg %", "81.4%", DesignSystem.Cyan, Modifier.weight(1f))
                AttendanceStatItem("Students", "${students.size}", DesignSystem.Violet, Modifier.weight(1f))
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text("Detailed Roster", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(12.dp))
            
            // Rule 15: Sticky Data Table
            StickyDataTable(
                headers = listOf("Roll No", "Name", "Lec (8)", "Prac (4)", "Total %", "Status"),
                rows = students.map { st ->
                    listOf(
                        st.rollNo,
                        st.name,
                        "6/8",
                        "4/4",
                        "83.3%",
                        "OK"
                    )
                },
                columnWidths = listOf(100.dp, 150.dp, 80.dp, 80.dp, 80.dp, 80.dp),
                stickyColumns = 2,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(DesignSystem.CornerRadius))
                    .border(1.dp, DesignSystem.Border, RoundedCornerShape(DesignSystem.CornerRadius))
            )
        }
    }
}

@Composable
fun AttendanceStatItem(label: String, value: String, color: Color, modifier: Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(DesignSystem.CardBg)
            .padding(16.dp)
    ) {
        Column {
            Text(label, color = DesignSystem.TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(value, color = color, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}
