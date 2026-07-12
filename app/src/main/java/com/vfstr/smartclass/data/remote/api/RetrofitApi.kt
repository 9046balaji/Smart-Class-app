package com.vfstr.smartclass.data.remote.api

import com.vfstr.smartclass.domain.models.*
import okhttp3.MultipartBody
import retrofit2.http.*

// Request & Response DTO definitions

data class TokenResponse(val access_token: String, val refresh_token: String? = null, val token_type: String? = null)

data class ActiveSessionResponse(
    val session_id: String?,
    val active: Boolean,
    val subject: String?
)

data class MarkBulkRequest(
    val session_id: String,
    val present_student_ids: List<String>,
    val marked_via: String = "bulk"
)

data class MarkBulkResponse(
    val marked: Int,
    val updated: Int,
    val errors: List<Any>
)

data class RecognizeRequest(
    val image: String,
    val section_id: String,
    val session_id: String?,
    val latitude: Double?,
    val longitude: Double?
)

data class ManualMarkRequest(
    val student_id: String,
    val session_id: String?
)

data class BleCheckInRequest(
    val encrypted_token: String
)

data class SendNotificationPayload(
    val student_ids: List<String>? = null,
    val department: String? = null,
    val year: Int? = null,
    val section: String? = null,
    val channel: String,          // "sms"|"email"|"both"
    val message_type: String,     // "warning_85"|"critical_75"|"review_cycle"|"custom"
    val subject: String? = null,
    val message: String,
    val week: Int? = null
)

data class ProfileResponse(
    val id: String,
    val username: String,
    val full_name: String? = null,
    val name: String? = null,
    val role: String,
    val is_active: Boolean,
    val department: String? = null,
    val email: String? = null,
    val permissions: List<String>? = null,
    val student_id: String? = null,
    val year: Int? = null,
    val section: String? = null
)

data class DashboardStatsDto(
    val present_today: Int? = null,
    val total_students: Int? = null,
    val events_today: Int? = null,
    val active_sessions: Int? = null,
    val online_devices: Int? = null,
    val total_devices: Int? = null,
    val offline_alerts: Int? = null,
    val face_enrolled: Int? = null,
    val pending_enrollment: Int? = null,
    val review_week: Int? = null,
    // Original fields for backward compatibility or if backend varies
    val presentCount: Int? = null,
    val totalCount: Int? = null,
    val activeSessions: Int? = null,
    val eventsCount: Int? = null,
    val onlineDevices: Int? = null,
    val totalDevices: Int? = null,
    val offlineAlerts: Int? = null,
    val reviewWeek: Int? = null,
    val rates: AttendanceRatesDto? = null
)

data class AttendanceRatesDto(
    val today: Float,
    val weekly: Float,
    val monthly: Float
)

data class AttendanceEventDto(
    val id: String,
    val student_id: String,
    val student_name: String? = null,
    val roll_no: String? = null,
    val session_id: String? = null,
    val room_id: String? = null,
    val room_name: String? = null,
    val room: String? = null,
    val section: String? = null,
    val department: String? = null,
    val timestamp: String,
    val confidence: Float,
    val status: String,
    val source: String? = null,
    val override_reason: String? = null
)

data class PaginatedResponse<T>(
    val items: List<T>,
    val total: Int,
    val page: Int,
    val page_size: Int,
    val total_pages: Int
)

data class SessionDto(
    val id: String,
    val room_id: String? = null,
    val room_name: String? = null,
    val room: String? = null,
    val subject_code: String? = null,
    val subject_name: String? = null,
    val subject: String? = null,
    val section: String? = null,
    val department: String? = null,
    val year: String? = null,
    val faculty_id: String? = null,
    val faculty_name: String? = null,
    val session_type: String? = null, // "L"|"T"|"P"
    val start_time: String? = null,
    val started_at: String? = null,
    val end_time: String? = null,
    val ended_at: String? = null,
    val status: String? = null, // "active"|"ended"|"scheduled"
    val attendance_count: Int? = null,
    val count: Int? = null,
    val total_students: Int? = null,
    val total: Int? = null,
    val timetable_slot_id: String? = null,
    val duration_minutes: Int? = null
)

data class CreateSessionPayload(
    val room_id: String,
    val class_id: String,
    val subject: String? = null
)

data class EndSessionBody(val backfill_absent: Boolean = true)

data class CreateOverridePayload(
    val student_id: String,
    val session_id: String,
    val status: String,
    val reason: String
)

data class StudentDto(
    val id: String,
    val student_id: String? = null,
    val name: String,
    val roll_number: String? = null,
    val roll_no: String? = null,
    val department: String,
    val year: Any, // Can be Int or String
    val section: String,
    val email: String? = null,
    val phone: String? = null,
    val face_enrolled: Boolean,
    val face_enrolled_at: String? = null,
    val face_images_count: Int? = null,
    val biometric_consent: Boolean,
    val dob: String? = null,
    val student_portal_enabled: Boolean? = null,
    val is_active: Boolean,
    val created_at: String? = null,
    val cgpa: Double? = null
)

data class StudentStatsDto(
    val total: Int,
    val face_enrolled: Int,
    val pending_enrollment: Int,
    val by_department: Map<String, Int>
)

data class TimetableSlotDto(
    val id: String,
    val day_of_week: Any, // Int or String
    val period_number: Int? = null,
    val period: Int? = null,
    val start_time: String,
    val end_time: String,
    val subject_code: String,
    val subject_name: String? = null,
    val faculty_id: String? = null,
    val faculty_name: String? = null,
    val room_id: String,
    val room_name: String? = null,
    val section: String,
    val department: String,
    val year: Any,
    val session_type: String,
    val is_lab: Boolean? = null
)

data class ReviewCycleDto(
    val id: Int,
    val week_number: Int,
    val start_date: String,
    val end_date: String,
    val status: String,
    val reviewed_by: String? = null,
    val reviewed_at: String? = null
)

data class RunReviewPayload(
    val cycle_id: Int? = null,
    val week_number: Int,
    val branch: String? = null,
    val year: Int? = null,
    val section: String? = null
)

data class ODRequestDto(
    val id: String,
    val student_id: String,
    val student_name: String? = null,
    val roll_number: String? = null,
    val status: String,
    val applied_on: String? = null,
    val created_at: String? = null,
    val requested_at: String? = null,
    val requested_by: String? = null,
    val faculty_approved_by: String? = null,
    val hod_approved_by: String? = null,
    val reviewed_by: String? = null,
    val review_note: String? = null,
    val rejection_reason: String? = null,
    val resolved_at: String? = null,
    val reviewed_at: String? = null,
    val event_name: String? = null,
    val event_date: String? = null,
    val reason: String? = null,
    val duration: Int? = null
)

data class CondonationRequestDto(
    val id: String,
    val student_id: String,
    val student_name: String? = null,
    val roll_number: String? = null,
    val department: String? = null,
    val year: Int? = null,
    val section: String? = null,
    val subject_id: Int? = null,
    val subject_code: String? = null,
    val subject_name: String? = null,
    val attendance_rate: Double? = null,
    val required_rate: Double? = null,
    val reason: String,
    val supporting_doc_path: String? = null,
    val applied_on: String? = null,
    val created_at: String? = null,
    val hod_id: String? = null,
    val hod_decision: String? = null,
    val hod_remarks: String? = null,
    val sessions_condoned: Int? = null,
    val resolved_at: String? = null,
    val status: String? = null,
    val approval_history: List<ApprovalHistoryItemDto>? = null
)

data class ApprovalHistoryItemDto(
    val step: String,
    val actor: String,
    val action: String,
    val timestamp: String,
    val comment: String? = null
)

data class GuardianDto(
    val id: String,
    val student_id: String,
    val name: String,
    val relation: String,
    val phone: String,
    val email: String? = null,
    val is_primary: Boolean
)

data class MOOCEnrollmentDto(
    val id: Int,
    val student_id: String,
    val student_name: String? = null,
    val department: String? = null,
    val year: Int? = null,
    val section: String? = null,
    val course_name: String,
    val platform: String? = null,
    val credits: Int? = null,
    val status: String? = null,
    val completion_status: String? = null,
    val completion_date: String? = null,
    val certificate_url: String? = null,
    val enrolled_at: String? = null
)

data class StudentAttendanceRecordDto(
    val id: String,
    val start_time: String,
    val subject_name: String,
    val status: String,
    val marked_via: String,
    val marked_at: String? = null
)

data class StudentMarksDto(
    val subject_code: String,
    val subject_name: String,
    val semester: String,
    val mid1: Float?,
    val mid2: Float?,
    val assignment: Float?,
    val attendance: Float?,
    val total_obtained: Float?,
    val total_max: Float
)

data class SemesterResultSubjectDto(
    val subject_code: String,
    val subject_name: String,
    val grade: String,
    val credits: Int,
    val grade_points: Int
)

data class SemesterResultDto(
    val semester: String,
    val sgpa: Float,
    val cgpa: Float,
    val subjects: List<SemesterResultSubjectDto>
)


data class BacklogRecordDto(
    val subject_code: String,
    val subject_name: String,
    val semester_failed: String,
    val attempts: Int,
    val next_exam_window: String,
    val status: String
)

data class BacklogsSummaryDto(
    val active_backlogs: List<BacklogRecordDto>,
    val cleared_backlogs_count: Int
)

data class MentorDto(
    val mentor_name: String,
    val mentor_email: String,
    val mentor_phone: String,
    val office_hours: String,
    val department: String
)


data class PlatformStatsDto(
    val enrolled: Int,
    val completed: Int,
    val credits: Int
)

data class MOOCStatsDto(
    val total_enrollments: Int,
    val completed: Int,
    val in_progress: Int,
    val dropped: Int,
    val total_credits_earned: Int,
    val by_platform: Map<String, PlatformStatsDto>
)

data class UserDto(
    val id: String,
    val username: String,
    val email: String? = null,
    val full_name: String? = null,
    val name: String? = null,
    val role: String,
    val is_active: Boolean,
    val department: String? = null,
    val permissions: List<String>? = null
)

data class AuditLogDto(
    val id: String,
    val timestamp: String,
    val user_id: String,
    val user_name: String,
    val action: String,
    val resource: String,
    val ip_address: String? = null,
    val details: Map<String, Any>? = null
)

data class HolidayDto(
    val id: String,
    val name: String,
    val date: String,
    val type: String,
    val description: String? = null,
    val academic_year: String
)

data class FrontendEventPayload(
    val event: String,
    val properties: Map<String, Any>,
    val timestamp: String,
    val userId: String? = null
)

data class AppVersionDto(
    val version_code: Int,
    val version_name: String,
    val mandatory: Boolean,
    val download_url: String,
    val release_notes: String? = null
)

data class StudentAttendanceDto(
    val subject_name: String,
    val attended: Int,
    val total: Int,
    val percentage: Double,
    val eligibility: String
)

data class StudentEligibilityDto(
    val overall_percentage: Double,
    val overall_status: String,
    val subjects: List<StudentAttendanceDto>
)

data class StudentMOOCEnrollPayload(
    val course_name: String,
    val platform: String,
    val course_code: String? = null,
    val credits: Int? = null,
    val enrollment_date: String? = null,
    val semester: Int? = null,
    val academic_year: String? = null
)

data class StudentODRequestPayload(
    val eventName: String,
    val eventDate: String,
    val duration: Int,
    val reason: String
)

interface RetrofitApi {

    // Auth
    @POST("auth/token")
    @Multipart
    suspend fun loginStaff(
        @Part("username") username: okhttp3.RequestBody,
        @Part("password") password: okhttp3.RequestBody
    ): TokenResponse

    @POST("student/login")
    suspend fun loginStudent(@Body body: Map<String, String>): TokenResponse

    @POST("../auth/refresh")
    suspend fun refreshToken(@Header("Authorization") oldToken: String): TokenResponse

    @GET("../auth/me")
    suspend fun getStaffProfile(): ProfileResponse

    @GET("student/me")
    suspend fun getStudentProfile(): ProfileResponse

    // Filters
    @GET("filters/departments")
    suspend fun getDepartments(): List<String>

    @GET("filters/years")
    suspend fun getYears(): List<Int>

    @GET("filters/sections")
    suspend fun getSections(): List<String>

    @GET("filters/subjects")
    suspend fun getSubjects(): List<String>

    @GET("filters/rooms")
    suspend fun getRooms(): List<String>

    @GET("filters/faculty")
    suspend fun getFaculty(): List<String>

    // Dashboard
    @GET("dashboard/stats")
    suspend fun getDashboardStats(): DashboardStatsDto

    @GET("stats/attendance-rates")
    suspend fun getAttendanceRates(): AttendanceRatesDto

    @GET("events")
    suspend fun getActivityFeed(@Query("limit") limit: Int = 15): List<AttendanceEventDto>

    // Attendance
    @GET("attendance/events")
    suspend fun getAttendanceEvents(
        @QueryMap filters: Map<String, String>
    ): PaginatedResponse<AttendanceEventDto>

    @GET("attendance/alerts")
    suspend fun getAttendanceAlerts(
        @Query("page") page: Int,
        @Query("per_page") perPage: Int
    ): PaginatedResponse<AttendanceEventDto>

    @POST("attendance/mark-bulk")
    suspend fun markBulk(@Body req: MarkBulkRequest): MarkBulkResponse

    @POST("attendance/override")
    suspend fun submitOverride(@Body req: CreateOverridePayload): Map<String, Any>

    @GET("attendance/override")
    suspend fun getOverrideHistory(
        @Query("page") page: Int,
        @Query("per_page") perPage: Int
    ): PaginatedResponse<AttendanceEventDto>

    // Sessions
    @GET("sessions")
    suspend fun getSessions(@QueryMap filters: Map<String, String>): List<SessionDto>

    @POST("sessions")
    suspend fun createSession(@Body session: CreateSessionPayload): SessionDto

    @PATCH("sessions/{id}/start")
    suspend fun startSession(@Path("id") id: String): SessionDto

    @PATCH("sessions/{id}/end")
    suspend fun endSession(@Path("id") id: String, @Body body: EndSessionBody): SessionDto

    @GET("sessions/{id}/attendance")
    suspend fun getSessionAttendance(@Path("id") id: String): List<AttendanceEventDto>

    // Sections
    @GET("sections/{sectionId}/students")
    suspend fun getSectionStudents(@Path("sectionId") sectionId: String): List<StudentDto>

    @GET("sections/{sectionId}/sessions/active")
    suspend fun getActiveSectionSession(@Path("sectionId") sectionId: String): ActiveSessionResponse

    @POST("sections/{sectionId}/attendance/recognize")
    suspend fun recognizeFace(@Path("sectionId") sectionId: String, @Body req: RecognizeRequest): AttendanceScanResult

    @POST("sections/{sectionId}/attendance/mark")
    suspend fun markManual(@Path("sectionId") sectionId: String, @Body req: ManualMarkRequest): AttendanceScanResult

    // Students
    @POST("sections/{sectionId}/sync-index")
    suspend fun syncIndex(@Path("sectionId") sectionId: String): Map<String, Any>

    @GET("students")
    suspend fun getStudents(@QueryMap filters: Map<String, String>): List<StudentDto>

    @GET("students/stats")
    suspend fun getStudentStats(): StudentStatsDto

    @POST("students/import")
    @Multipart
    suspend fun importStudents(@Part file: MultipartBody.Part): List<StudentDto>

    // Timetable
    @GET("timetable")
    suspend fun getTimetable(@QueryMap filters: Map<String, String>): List<TimetableSlotDto>

    @POST("timetable/import")
    @Multipart
    suspend fun importTimetable(@Part file: MultipartBody.Part): List<TimetableSlotDto>

    @POST("timetable/sync-sessions")
    suspend fun syncSessions(): Map<String, Any>

    // Compliance
    @GET("compliance/review-cycles")
    suspend fun getReviewCycles(@QueryMap filters: Map<String, String>): List<ReviewCycleDto>

    @POST("compliance/run-review")
    suspend fun runReview(@Body payload: RunReviewPayload): Map<String, Any>

    @GET("compliance/defaulters")
    suspend fun getDefaulters(@QueryMap filters: Map<String, String>): List<StudentDto>

    @GET("compliance/eligibility")
    suspend fun getEligibility(@QueryMap filters: Map<String, String>): List<StudentDto>

    // Leave & OD
    @GET("od-requests")
    suspend fun getODRequests(@QueryMap filters: Map<String, String>): List<ODRequestDto>

    @POST("od-requests/{id}/review")
    suspend fun reviewODRequest(@Path("id") id: String, @Body body: Map<String, String>): Map<String, Any>

    @GET("condonation-requests")
    suspend fun getCondonationRequests(@QueryMap filters: Map<String, String>): List<CondonationRequestDto>

    @POST("condonation-requests/{id}/review")
    suspend fun reviewCondonationRequest(@Path("id") id: String, @Body body: Map<String, String>): Map<String, Any>

    // Notifications
    @GET("notifications/sms-credits")
    suspend fun getSmsCredits(): Map<String, Int>

    @GET("notifications/log")
    suspend fun getNotificationLog(@QueryMap filters: Map<String, String>): PaginatedResponse<Map<String, Any>>

    @GET("students/{studentId}/guardians")
    suspend fun getGuardians(@Path("studentId") studentId: String): List<GuardianDto>

    @POST("notifications/send")
    suspend fun sendNotification(@Body payload: SendNotificationPayload): Map<String, Any>

    // MOOC
    @GET("mooc/enrollments")
    suspend fun getMOOCEnrollments(@QueryMap filters: Map<String, String>): List<MOOCEnrollmentDto>

    @GET("mooc/stats")
    suspend fun getMOOCStats(): MOOCStatsDto

    // Users & Audit
    @GET("../auth/users")
    suspend fun getUsers(): List<UserDto>

    @GET("audit")
    suspend fun getAuditLogs(@Query("search") search: String?): List<AuditLogDto>

    @POST("audit/frontend-events")
    suspend fun trackEvents(@Body events: List<FrontendEventPayload>): Map<String, Any>

    // Health
    @GET("health")
    suspend fun checkHealth(): Map<String, String>

    @GET("app/version")
    suspend fun getLatestVersion(): AppVersionDto

    // Student Portal Specific
    @GET("student/attendance")
    suspend fun getStudentAttendance(
        @Query("from_date") fromDate: String? = null,
        @Query("to_date") toDate: String? = null
    ): List<StudentAttendanceDto>

    @GET("student/attendance/report")
    suspend fun getStudentAttendanceReport(
        @Query("from_date") fromDate: String? = null,
        @Query("to_date") toDate: String? = null
    ): List<StudentAttendanceRecordDto>

    @GET("student/od-requests")
    suspend fun getStudentODRequests(): List<ODRequestDto>

    @POST("student/od-requests")
    suspend fun submitStudentODRequest(@Body body: StudentODRequestPayload): Map<String, Any>

    @GET("student/eligibility")
    suspend fun getStudentEligibility(
        @Query("from_date") fromDate: String? = null,
        @Query("to_date") toDate: String? = null
    ): StudentEligibilityDto

    @GET("student/mooc")
    suspend fun getStudentMOOCs(): List<MOOCEnrollmentDto>

    @POST("student/mooc/enroll")
    suspend fun enrollStudentMOOC(@Body body: StudentMOOCEnrollPayload): MOOCEnrollmentDto

    @POST("student/change-password")
    suspend fun changeStudentPassword(@Body body: Map<String, String>): Map<String, String>

    @GET("student/marks")
    suspend fun getStudentMarks(
        @Query("semester") semester: String? = null
    ): List<StudentMarksDto>

    @GET("student/results")
    suspend fun getStudentResults(): List<SemesterResultDto>

    @GET("student/backlogs")
    suspend fun getStudentBacklogs(): BacklogsSummaryDto

    @GET("student/mentor")
    suspend fun getStudentMentor(): MentorDto

    @POST("student/attendance/check-in")
    suspend fun submitBleCheckIn(@Body req: BleCheckInRequest): AttendanceScanResult
}
