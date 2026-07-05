package com.vfstr.smartclass.ui.screens.enrollment

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
import com.vfstr.smartclass.domain.models.*
import com.vfstr.smartclass.ui.components.GlassmorphicCard
import com.vfstr.smartclass.ui.components.MeshBackground
import com.vfstr.smartclass.ui.theme.DesignSystem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenManageStudents(
    vm: ManageStudentsViewModel,
    onNavigateToDetail: (String) -> Unit
) {
    val students by vm.students.collectAsState()
    val isLoading by vm.isLoading.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showFilters by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        vm.loadStudents(reset = true)
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(DesignSystem.Surface).padding(16.dp)) {
                Text("Student Directory", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onSearch = { vm.updateFilters(mapOf("search" to it)) },
                    active = false,
                    onActiveChange = {},
                    placeholder = { Text("Search by name or roll number") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    modifier = Modifier.fillMaxWidth()
                ) {}
            }
        },
        containerColor = DesignSystem.Background
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            MeshBackground()

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(students) { student ->
                    StudentDirectoryCard(student, onNavigateToDetail)
                }
                
                item {
                    if (!isLoading) {
                        Button(
                            onClick = { vm.loadStudents() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = DesignSystem.Surface)
                        ) {
                            Text("Load More")
                        }
                    }
                }
            }

            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = DesignSystem.Cyan)
            }
        }
    }
}

@Composable
fun StudentDirectoryCard(item: StudentListItem, onClick: (String) -> Unit) {
    GlassmorphicCard(modifier = Modifier.fillMaxWidth().clickable { onClick(item.id) }) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp).clip(CircleShape).background(DesignSystem.Surface), contentAlignment = Alignment.Center) {
                Text(item.name.take(1), color = DesignSystem.Cyan, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(item.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("Roll: ${item.rollNumber}", color = DesignSystem.TextSecondary, fontSize = 11.sp)
                Text("${item.branch} • ${item.year} • Sec ${item.section}", color = DesignSystem.TextMuted, fontSize = 10.sp)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (item.status == EnrollmentStatus.APPROVED) DesignSystem.Success.copy(alpha = 0.1f) else DesignSystem.Warning.copy(alpha = 0.1f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    item.status.name.take(1),
                    color = if (item.status == EnrollmentStatus.APPROVED) DesignSystem.Success else DesignSystem.Warning,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
