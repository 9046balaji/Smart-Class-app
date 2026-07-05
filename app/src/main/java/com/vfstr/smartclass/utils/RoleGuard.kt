package com.vfstr.smartclass.utils

import com.vfstr.smartclass.domain.models.UserRole
import com.vfstr.smartclass.ui.navigation.Navigation

object RoleGuard {
    val ROLE_PAGES: Map<UserRole, List<String>> = mapOf(
        UserRole.superadmin to listOf(
            Navigation.ROUTE_OVERVIEW, Navigation.ROUTE_ATTENDANCE, Navigation.ROUTE_ATTENDANCE_LOGS, Navigation.ROUTE_SECTION_ATTENDANCE, Navigation.ROUTE_SESSIONS, Navigation.ROUTE_OVERRIDE,
            Navigation.ROUTE_STUDENTS, Navigation.ROUTE_STUDENT_HIERARCHY, Navigation.ROUTE_TIMETABLE, Navigation.ROUTE_SUMMARY,
            Navigation.ROUTE_DEVICES, Navigation.ROUTE_COMPLIANCE, Navigation.ROUTE_LEAVEOD, Navigation.ROUTE_NOTIFICATIONS,
            Navigation.ROUTE_ANALYTICS, Navigation.ROUTE_STUDENT_ANALYTICS, Navigation.ROUTE_STUDENT_REPORTS,
            Navigation.ROUTE_MOOC, Navigation.ROUTE_USERS, Navigation.ROUTE_AUDIT, Navigation.ROUTE_SCANNER
        ),
        UserRole.admin to listOf(
            Navigation.ROUTE_OVERVIEW, Navigation.ROUTE_ATTENDANCE, Navigation.ROUTE_ATTENDANCE_LOGS, Navigation.ROUTE_SECTION_ATTENDANCE, Navigation.ROUTE_SESSIONS, Navigation.ROUTE_OVERRIDE,
            Navigation.ROUTE_STUDENTS, Navigation.ROUTE_STUDENT_HIERARCHY, Navigation.ROUTE_TIMETABLE, Navigation.ROUTE_SUMMARY,
            Navigation.ROUTE_DEVICES, Navigation.ROUTE_COMPLIANCE, Navigation.ROUTE_LEAVEOD, Navigation.ROUTE_NOTIFICATIONS,
            Navigation.ROUTE_ANALYTICS, Navigation.ROUTE_STUDENT_ANALYTICS, Navigation.ROUTE_STUDENT_REPORTS,
            Navigation.ROUTE_MOOC, Navigation.ROUTE_USERS, Navigation.ROUTE_SCANNER
        ),
        UserRole.faculty to listOf(
            Navigation.ROUTE_OVERVIEW, Navigation.ROUTE_ATTENDANCE, Navigation.ROUTE_ATTENDANCE_LOGS, Navigation.ROUTE_SECTION_ATTENDANCE, Navigation.ROUTE_SESSIONS, Navigation.ROUTE_OVERRIDE,
            Navigation.ROUTE_TIMETABLE, Navigation.ROUTE_SUMMARY, Navigation.ROUTE_COMPLIANCE, Navigation.ROUTE_LEAVEOD,
            Navigation.ROUTE_NOTIFICATIONS, Navigation.ROUTE_ANALYTICS, Navigation.ROUTE_STUDENT_ANALYTICS,
            Navigation.ROUTE_STUDENT_REPORTS, Navigation.ROUTE_SCANNER
        ),
        UserRole.viewer to listOf(
            Navigation.ROUTE_OVERVIEW, Navigation.ROUTE_ATTENDANCE, Navigation.ROUTE_ATTENDANCE_LOGS, Navigation.ROUTE_SUMMARY, Navigation.ROUTE_ANALYTICS,
            Navigation.ROUTE_STUDENT_ANALYTICS, Navigation.ROUTE_STUDENT_REPORTS
        ),
        UserRole.student to listOf(
            Navigation.ROUTE_STUDENT_PORTAL, Navigation.ROUTE_STUDENT_OVERVIEW, Navigation.ROUTE_STUDENT_ATTENDANCE, Navigation.ROUTE_STUDENT_PERFORMANCE,
            Navigation.ROUTE_STUDENT_ACADEMICS, Navigation.ROUTE_STUDENT_OD, Navigation.ROUTE_STUDENT_CERTIFICATES
        )
    )

    fun isAllowed(role: UserRole?, route: String, activeContext: UserRole? = null): Boolean {
        if (route == Navigation.ROUTE_LOGIN || 
            route == Navigation.ROUTE_UNAUTHORIZED ||
            route == Navigation.ROUTE_SPLASH ||
            route == Navigation.ROUTE_ONBOARDING ||
            route == Navigation.ROUTE_PERMISSION_HUB
        ) return true
        val effectiveRole = activeContext ?: role ?: return false
        val allowedList = ROLE_PAGES[effectiveRole] ?: emptyList()
        return allowedList.contains(route)
    }

    fun filterNavigation(role: UserRole?, items: List<String>, activeContext: UserRole? = null): List<String> {
        val effectiveRole = activeContext ?: role ?: return emptyList()
        return items.filter { isAllowed(effectiveRole, it) }
    }
}

