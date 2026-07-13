package com.vfstr.smartclass.domain.models

import java.time.LocalDateTime

enum class UserRole {
    superadmin, admin, faculty, viewer, student
}

data class User(
    val id: String,
    val username: String,
    val displayName: String,
    val role: UserRole,
    val department: String?,
    val email: String? = null,
    val permissions: List<String> = emptyList(),
    val isActive: Boolean = true
)

enum class AttendanceStatus {
    Present, Absent, Late, Override, OD
}

enum class AttendanceSource {
    Camera, Pencil, Override, OD
}

data class AttendanceEvent(
    val id: String,
    val studentId: String,
    val studentName: String?,
    val rollNo: String,
    val sessionId: String?,
    val roomId: String?,
    val roomName: String?,
    val room: String, // Keep for backward compatibility/simpler UI
    val section: String,
    val department: String,
    val confidence: Float,
    val status: AttendanceStatus,
    val source: AttendanceSource,
    val timestamp: String,
    val overrideReason: String? = null
)

enum class SessionStatus {
    Active, Ended, Scheduled
}

data class ClassSession(
    val id: String,
    val roomId: String?,
    val roomName: String?,
    val room: String,
    val subjectCode: String,
    val subjectName: String,
    val section: String,
    val department: String,
    val year: String,
    val facultyId: String?,
    val facultyName: String?,
    val sessionType: String, // "L"|"T"|"P"
    val startTime: String,
    val endTime: String? = null,
    val status: SessionStatus,
    val attendanceCount: Int,
    val totalStudents: Int,
    val durationMinutes: Int = 50,
    val timetableSlotId: String? = null,
    val bleKey: String? = null
)

data class Student(
    val id: String,
    val studentId: String?,
    val name: String,
    val rollNo: String,
    val department: String,
    val year: String,
    val section: String,
    val email: String? = null,
    val phone: String? = null,
    val faceEnrolled: Boolean,
    val faceEnrolledAt: String? = null,
    val faceImagesCount: Int = 0,
    val biometricConsent: Boolean,
    val dob: String? = null,
    val studentPortalEnabled: Boolean = false,
    val isActive: Boolean = true,
    val createdAt: String? = null,
    val cgpa: Double = 0.0,
    val attendancePercentage: Map<String, Double> = emptyMap(),
    val attendanceHistory: List<AttendanceEvent> = emptyList()
)

data class TimetableSlot(
    val id: String,
    val dayOfWeek: String, // Monday, Tuesday ... or "1", "2" ...
    val period: Int, // 1 to 8
    val startTime: String,
    val endTime: String,
    val subjectCode: String,
    val subjectName: String,
    val sessionType: String, // "L"|"T"|"P"
    val room: String,
    val roomId: String? = null,
    val roomName: String? = null,
    val facultyId: String? = null,
    val facultyName: String,
    val department: String,
    val year: String,
    val section: String,
    val isLab: Boolean = false
)

enum class StepState { PENDING, COMPLETE, REJECTED }

enum class EnrollmentStatus { DRAFT, PENDING, APPROVED, REJECTED, NEEDS_REVISION }

enum class PhotoType { FRONT, LEFT, RIGHT }

enum class StudentActiveStatus { active, inactive }

data class StudentPhoto(
    val photoType: PhotoType,
    val filePath: String
)

data class EnrollmentStudent(
    val id: String,
    val studentId: String,
    val rollNumber: String,
    val name: String,
    val branch: String,
    val year: String,
    val section: String,
    val mobileNumber: String?,
    val email: String?,
    val dateOfBirth: String?,
    val dateOfEnrollment: String?,
    val status: StudentActiveStatus,
    val createdAt: String?,
    val updatedAt: String?,
    val photos: List<StudentPhoto>
)

data class EnrollmentRequest(
    val id: String,
    val rollNumber: String,
    val name: String,
    val branch: String,
    val year: String,
    val section: String,
    val mobileNumber: String?,
    val dateOfBirth: String?,
    val status: EnrollmentStatus,
    val submittedAt: String?,
    val reviewedAt: String?,
    val reviewComments: String?,
    val createdAt: String,
    val updatedAt: String,
    val notifications: List<EnrollmentNotification> = emptyList()
)

data class EnrollmentNotification(
    val id: String,
    val message: String,
    val timestamp: String,
    val isRead: Boolean
)

data class DashboardStats(
    val totalStudents: Int,
    val activeStudents: Int,
    val inactiveStudents: Int,
    val branches: List<BranchStat>,
    val years: List<YearStat>,
    val recentEnrollments: List<StudentListItem>
)

data class BranchStat(
    val branch: String,
    val count: Int
)

data class YearStat(
    val year: String,
    val count: Int
)

data class StudentListItem(
    val id: String,
    val name: String,
    val rollNumber: String,
    val branch: String,
    val year: String,
    val section: String,
    val status: EnrollmentStatus,
    val submittedAt: String
)

data class BulkUploadResult(
    val created: Int,
    val skipped: Int,
    val errors: List<String>
)

data class PaginatedResponse<T>(
    val items: List<T>,
    val total: Int,
    val page: Int,
    val pageSize: Int,
    val totalPages: Int
)

data class EnrollmentRequestPayload(
    val rollNumber: String,
    val name: String,
    val branch: String,
    val year: String,
    val section: String,
    val mobileNumber: String?,
    val dateOfBirth: String?
)

data class StudentUpdatePayload(
    val name: String? = null,
    val branch: String? = null,
    val year: String? = null,
    val section: String? = null,
    val mobileNumber: String? = null,
    val email: String? = null,
    val dateOfBirth: String? = null,
    val status: StudentActiveStatus? = null
)

data class ApprovalStep(
    val id: String,
    val stepTitle: String,
    val roleLabel: String,
    val state: StepState,
    val actor: String? = null,
    val timestamp: String? = null,
    val comment: String? = null
)

data class ODRequest(
    val id: String,
    val studentId: String,
    val studentName: String?,
    val rollNo: String,
    val section: String,
    val department: String?,
    val year: Int?,
    val eventType: String? = null,
    val eventName: String,
    val eventDate: String,
    val dates: String, // For backward compatibility
    val status: String, // PENDING, FACULTY_APPROVED, HOD_APPROVED, APPROVED, REJECTED
    val reason: String? = null,
    val duration: Int? = null,
    val supportingDocUrl: String = "",
    val approvalSteps: List<ApprovalStep> = emptyList(),
    val appliedOn: String? = null,
    val requestedBy: String? = null,
    val facultyApprovedBy: String? = null,
    val hodApprovedBy: String? = null,
    val reviewedBy: String? = null,
    val reviewNote: String? = null,
    val rejectionReason: String? = null,
    val resolvedAt: String? = null,
    val reviewedAt: String? = null
)

data class CondonationRequest(
    val id: String,
    val studentId: String,
    val studentName: String?,
    val rollNo: String,
    val department: String?,
    val year: Int?,
    val section: String?,
    val subjectId: Int?,
    val subjectCode: String?,
    val subjectName: String?,
    val attendanceRate: Double?,
    val requiredRate: Double?,
    val reason: String,
    val supportingDocPath: String? = null,
    val appliedOn: String? = null,
    val createdAt: String? = null,
    val hodId: String? = null,
    val hodDecision: String? = null,
    val hodRemarks: String? = null,
    val sessionsCondoned: Int? = null,
    val resolvedAt: String? = null,
    val status: String?,
    val approvalHistory: List<ApprovalStep> = emptyList()
)

data class Holiday(
    val id: String,
    val name: String,
    val date: String,
    val type: String, // College, Government, Regional, Exam Suspension
    val description: String? = null,
    val academicYear: String = ""
)

data class DeviceStatus(
    val id: String,
    val name: String,
    val room: String,
    val status: Boolean, // true = online, false = offline
    val lastSeen: String,
    val eventsCount: Int,
    val ipAddress: String,
    val version: String
)

data class AuditLog(
    val id: String,
    val timestamp: String,
    val userId: String,
    val userName: String,
    val action: String, // CREATE, UPDATE, DELETE, LOGIN, SYSTEM_CONFIG
    val resource: String,
    val ipAddress: String?,
    val details: Map<String, Any>? = null
)

data class Guardian(
    val id: String,
    val studentId: String,
    val name: String,
    val relation: String, // father, mother, guardian
    val phone: String,
    val email: String? = null,
    val isPrimary: Boolean
)

data class MOOCEnrollment(
    val id: Int,
    val studentId: String,
    val studentName: String?,
    val department: String?,
    val year: Int?,
    val section: String?,
    val courseName: String,
    val platform: String, // swayam, nptel, coursera, other
    val credits: Int,
    val status: String, // enrolled, completed, dropped
    val completionDate: String? = null,
    val certificateUrl: String? = null,
    val enrolledAt: String
)

data class MOOCStats(
    val totalEnrollments: Int,
    val completed: Int,
    val inProgress: Int,
    val dropped: Int,
    val totalCreditsEarned: Int,
    val byPlatform: Map<String, PlatformStats>
)

data class PlatformStats(
    val enrolled: Int,
    val completed: Int,
    val credits: Int
)

data class NotificationPreference(
    val id: String,
    val studentId: String,
    val guardianId: String,
    val enabled: Boolean,
    val types: List<String>,
    val frequency: String,
    val whatsappEnabled: Boolean,
    val smsEnabled: Boolean,
    val emailEnabled: Boolean
)
