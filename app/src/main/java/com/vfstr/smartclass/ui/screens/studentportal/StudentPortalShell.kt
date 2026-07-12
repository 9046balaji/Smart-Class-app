package com.vfstr.smartclass.ui.screens.studentportal

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vfstr.smartclass.ui.MainViewModel
import com.vfstr.smartclass.ui.screens.profile.ProfileScreen
import com.vfstr.smartclass.ui.screens.profile.SettingsScreen
import com.vfstr.smartclass.ui.theme.DesignSystem

enum class PortalSection(val label: String, val icon: ImageVector) {
    OVERVIEW("Dashboard", Icons.Default.Dashboard),
    ATTENDANCE("Attendance", Icons.Default.EventNote),
    MARKS("Marks", Icons.Default.Assessment),
    CERTIFICATES("MOOCs", Icons.Default.Verified),
    ENROLLMENT("Face Sync", Icons.Default.PersonAdd),
    OD_REQUESTS("Leave & OD", Icons.Default.Description),
    RESULTS("Academic Results", Icons.Default.Grade),
    PROFILE("Profile", Icons.Default.Person),
    SETTINGS("Settings", Icons.Default.Settings)
}

@Composable
fun StudentPortalShell(
    vm: MainViewModel,
    modifier: Modifier = Modifier
) {
    var currentSection by remember { mutableStateOf(PortalSection.OVERVIEW) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DesignSystem.Background,
        topBar = {
            PortalTopBar(
                vm = vm,
                section = currentSection,
                onProfileClick = { currentSection = PortalSection.PROFILE },
                onSettingsClick = { currentSection = PortalSection.SETTINGS }
            )
        },
        bottomBar = {
            PortalBottomNav(currentSection) { currentSection = it }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            Crossfade(targetState = currentSection, label = "portal_sections") { section ->
                when (section) {
                    PortalSection.OVERVIEW -> ScreenStudentOverview(
                        vm = vm,
                        onNavigateToMOOCs = { currentSection = PortalSection.CERTIFICATES },
                        onNavigateToLeave = { currentSection = PortalSection.OD_REQUESTS },
                        onNavigateToResults = { currentSection = PortalSection.RESULTS }
                    )
                    PortalSection.ATTENDANCE -> ScreenStudentAttendance(vm)
                    PortalSection.MARKS -> ScreenStudentMarks(vm)
                    PortalSection.CERTIFICATES -> ScreenStudentCertificates(vm)
                    PortalSection.ENROLLMENT -> EnrollmentDispatcher(vm)
                    PortalSection.OD_REQUESTS -> ScreenStudentOD(vm)
                    PortalSection.RESULTS -> ScreenStudentResults(vm)
                    PortalSection.PROFILE -> ProfileScreen(vm)
                    PortalSection.SETTINGS -> SettingsScreen(vm)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortalTopBar(
    vm: MainViewModel,
    section: PortalSection,
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    TopAppBar(
        title = { 
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(DesignSystem.VignanBlue)
                        .clickable { onProfileClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("VF", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.clickable { onProfileClick() }) {
                    Text("VFSTR Student Portal", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(section.label, fontSize = 10.sp, color = DesignSystem.Cyan)
                }
            }
        },
        actions = {
            IconButton(onClick = onSettingsClick) {
                Icon(Icons.Default.Settings, null, tint = DesignSystem.TextSecondary)
            }
            IconButton(onClick = { vm.logout() }) {
                Icon(Icons.AutoMirrored.Filled.Logout, null, tint = DesignSystem.Danger)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = DesignSystem.Surface)
    )
}

@Composable
fun EnrollmentDispatcher(vm: MainViewModel) {
    val studentId by vm.currentUserName.collectAsState()
    val enrollmentVm: com.vfstr.smartclass.ui.screens.enrollment.EnrollmentViewModel = hiltViewModel()
    
    val requestState by enrollmentVm.requestState.collectAsState()
    
    if (requestState == null) {
        com.vfstr.smartclass.ui.screens.enrollment.ScreenEnrollmentRegistration(
            vm = enrollmentVm,
            onNavigateBack = { /* Portal handles nav */ },
            onSuccess = { enrollmentVm.loadRequest(studentId) }
        )
    } else {
        com.vfstr.smartclass.ui.screens.enrollment.ScreenEnrollmentStatus(
            vm = enrollmentVm,
            studentId = studentId,
            onNavigateToEdit = { /* Toggle state */ },
            onNavigateToProfile = { /* Portal tab profile */ }
        )
    }
}

@Composable
fun PortalBottomNav(current: PortalSection, onSelect: (PortalSection) -> Unit) {
    Surface(
        color = DesignSystem.Surface,
        modifier = Modifier.fillMaxWidth().height(72.dp),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PortalSection.entries.filter { 
                it != PortalSection.PROFILE && 
                it != PortalSection.SETTINGS && 
                it != PortalSection.RESULTS && 
                it != PortalSection.OD_REQUESTS 
            }.forEach { section ->
                val isSel = current == section
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelect(section) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    BadgedBox(
                        badge = {
                            if (section == PortalSection.ENROLLMENT) {
                                // Mock badge for now, real one from ViewModel can be here
                            }
                        }
                    ) {
                        Icon(
                            imageVector = section.icon,
                            contentDescription = section.label,
                            tint = if (isSel) DesignSystem.Cyan else DesignSystem.TextMuted,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = section.label,
                        fontSize = 9.sp,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSel) DesignSystem.Cyan else DesignSystem.TextMuted
                    )
                }
            }
        }
    }
}

