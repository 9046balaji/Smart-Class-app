package com.vfstr.smartclass

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
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
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.vfstr.smartclass.domain.models.UserRole
import com.vfstr.smartclass.ui.MainViewModel
import com.vfstr.smartclass.ui.navigation.Navigation
import com.vfstr.smartclass.ui.navigation.NavItem
import com.vfstr.smartclass.ui.components.ApiHealthBanner
import com.vfstr.smartclass.ui.screens.ScreenUnauthorized
import com.vfstr.smartclass.ui.screens.auth.ScreenLogin
import com.vfstr.smartclass.ui.screens.onboarding.BiometricConsentBottomSheet
import com.vfstr.smartclass.ui.screens.onboarding.OnboardingPagerScreen
import com.vfstr.smartclass.ui.screens.onboarding.PermissionHubScreen
import com.vfstr.smartclass.ui.screens.splash.LaunchDestination
import com.vfstr.smartclass.ui.screens.splash.SplashGatekeeperScreen
import com.vfstr.smartclass.ui.screens.overview.ScreenOverview
import com.vfstr.smartclass.ui.screens.attendance.AttendanceScreen
import com.vfstr.smartclass.ui.screens.attendance.AttendanceEventViewer
import com.vfstr.smartclass.ui.screens.attendance.ScreenSectionAttendance
import com.vfstr.smartclass.ui.screens.compliance.ScreenComplianceDetailed
import com.vfstr.smartclass.ui.screens.sessions.ScreenSessions
import com.vfstr.smartclass.ui.screens.override.ScreenOverride
import com.vfstr.smartclass.ui.screens.students.ScreenStudents
import com.vfstr.smartclass.ui.screens.hierarchy.ScreenHierarchy
import com.vfstr.smartclass.ui.screens.timetable.ScreenTimetable
import com.vfstr.smartclass.ui.screens.summary.ScreenSummary
import com.vfstr.smartclass.ui.screens.devices.ScreenDevices
import com.vfstr.smartclass.ui.screens.compliance.ScreenCompliance
import com.vfstr.smartclass.ui.screens.leaveod.ScreenLeaveOD
import com.vfstr.smartclass.ui.screens.notifications.ScreenNotifications
import com.vfstr.smartclass.ui.screens.analytics.ScreenAnalytics
import com.vfstr.smartclass.ui.screens.studentanalytics.ScreenStudentAnalytics
import com.vfstr.smartclass.ui.screens.studentreports.ScreenStudentReports
import com.vfstr.smartclass.ui.screens.mooc.ScreenMOOC
import com.vfstr.smartclass.ui.screens.users.ScreenUsers
import com.vfstr.smartclass.ui.screens.audit.ScreenAudit
import com.vfstr.smartclass.ui.screens.scanner.ScreenScanner
import com.vfstr.smartclass.ui.screens.enrollment.*
import com.vfstr.smartclass.ui.screens.studentportal.*
import com.vfstr.smartclass.ui.screens.profile.ProfileScreen
import com.vfstr.smartclass.ui.screens.profile.SettingsScreen

// New screens imports
import com.vfstr.smartclass.ui.screens.staff.ScreenMarkAppeal
import com.vfstr.smartclass.ui.screens.timetable.ScreenTimetableAuthor
import com.vfstr.smartclass.ui.screens.staff.ScreenFacultyWorkload
import com.vfstr.smartclass.ui.screens.students.ScreenPlacementSync
import com.vfstr.smartclass.ui.screens.students.ScreenHostelSync
import com.vfstr.smartclass.ui.screens.audit.ScreenArchival
import com.vfstr.smartclass.ui.screens.hierarchy.ScreenCurriculum
import com.vfstr.smartclass.ui.screens.hierarchy.ScreenSabbatical
import com.vfstr.smartclass.ui.screens.sessions.ScreenExamConsole
import com.vfstr.smartclass.ui.screens.audit.ScreenGrievance
import com.vfstr.smartclass.ui.screens.studentportal.ScreenParentPortal
import com.vfstr.smartclass.ui.screens.devices.ScreenLibraryGating
import com.vfstr.smartclass.ui.screens.hierarchy.ScreenCampusConfig
import com.vfstr.smartclass.ui.theme.MyApplicationTheme
import com.vfstr.smartclass.ui.theme.DesignSystem
import com.vfstr.smartclass.utils.RoleGuard
import com.vfstr.smartclass.utils.security.BiometricLockManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    private val vm: MainViewModel by viewModels()

    @Inject
    lateinit var biometricLockManager: BiometricLockManager

    private var isAppLocked = true

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        if (isGranted) {
            vm.scanningGeoStatus.value = "INSIDE"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val biometricLockEnabled by vm.biometricLockEnabled.collectAsState()
                var isLockAuthenticated by remember { mutableStateOf(!biometricLockEnabled) }

                if (biometricLockEnabled && !isLockAuthenticated) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(DesignSystem.Background),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                tint = DesignSystem.Cyan,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(Modifier.height(16.dp))
                            Text("App Locked", color = DesignSystem.TextPrimary, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(32.dp))
                            Button(
                                onClick = {
                                    biometricLockManager.showBiometricPrompt(
                                        this@MainActivity,
                                        onSuccess = { isLockAuthenticated = true },
                                        onError = { /* Handle error if needed */ }
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DesignSystem.Cyan)
                            ) {
                                Text("Unlock with Biometrics", color = Color.Black)
                            }
                        }
                    }

                    LaunchedEffect(Unit) {
                        biometricLockManager.showBiometricPrompt(
                            this@MainActivity,
                            onSuccess = { isLockAuthenticated = true },
                            onError = { /* Handle error if needed */ }
                        )
                    }
                } else {
                    MainAppContent()
                }
            }
        }
    }

    @Composable
    fun MainAppContent() {
        val currentRoute by vm.currentRoute.collectAsState()
        val currentRole by vm.currentRole.collectAsState()
        val activeRoleContext by vm.activeRoleContext.collectAsState()
        val isUserLoggedIn by vm.isLoggedIn.collectAsState()
        val userName by vm.currentUserName.collectAsState()

        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        var showBiometricConsent by remember { mutableStateOf(false) }

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                if (isUserLoggedIn && currentRole != UserRole.student) {
                    StaffNavigationDrawer(
                        role = currentRole,
                        activeContext = activeRoleContext,
                        currentRoute = currentRoute,
                        onNavigate = { route ->
                            vm.currentRoute.value = route
                            scope.launch { drawerState.close() }
                        }
                    )
                }
            },
            gesturesEnabled = isUserLoggedIn && currentRole != UserRole.student
        ) {
            val isOnline by vm.isOnline.collectAsState()
            
            Scaffold(
                topBar = {
                    Column {
                        if (isUserLoggedIn) {
                            SmartClassTopBar(
                                role = activeRoleContext ?: currentRole,
                                name = userName,
                                onLogout = { vm.logout() },
                                onOpenDrawer = { scope.launch { drawerState.open() } },
                                onProfileClick = { vm.currentRoute.value = Navigation.ROUTE_PROFILE }
                            )
                        }
                        ApiHealthBanner(isOnline = isOnline)
                    }
                },
                bottomBar = {
                    if (isUserLoggedIn) {
                        if (currentRole == UserRole.student) {
                            // Student Portal usually has its own internal nav if complex
                            // but we follow RULE 12
                        } else {
                            StaffNavigationBar(
                                role = currentRole,
                                activeContext = activeRoleContext,
                                currentRoute = currentRoute,
                                onNavigate = { route -> vm.currentRoute.value = route },
                                onOpenMore = { scope.launch { drawerState.open() } }
                            )
                        }
                    }
                },
                containerColor = DesignSystem.Background
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                    if (isUserLoggedIn && currentRole == UserRole.student) {
                        StudentPortalShell(vm = vm)
                    } else {
                        AnimatedContent(
                            targetState = currentRoute,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(100))
                            },
                            label = "screen_navigation"
                        ) { targetScreen ->
                            val resolvedScreen = if (Navigation.isAllowed(currentRole, targetScreen, activeRoleContext)) {
                                targetScreen
                            } else {
                                Navigation.ROUTE_UNAUTHORIZED
                            }
                            
                            when {
                                resolvedScreen == Navigation.ROUTE_SPLASH -> SplashGatekeeperScreen(
                                    onNavigate = { destination ->
                                        vm.currentRoute.value = when (destination) {
                                            LaunchDestination.ONBOARDING -> Navigation.ROUTE_ONBOARDING
                                            LaunchDestination.STAFF_DASHBOARD -> Navigation.ROUTE_OVERVIEW
                                            LaunchDestination.STUDENT_PORTAL -> Navigation.ROUTE_STUDENT_OVERVIEW
                                            LaunchDestination.LOGIN -> Navigation.ROUTE_LOGIN
                                        }
                                    }
                                )
                                resolvedScreen == Navigation.ROUTE_ONBOARDING -> OnboardingPagerScreen(
                                    onOnboardingComplete = {
                                        vm.currentRoute.value = Navigation.ROUTE_PERMISSION_HUB
                                    }
                                )
                                resolvedScreen == Navigation.ROUTE_PERMISSION_HUB -> PermissionHubScreen(
                                    onPermissionsGranted = {
                                        vm.completeOnboarding()
                                        // Automatically navigate based on current state
                                        // vm.currentRoute.value = Navigation.ROUTE_LOGIN is handled by completeOnboarding or here
                                    }
                                )
                                resolvedScreen == Navigation.ROUTE_UNAUTHORIZED -> ScreenUnauthorized(vm = vm)
                                resolvedScreen == Navigation.ROUTE_LOGIN -> ScreenLogin(vm = vm)
                                resolvedScreen == Navigation.ROUTE_OVERVIEW -> ScreenOverview(vm = vm, onNav = { vm.currentRoute.value = it })
                                resolvedScreen == Navigation.ROUTE_ATTENDANCE -> {
                                    val attendanceVm: com.vfstr.smartclass.ui.screens.attendance.AttendanceViewModel = androidx.hilt.navigation.compose.hiltViewModel()
                                    AttendanceScreen(viewModel = attendanceVm)
                                }
                                resolvedScreen == Navigation.ROUTE_ATTENDANCE_LOGS -> AttendanceEventViewer(
                                    vm = vm,
                                    onNavigateBack = { vm.currentRoute.value = Navigation.ROUTE_ATTENDANCE }
                                )
                                resolvedScreen == Navigation.ROUTE_SESSIONS -> ScreenSessions(vm = vm)
                                resolvedScreen == Navigation.ROUTE_OVERRIDE -> ScreenOverride(vm = vm)
                                resolvedScreen == Navigation.ROUTE_STUDENTS -> ScreenStudents(vm = vm)
                                resolvedScreen == Navigation.ROUTE_STUDENT_HIERARCHY -> ScreenHierarchy(vm = vm)
                                resolvedScreen == Navigation.ROUTE_TIMETABLE -> ScreenTimetable(vm = vm)
                                resolvedScreen == Navigation.ROUTE_SUMMARY -> ScreenSummary(vm = vm)
                                resolvedScreen == Navigation.ROUTE_DEVICES -> ScreenDevices(vm = vm)
                                resolvedScreen == Navigation.ROUTE_COMPLIANCE -> ScreenCompliance(vm = vm)
                                resolvedScreen == Navigation.ROUTE_COMPLIANCE_DETAILED -> ScreenComplianceDetailed(
                                    vm = vm,
                                    onNavigateBack = { vm.currentRoute.value = Navigation.ROUTE_COMPLIANCE }
                                )
                                resolvedScreen == Navigation.ROUTE_SECTION_ATTENDANCE_DETAILS -> {
                                    ScreenSectionAttendance(
                                        vm = vm,
                                        sectionId = "AI3A", // Hardcoded for now or from state
                                        onNavigateBack = { vm.currentRoute.value = Navigation.ROUTE_ATTENDANCE }
                                    )
                                }
                                resolvedScreen == Navigation.ROUTE_LEAVEOD -> ScreenLeaveOD(vm = vm)
                                resolvedScreen == Navigation.ROUTE_NOTIFICATIONS -> ScreenNotifications(vm = vm)
                                resolvedScreen == Navigation.ROUTE_ANALYTICS -> ScreenAnalytics(vm = vm)
                                resolvedScreen == Navigation.ROUTE_STUDENT_ANALYTICS -> ScreenStudentAnalytics(vm = vm)
                                resolvedScreen == Navigation.ROUTE_STUDENT_REPORTS -> ScreenStudentReports(vm = vm)
                                resolvedScreen == Navigation.ROUTE_MOOC -> ScreenMOOC(vm = vm)
                                resolvedScreen == Navigation.ROUTE_USERS -> ScreenUsers(vm = vm)
                                resolvedScreen == Navigation.ROUTE_AUDIT -> ScreenAudit(vm = vm)
                                resolvedScreen == Navigation.ROUTE_PROFILE -> ProfileScreen(vm = vm)
                                resolvedScreen == Navigation.ROUTE_SETTINGS -> SettingsScreen(vm = vm)
                                 
                                 // New R22 and Operational Audit Screen routes
                                 resolvedScreen == Navigation.ROUTE_MARK_APPEAL -> ScreenMarkAppeal(vm = vm)
                                 resolvedScreen == Navigation.ROUTE_TIMETABLE_AUTHOR -> ScreenTimetableAuthor(vm = vm)
                                 resolvedScreen == Navigation.ROUTE_FACULTY_WORKLOAD -> ScreenFacultyWorkload(vm = vm)
                                 resolvedScreen == Navigation.ROUTE_PLACEMENT_SYNC -> ScreenPlacementSync(vm = vm)
                                 resolvedScreen == Navigation.ROUTE_HOSTEL_SYNC -> ScreenHostelSync(vm = vm)
                                 resolvedScreen == Navigation.ROUTE_ARCHIVAL -> ScreenArchival(vm = vm)
                                 resolvedScreen == Navigation.ROUTE_CURRICULUM -> ScreenCurriculum(vm = vm)
                                 resolvedScreen == Navigation.ROUTE_SABBATICAL -> ScreenSabbatical(vm = vm)
                                 resolvedScreen == Navigation.ROUTE_EXAM_CONSOLE -> ScreenExamConsole(vm = vm)
                                 resolvedScreen == Navigation.ROUTE_GRIEVANCE -> ScreenGrievance(vm = vm)
                                 resolvedScreen == Navigation.ROUTE_PARENT_PORTAL -> ScreenParentPortal(vm = vm)
                                 resolvedScreen == Navigation.ROUTE_LIBRARY_GATING -> ScreenLibraryGating(vm = vm)
                                 resolvedScreen == Navigation.ROUTE_CAMPUS_CONFIG -> ScreenCampusConfig(vm = vm)
                                
                                // Enrollment Routes (Phase 4)
                                resolvedScreen == Navigation.ROUTE_ENROLLMENT_ADMIN -> ScreenEnrollmentAdminDashboard(
                                    vm = hiltViewModel(),
                                    onNavigateToReview = { id -> vm.currentRoute.value = Navigation.ROUTE_ENROLLMENT_STUDENT_DETAIL.replace("{id}", id) },
                                    onNavigateToBulk = { vm.currentRoute.value = Navigation.ROUTE_ENROLLMENT_BULK }
                                )
                                resolvedScreen == Navigation.ROUTE_ENROLLMENT_MANAGE -> ScreenManageStudents(
                                    vm = hiltViewModel(),
                                    onNavigateToDetail = { id -> vm.currentRoute.value = Navigation.ROUTE_ENROLLMENT_STUDENT_DETAIL.replace("{id}", id) }
                                )
                                resolvedScreen == Navigation.ROUTE_ENROLLMENT_BULK -> ScreenBulkUpload(
                                    vm = hiltViewModel(),
                                    onNavigateBack = { vm.currentRoute.value = Navigation.ROUTE_ENROLLMENT_ADMIN }
                                )
                                resolvedScreen.startsWith("enrollment/students/") -> {
                                    val id = resolvedScreen.substringAfterLast("/")
                                    ScreenStudentProfileReview(
                                        vm = hiltViewModel(),
                                        studentId = id,
                                        onNavigateBack = { vm.currentRoute.value = Navigation.ROUTE_ENROLLMENT_ADMIN }
                                    )
                                }

                                resolvedScreen.startsWith(Navigation.ROUTE_SCANNER) -> {
                                    val uri = android.net.Uri.parse("app://" + resolvedScreen)
                                    val sectionId = uri.getQueryParameter("section")
                                    val mode = uri.getQueryParameter("mode")
                                    val rollNo = uri.getQueryParameter("rollNo")
                                    ScreenScanner(
                                        sectionId = sectionId,
                                        sessionIdFromUrl = null,
                                        enrollmentMode = (mode == "enroll"),
                                        enrollmentRollNo = rollNo,
                                        onNavigateBack = { vm.currentRoute.value = Navigation.ROUTE_ATTENDANCE }
                                    )
                                }
                                else -> ScreenLogin(vm = vm)
                            }
                        }
                    }
                }

                if (showBiometricConsent) {
                    BiometricConsentBottomSheet(
                        onDismiss = { showBiometricConsent = false },
                        onConsentGiven = { granted ->
                            showBiometricConsent = false
                            vm.saveBiometricConsent(granted)
                            vm.currentRoute.value = Navigation.ROUTE_LOGIN
                        }
                    )
                }
            }
        }
    }

    private fun checkCameraPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            vm.scanningGeoStatus.value = "INSIDE"
        }
    }
}

@Composable
fun SmartClassTopBar(role: UserRole?, name: String, onLogout: () -> Unit, onOpenDrawer: () -> Unit, onProfileClick: () -> Unit) {
    Surface(
        color = DesignSystem.Background,
        modifier = Modifier.fillMaxWidth().statusBarsPadding().border(width = 1.dp, color = DesignSystem.Border)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (role != UserRole.student) {
                    IconButton(onClick = onOpenDrawer, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Menu, contentDescription = "menu", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Box(modifier = Modifier.size(36.dp).background(DesignSystem.VignanBlue, RoundedCornerShape(10.dp)).clickable { onProfileClick() }, contentAlignment = Alignment.Center) {
                    Text("VF", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.clickable { onProfileClick() }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("SmartClass", color = DesignSystem.Cyan, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        role?.let {
                            Spacer(Modifier.width(8.dp))
                            Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(Color.White.copy(alpha = 0.1f)).padding(horizontal = 4.dp, vertical = 2.dp)) {
                                Text(it.name.uppercase(), color = DesignSystem.TextSecondary, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Text("VFSTR UNIVERSITY", color = DesignSystem.TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(DesignSystem.CardBg).clickable { onProfileClick() }.padding(horizontal = 10.dp, vertical = 6.dp)) {
                    Text(text = name.ifEmpty { "Guest" }, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onLogout, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "logout", tint = DesignSystem.Danger)
                }
            }
        }
    }
}

@Composable
fun StaffNavigationBar(role: UserRole?, activeContext: UserRole?, currentRoute: String, onNavigate: (String) -> Unit, onOpenMore: () -> Unit) {
    val effectiveRole = activeContext ?: role
    val permitted = effectiveRole?.let { Navigation.getPermittedRoutes(it) } ?: emptyList()
    
    Surface(
        color = DesignSystem.Surface,
        modifier = Modifier.fillMaxWidth().navigationBarsPadding().border(width = 1.dp, color = DesignSystem.Border)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StaffNavBarItem(icon = Icons.Default.Home, label = "Home", isSelected = currentRoute == Navigation.ROUTE_OVERVIEW, onClick = { onNavigate(Navigation.ROUTE_OVERVIEW) })
            
            if (permitted.contains(Navigation.ROUTE_ATTENDANCE)) {
                StaffNavBarItem(icon = Icons.Default.CalendarToday, label = "Attendance", isSelected = currentRoute == Navigation.ROUTE_ATTENDANCE, onClick = { onNavigate(Navigation.ROUTE_ATTENDANCE) })
            }
            
            if (permitted.contains(Navigation.ROUTE_SESSIONS)) {
                StaffNavBarItem(icon = Icons.Default.VideoCall, label = "Sessions", isSelected = currentRoute == Navigation.ROUTE_SESSIONS, onClick = { onNavigate(Navigation.ROUTE_SESSIONS) })
            }
            
            if (permitted.contains(Navigation.ROUTE_ANALYTICS)) {
                StaffNavBarItem(icon = Icons.Default.BarChart, label = "Analytics", isSelected = currentRoute == Navigation.ROUTE_ANALYTICS, onClick = { onNavigate(Navigation.ROUTE_ANALYTICS) })
            }

            StaffNavBarItem(icon = Icons.Default.Menu, label = "More", isSelected = false, onClick = onOpenMore)
        }
    }
}

@Composable
fun StaffNavBarItem(icon: ImageVector, label: String, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable { onClick() }.padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = if (isSelected) DesignSystem.Cyan else DesignSystem.TextMuted, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = label, fontSize = 9.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, color = if (isSelected) DesignSystem.Cyan else DesignSystem.TextMuted)
    }
}

@Composable
fun StaffNavigationDrawer(
    role: UserRole?,
    activeContext: UserRole?,
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    val items = Navigation.getFilteredNavItems(role, activeContext)
    val mainItems = items.filter { it.group == "main" }
    val accountItems = items.filter { it.group == "account" }
    val adminItems = items.filter { it.group == "admin" }

    ModalDrawerSheet(
        drawerContainerColor = DesignSystem.Surface,
        modifier = Modifier.fillMaxHeight().width(300.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
            Text(
                "SMARTCLASS MENU",
                color = DesignSystem.Cyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 16.dp)
            )

            mainItems.forEach { item ->
                NavigationDrawerItem(
                    label = { Text(item.title, fontWeight = FontWeight.Bold, fontSize = 14.sp) },
                    selected = currentRoute == item.route,
                    onClick = { onNavigate(item.route) },
                    icon = { Icon(item.icon, contentDescription = null, modifier = Modifier.size(22.dp)) },
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedContainerColor = Color.Transparent,
                        selectedContainerColor = DesignSystem.Cyan.copy(alpha = 0.1f),
                        unselectedTextColor = DesignSystem.TextSecondary,
                        selectedTextColor = DesignSystem.Cyan,
                        unselectedIconColor = DesignSystem.TextMuted,
                        selectedIconColor = DesignSystem.Cyan
                    ),
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }

            if (accountItems.isNotEmpty()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp, horizontal = 12.dp), color = DesignSystem.Border)
                Text(
                    "ACCOUNT & SETTINGS",
                    color = DesignSystem.Violet,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )

                accountItems.forEach { item ->
                    NavigationDrawerItem(
                        label = { Text(item.title, fontWeight = FontWeight.Bold, fontSize = 14.sp) },
                        selected = currentRoute == item.route,
                        onClick = { onNavigate(item.route) },
                        icon = { Icon(item.icon, contentDescription = null, modifier = Modifier.size(22.dp)) },
                        colors = NavigationDrawerItemDefaults.colors(
                            unselectedContainerColor = Color.Transparent,
                            selectedContainerColor = DesignSystem.Violet.copy(alpha = 0.1f),
                            unselectedTextColor = DesignSystem.TextSecondary,
                            selectedTextColor = DesignSystem.Violet,
                            unselectedIconColor = DesignSystem.TextMuted,
                            selectedIconColor = DesignSystem.Violet
                        ),
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }

            if (adminItems.isNotEmpty()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp, horizontal = 12.dp), color = DesignSystem.Border)
                Text(
                    "ADMINISTRATION",
                    color = DesignSystem.Danger,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )

                adminItems.forEach { item ->
                    NavigationDrawerItem(
                        label = { Text(item.title, fontWeight = FontWeight.Bold, fontSize = 14.sp) },
                        selected = currentRoute == item.route,
                        onClick = { onNavigate(item.route) },
                        icon = { Icon(item.icon, contentDescription = null, modifier = Modifier.size(22.dp)) },
                        colors = NavigationDrawerItemDefaults.colors(
                            unselectedContainerColor = Color.Transparent,
                            selectedContainerColor = DesignSystem.Danger.copy(alpha = 0.1f),
                            unselectedTextColor = DesignSystem.TextSecondary,
                            selectedTextColor = DesignSystem.Danger,
                            unselectedIconColor = DesignSystem.TextMuted,
                            selectedIconColor = DesignSystem.Danger
                        ),
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
