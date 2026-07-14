package com.vfstr.smartclass.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.automirrored.filled.HelpCenter
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.vfstr.smartclass.domain.models.UserRole

data class NavItem(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val route: String,
    val group: String = "main"
)

object Navigation {
    const val ROUTE_SPLASH = "splash"
    const val ROUTE_ONBOARDING = "onboarding"
    const val ROUTE_PERMISSION_HUB = "permission-hub"
    const val ROUTE_LOGIN = "login"
    const val ROUTE_UNAUTHORIZED = "unauthorized"
    
    // Staff Screens
    const val ROUTE_OVERVIEW = "overview"
    const val ROUTE_ATTENDANCE = "attendance"
    const val ROUTE_SECTION_ATTENDANCE = "section-attendance"
    const val ROUTE_SESSIONS = "sessions"
    const val ROUTE_OVERRIDE = "override"
    const val ROUTE_STUDENTS = "students"
    const val ROUTE_STUDENT_HIERARCHY = "student-hierarchy"
    const val ROUTE_TIMETABLE = "timetable"
    const val ROUTE_SUMMARY = "summary"
    const val ROUTE_DEVICES = "devices"
    const val ROUTE_COMPLIANCE = "compliance"
    const val ROUTE_LEAVEOD = "leave-od"
    const val ROUTE_NOTIFICATIONS = "notifications"
    const val ROUTE_ANALYTICS = "analytics"
    const val ROUTE_STUDENT_ANALYTICS = "student-analytics"
    const val ROUTE_STUDENT_REPORTS = "student-reports"
    const val ROUTE_MOOC = "mooc"
    const val ROUTE_USERS = "users"
    const val ROUTE_AUDIT = "audit"
    const val ROUTE_SCANNER = "scanner"
    const val ROUTE_PROFILE = "profile"
    const val ROUTE_SETTINGS = "settings"
    const val ROUTE_ATTENDANCE_LOGS = "attendance-logs"
    const val ROUTE_SECTION_ATTENDANCE_DETAILS = "section-attendance-details"
    const val ROUTE_COMPLIANCE_DETAILED = "compliance-detailed"

    // New R22 & Operational Audit screens
    const val ROUTE_MARK_APPEAL = "mark-appeal"
    const val ROUTE_TIMETABLE_AUTHOR = "timetable-author"
    const val ROUTE_FACULTY_WORKLOAD = "faculty-workload"
    const val ROUTE_PLACEMENT_SYNC = "placement-sync"
    const val ROUTE_HOSTEL_SYNC = "hostel-sync"
    const val ROUTE_ARCHIVAL = "archival"
    const val ROUTE_CURRICULUM = "curriculum"
    const val ROUTE_SABBATICAL = "sabbatical"
    const val ROUTE_EXAM_CONSOLE = "exam-console"
    const val ROUTE_GRIEVANCE = "grievance"
    const val ROUTE_PARENT_PORTAL = "parent-portal"
    const val ROUTE_LIBRARY_GATING = "library-gating"
    const val ROUTE_CAMPUS_CONFIG = "campus-config"
    
    // Enrollment Routes
    const val ROUTE_ENROLLMENT = "enrollment"
    const val ROUTE_ENROLLMENT_ADMIN = "enrollment/admin"
    const val ROUTE_ENROLLMENT_MANAGE = "enrollment/manage"
    const val ROUTE_ENROLLMENT_BULK = "enrollment/bulk"
    const val ROUTE_ENROLLMENT_STUDENT_DETAIL = "enrollment/students/{id}"

    // Student Portal Subtree Routes (RULE 12)
    const val ROUTE_STUDENT_PORTAL = "student/portal"
    const val ROUTE_STUDENT_OVERVIEW = "student/overview"
    const val ROUTE_STUDENT_ATTENDANCE = "student/attendance"
    const val ROUTE_STUDENT_PERFORMANCE = "student/performance"
    const val ROUTE_STUDENT_ACADEMICS = "student/academics"
    const val ROUTE_STUDENT_OD = "student/od"
    const val ROUTE_STUDENT_CERTIFICATES = "student/certificates"

    // Role-based page availability mapping (CORRECTION 10)
    private val ROLE_PAGES: Map<UserRole, List<String>> = mapOf(
        UserRole.superadmin to listOf(
            ROUTE_OVERVIEW, ROUTE_ATTENDANCE, ROUTE_ATTENDANCE_LOGS, ROUTE_SECTION_ATTENDANCE, ROUTE_SESSIONS,
            ROUTE_OVERRIDE, ROUTE_STUDENTS, ROUTE_STUDENT_HIERARCHY, ROUTE_TIMETABLE,
            ROUTE_SUMMARY, ROUTE_DEVICES, ROUTE_COMPLIANCE, ROUTE_LEAVEOD,
            ROUTE_NOTIFICATIONS, ROUTE_ANALYTICS, ROUTE_STUDENT_ANALYTICS,
            ROUTE_STUDENT_REPORTS, ROUTE_MOOC, ROUTE_USERS, ROUTE_AUDIT, ROUTE_SCANNER,
            ROUTE_PROFILE, ROUTE_SETTINGS,
            ROUTE_ENROLLMENT_ADMIN, ROUTE_ENROLLMENT_MANAGE, ROUTE_ENROLLMENT_BULK, ROUTE_ENROLLMENT_STUDENT_DETAIL,
            ROUTE_MARK_APPEAL, ROUTE_TIMETABLE_AUTHOR, ROUTE_FACULTY_WORKLOAD, ROUTE_PLACEMENT_SYNC, ROUTE_HOSTEL_SYNC,
            ROUTE_ARCHIVAL, ROUTE_CURRICULUM, ROUTE_SABBATICAL, ROUTE_EXAM_CONSOLE, ROUTE_GRIEVANCE,
            ROUTE_LIBRARY_GATING, ROUTE_CAMPUS_CONFIG
        ),
        UserRole.admin to listOf(
            ROUTE_OVERVIEW, ROUTE_ATTENDANCE, ROUTE_ATTENDANCE_LOGS, ROUTE_SECTION_ATTENDANCE, ROUTE_SESSIONS,
            ROUTE_OVERRIDE, ROUTE_STUDENTS, ROUTE_STUDENT_HIERARCHY, ROUTE_TIMETABLE,
            ROUTE_SUMMARY, ROUTE_DEVICES, ROUTE_COMPLIANCE, ROUTE_LEAVEOD,
            ROUTE_NOTIFICATIONS, ROUTE_ANALYTICS, ROUTE_STUDENT_ANALYTICS,
            ROUTE_STUDENT_REPORTS, ROUTE_MOOC, ROUTE_USERS, ROUTE_SCANNER,
            ROUTE_PROFILE, ROUTE_SETTINGS,
            ROUTE_ENROLLMENT_ADMIN, ROUTE_ENROLLMENT_MANAGE, ROUTE_ENROLLMENT_BULK, ROUTE_ENROLLMENT_STUDENT_DETAIL,
            ROUTE_MARK_APPEAL, ROUTE_TIMETABLE_AUTHOR, ROUTE_FACULTY_WORKLOAD, ROUTE_PLACEMENT_SYNC, ROUTE_HOSTEL_SYNC,
            ROUTE_CURRICULUM, ROUTE_SABBATICAL, ROUTE_EXAM_CONSOLE, ROUTE_GRIEVANCE,
            ROUTE_LIBRARY_GATING, ROUTE_CAMPUS_CONFIG
        ),
        UserRole.faculty to listOf(
            ROUTE_OVERVIEW, ROUTE_ATTENDANCE, ROUTE_ATTENDANCE_LOGS, ROUTE_SECTION_ATTENDANCE, ROUTE_SESSIONS,
            ROUTE_OVERRIDE, ROUTE_TIMETABLE, ROUTE_SUMMARY, ROUTE_COMPLIANCE,
            ROUTE_LEAVEOD, ROUTE_NOTIFICATIONS, ROUTE_ANALYTICS,
            ROUTE_STUDENT_ANALYTICS, ROUTE_STUDENT_REPORTS, ROUTE_SCANNER,
            ROUTE_PROFILE, ROUTE_SETTINGS,
            ROUTE_MARK_APPEAL
        ),
        UserRole.viewer to listOf(
            ROUTE_OVERVIEW, ROUTE_ATTENDANCE, ROUTE_ATTENDANCE_LOGS, ROUTE_SUMMARY, ROUTE_ANALYTICS,
            ROUTE_STUDENT_ANALYTICS, ROUTE_STUDENT_REPORTS,
            ROUTE_PROFILE, ROUTE_SETTINGS,
            ROUTE_PARENT_PORTAL
        ),
        UserRole.student to listOf(
            ROUTE_STUDENT_PORTAL, ROUTE_STUDENT_OVERVIEW, ROUTE_STUDENT_ATTENDANCE,
            ROUTE_STUDENT_PERFORMANCE, ROUTE_STUDENT_ACADEMICS, ROUTE_STUDENT_OD, ROUTE_STUDENT_CERTIFICATES,
            ROUTE_PROFILE, ROUTE_SETTINGS, ROUTE_ENROLLMENT,
            ROUTE_PARENT_PORTAL
        )
    )

    // Checks permission in on-the-fly navigation guards (RULE 4)
    fun isAllowed(role: UserRole?, route: String, activeContext: UserRole? = null): Boolean {
        if (route == ROUTE_LOGIN || route == ROUTE_SPLASH || route == ROUTE_ONBOARDING || route == ROUTE_PERMISSION_HUB) return true
        val effectiveRole = activeContext ?: role ?: return false
        val allowedList = ROLE_PAGES[effectiveRole] ?: emptyList()
        return allowedList.contains(route)
    }

    // Filters navigation drawers to match exact active user permissions
    fun getPermittedRoutes(role: UserRole): List<String> {
        return ROLE_PAGES[role] ?: emptyList()
    }

    val ALL_NAV_ITEMS = listOf(
        NavItem("overview", "Overview", Icons.Default.Dashboard, ROUTE_OVERVIEW),
        NavItem("attendance", "Attendance", Icons.Default.AccessTime, ROUTE_ATTENDANCE),
        NavItem("sessions", "Sessions", Icons.Default.VideoSettings, ROUTE_SESSIONS),
        NavItem("override", "Override", Icons.Default.CheckBox, ROUTE_OVERRIDE),
        NavItem("students", "Students", Icons.Default.People, ROUTE_STUDENTS),
        NavItem("student-hierarchy", "Student Hierarchy", Icons.Default.School, ROUTE_STUDENT_HIERARCHY),
        NavItem("timetable", "Timetable", Icons.Default.CalendarToday, ROUTE_TIMETABLE),
        NavItem("summary", "Summary", Icons.Default.Description, ROUTE_SUMMARY),
        NavItem("devices", "Devices", Icons.Default.CameraAlt, ROUTE_DEVICES),
        NavItem("compliance", "Compliance (R22)", Icons.AutoMirrored.Filled.FactCheck, ROUTE_COMPLIANCE),
        NavItem("leave-od", "Leave & OD", Icons.AutoMirrored.Filled.HelpCenter, ROUTE_LEAVEOD),
        NavItem("notifications", "Notifications", Icons.Default.Notifications, ROUTE_NOTIFICATIONS),
        NavItem("analytics", "Analytics", Icons.Default.BarChart, ROUTE_ANALYTICS),
        NavItem("student-analytics", "Student Analytics", Icons.Default.Psychology, ROUTE_STUDENT_ANALYTICS),
        NavItem("student-reports", "Student Reports", Icons.AutoMirrored.Filled.Assignment, ROUTE_STUDENT_REPORTS),
        NavItem("mooc", "MOOC Tracking", Icons.Default.Book, ROUTE_MOOC),
        
        // P2/P3 aligned items
        NavItem("mark-appeal", "Mark Appeal", Icons.Default.Warning, ROUTE_MARK_APPEAL),
        NavItem("timetable-author", "Timetable Author", Icons.Default.DateRange, ROUTE_TIMETABLE_AUTHOR, group = "admin"),
        NavItem("faculty-workload", "Faculty Workload", Icons.Default.Assessment, ROUTE_FACULTY_WORKLOAD, group = "admin"),
        NavItem("placement-sync", "Placement Sync", Icons.Default.Sync, ROUTE_PLACEMENT_SYNC, group = "admin"),
        NavItem("hostel-sync", "Hostel Sync", Icons.Default.Home, ROUTE_HOSTEL_SYNC, group = "admin"),
        NavItem("archival", "Archival Console", Icons.Default.Archive, ROUTE_ARCHIVAL, group = "admin"),
        NavItem("curriculum", "Curriculum Registry", Icons.Default.Book, ROUTE_CURRICULUM, group = "admin"),
        NavItem("sabbatical", "Sabbatical Registry", Icons.Default.CardMembership, ROUTE_SABBATICAL, group = "admin"),
        NavItem("exam-console", "Exam Console", Icons.Default.Assessment, ROUTE_EXAM_CONSOLE, group = "admin"),
        NavItem("grievance", "Grievance Desk", Icons.Default.Feedback, ROUTE_GRIEVANCE, group = "admin"),
        NavItem("parent-portal", "Parent Portal", Icons.Default.SupervisorAccount, ROUTE_PARENT_PORTAL),
        NavItem("library-gating", "Library Gating", Icons.Default.LocalLibrary, ROUTE_LIBRARY_GATING, group = "admin"),
        NavItem("campus-config", "Campus Config", Icons.Default.Domain, ROUTE_CAMPUS_CONFIG, group = "admin"),

        NavItem("enrollment-admin", "Enrollment Requests", Icons.Default.AppRegistration, ROUTE_ENROLLMENT_ADMIN, group = "admin"),
        NavItem("enrollment-manage", "Manage Students", Icons.Default.ManageAccounts, ROUTE_ENROLLMENT_MANAGE, group = "admin"),
        NavItem("enrollment-bulk", "Bulk Upload", Icons.Default.UploadFile, ROUTE_ENROLLMENT_BULK, group = "admin"),
        NavItem("profile", "Profile", Icons.Default.Person, ROUTE_PROFILE, group = "account"),
        NavItem("settings", "Settings", Icons.Default.Settings, ROUTE_SETTINGS, group = "account"),
        NavItem("users", "Users", Icons.Default.Settings, ROUTE_USERS, group = "admin"),
        NavItem("audit", "Audit", Icons.Default.Shield, ROUTE_AUDIT, group = "admin")
    )

    fun getFilteredNavItems(role: UserRole?, activeContext: UserRole? = null): List<NavItem> {
        val effectiveRole = activeContext ?: role ?: return emptyList()
        val permitted = ROLE_PAGES[effectiveRole] ?: emptyList()
        return ALL_NAV_ITEMS.filter { permitted.contains(it.route) }
    }
}
