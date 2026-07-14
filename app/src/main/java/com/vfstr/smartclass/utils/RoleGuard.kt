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
            Navigation.ROUTE_MOOC, Navigation.ROUTE_USERS, Navigation.ROUTE_AUDIT, Navigation.ROUTE_SCANNER,
            Navigation.ROUTE_MARK_APPEAL, Navigation.ROUTE_TIMETABLE_AUTHOR, Navigation.ROUTE_FACULTY_WORKLOAD, Navigation.ROUTE_PLACEMENT_SYNC, Navigation.ROUTE_HOSTEL_SYNC,
            Navigation.ROUTE_ARCHIVAL, Navigation.ROUTE_CURRICULUM, Navigation.ROUTE_SABBATICAL, Navigation.ROUTE_EXAM_CONSOLE, Navigation.ROUTE_GRIEVANCE,
            Navigation.ROUTE_LIBRARY_GATING, Navigation.ROUTE_CAMPUS_CONFIG
        ),
        UserRole.admin to listOf(
            Navigation.ROUTE_OVERVIEW, Navigation.ROUTE_ATTENDANCE, Navigation.ROUTE_ATTENDANCE_LOGS, Navigation.ROUTE_SECTION_ATTENDANCE, Navigation.ROUTE_SESSIONS, Navigation.ROUTE_OVERRIDE,
            Navigation.ROUTE_STUDENTS, Navigation.ROUTE_STUDENT_HIERARCHY, Navigation.ROUTE_TIMETABLE, Navigation.ROUTE_SUMMARY,
            Navigation.ROUTE_DEVICES, Navigation.ROUTE_COMPLIANCE, Navigation.ROUTE_LEAVEOD, Navigation.ROUTE_NOTIFICATIONS,
            Navigation.ROUTE_ANALYTICS, Navigation.ROUTE_STUDENT_ANALYTICS, Navigation.ROUTE_STUDENT_REPORTS,
            Navigation.ROUTE_MOOC, Navigation.ROUTE_USERS, Navigation.ROUTE_SCANNER,
            Navigation.ROUTE_MARK_APPEAL, Navigation.ROUTE_TIMETABLE_AUTHOR, Navigation.ROUTE_FACULTY_WORKLOAD, Navigation.ROUTE_PLACEMENT_SYNC, Navigation.ROUTE_HOSTEL_SYNC,
            Navigation.ROUTE_CURRICULUM, Navigation.ROUTE_SABBATICAL, Navigation.ROUTE_EXAM_CONSOLE, Navigation.ROUTE_GRIEVANCE,
            Navigation.ROUTE_LIBRARY_GATING, Navigation.ROUTE_CAMPUS_CONFIG
        ),
        UserRole.faculty to listOf(
            Navigation.ROUTE_OVERVIEW, Navigation.ROUTE_ATTENDANCE, Navigation.ROUTE_ATTENDANCE_LOGS, Navigation.ROUTE_SECTION_ATTENDANCE, Navigation.ROUTE_SESSIONS, Navigation.ROUTE_OVERRIDE,
            Navigation.ROUTE_TIMETABLE, Navigation.ROUTE_SUMMARY, Navigation.ROUTE_COMPLIANCE, Navigation.ROUTE_LEAVEOD,
            Navigation.ROUTE_NOTIFICATIONS, Navigation.ROUTE_ANALYTICS, Navigation.ROUTE_STUDENT_ANALYTICS,
            Navigation.ROUTE_STUDENT_REPORTS, Navigation.ROUTE_SCANNER,
            Navigation.ROUTE_MARK_APPEAL
        ),
        UserRole.viewer to listOf(
            Navigation.ROUTE_OVERVIEW, Navigation.ROUTE_ATTENDANCE, Navigation.ROUTE_ATTENDANCE_LOGS, Navigation.ROUTE_SUMMARY, Navigation.ROUTE_ANALYTICS,
            Navigation.ROUTE_STUDENT_ANALYTICS, Navigation.ROUTE_STUDENT_REPORTS, Navigation.ROUTE_PARENT_PORTAL
        ),
        UserRole.student to listOf(
            Navigation.ROUTE_STUDENT_PORTAL, Navigation.ROUTE_STUDENT_OVERVIEW, Navigation.ROUTE_STUDENT_ATTENDANCE, Navigation.ROUTE_STUDENT_PERFORMANCE,
            Navigation.ROUTE_STUDENT_ACADEMICS, Navigation.ROUTE_STUDENT_OD, Navigation.ROUTE_STUDENT_CERTIFICATES, Navigation.ROUTE_PARENT_PORTAL
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

    fun canViewField(role: UserRole?, fieldName: String): Boolean {
        if (role == UserRole.viewer) {
            val blocked = listOf("phone", "email", "parentName", "aadhaar", "address")
            return !blocked.contains(fieldName)
        }
        return true
    }

    fun maskPII(role: UserRole?, value: String, isEmail: Boolean = false): String {
        if (role != UserRole.viewer || value.isEmpty()) return value
        return if (isEmail) {
            val parts = value.split("@")
            if (parts.size == 2) {
                val first = parts[0]
                if (first.length > 2) {
                    first.first() + "***" + first.last() + "@" + parts[1]
                } else {
                    "***@" + parts[1]
                }
            } else {
                "***"
            }
        } else {
            // Assume phone or generic string
            if (value.length > 4) {
                value.take(2) + "****" + value.takeLast(2)
            } else {
                "****"
            }
        }
    }
}

