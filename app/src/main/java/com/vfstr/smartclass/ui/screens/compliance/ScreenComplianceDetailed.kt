package com.vfstr.smartclass.ui.screens.compliance

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
fun ScreenComplianceDetailed(
    vm: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val students by vm.students.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("R22 Compliance Audit", fontWeight = FontWeight.Bold) },
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
            Text(
                "Week 12 Review Cycle", 
                color = DesignSystem.Cyan, 
                fontWeight = FontWeight.ExtraBold, 
                fontSize = 12.sp,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Detailed eligibility metrics for CSAI-3A",
                color = DesignSystem.TextSecondary,
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Rule 15: Sticky Data Table
            StickyDataTable(
                headers = listOf("Roll No", "Name", "Attendance %", "Min Required", "Status", "Reviewer"),
                rows = students.map { st ->
                    listOf(
                        st.rollNo,
                        st.name,
                        "${(70..95).random()}%",
                        "75%",
                        if ((1..10).random() > 2) "ELIGIBLE" else "CONDITIONAL",
                        "System_Auto"
                    )
                },
                columnWidths = listOf(100.dp, 150.dp, 100.dp, 100.dp, 120.dp, 120.dp),
                stickyColumns = 2,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(DesignSystem.CornerRadius))
                    .border(1.dp, DesignSystem.Border, RoundedCornerShape(DesignSystem.CornerRadius))
            )
        }
    }
}
