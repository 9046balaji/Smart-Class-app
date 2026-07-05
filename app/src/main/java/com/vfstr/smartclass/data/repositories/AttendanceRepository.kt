package com.vfstr.smartclass.data.repositories

import com.vfstr.smartclass.data.remote.api.*
import com.vfstr.smartclass.domain.models.*
import com.vfstr.smartclass.data.remote.api.MarkBulkRequest as ApiMarkBulkRequest
import com.vfstr.smartclass.data.remote.api.MarkBulkResponse as ApiMarkBulkResponse
import com.vfstr.smartclass.data.remote.api.RecognizeRequest as ApiRecognizeRequest
import com.vfstr.smartclass.data.remote.api.ManualMarkRequest as ApiManualMarkRequest
import com.vfstr.smartclass.data.remote.api.PaginatedResponse as ApiPaginatedResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttendanceRepository @Inject constructor(
    private val api: RetrofitApi
) {
    suspend fun markBulk(req: MarkBulkRequest): MarkBulkResponse {
        val apiReq = ApiMarkBulkRequest(req.session_id, req.present_student_ids, req.marked_via)
        val res: ApiMarkBulkResponse = api.markBulk(apiReq)
        return MarkBulkResponse(res.marked, res.updated, res.errors)
    }

    suspend fun getSectionStudents(sectionId: String): List<StudentDto> {
        return api.getSectionStudents(sectionId)
    }

    suspend fun recognizeFace(sectionId: String, req: RecognizeRequest): AttendanceScanResult {
        val apiReq = ApiRecognizeRequest(req.image, req.section_id, req.session_id, req.latitude, req.longitude)
        return api.recognizeFace(sectionId, apiReq)
    }

    suspend fun markManual(sectionId: String, req: ManualMarkRequest): AttendanceScanResult {
        val apiReq = ApiManualMarkRequest(req.student_id, req.session_id)
        return api.markManual(sectionId, apiReq)
    }

    suspend fun getActiveSession(sectionId: String): ActiveSessionResponse {
        return api.getActiveSectionSession(sectionId)
    }

    suspend fun syncIndex(sectionId: String): Map<String, Any> {
        return api.syncIndex(sectionId)
    }

    suspend fun getSessions(filters: Map<String, String> = emptyMap()): List<SessionDto> {
        return api.getSessions(filters)
    }

    suspend fun getAttendanceEvents(filters: Map<String, String>): ApiPaginatedResponse<AttendanceEventDto> {
        return api.getAttendanceEvents(filters)
    }
}
