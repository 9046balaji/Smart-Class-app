package com.vfstr.smartclass.data.repositories

import com.vfstr.smartclass.data.remote.api.EnrollmentApiService
import com.vfstr.smartclass.data.remote.api.PaginatedResponse as ApiPaginatedResponse
import com.vfstr.smartclass.domain.models.*
import okhttp3.ResponseBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StudentManagementRepository @Inject constructor(
    private val apiService: EnrollmentApiService
) {
    suspend fun getEnrollmentStats(): DashboardStats {
        return apiService.getEnrollmentStats()
    }

    suspend fun getEnrollmentRequests(
        page: Int,
        perPage: Int,
        status: String? = null
    ): PaginatedResponse<StudentListItem> {
        val res: ApiPaginatedResponse<StudentListItem> = apiService.getEnrollmentRequests(page, perPage, status)
        return PaginatedResponse(
            items = res.items,
            total = res.total,
            page = res.page,
            pageSize = res.page_size,
            totalPages = res.total_pages
        )
    }

    suspend fun approveRequest(id: String): EnrollmentRequest {
        return apiService.approveEnrollmentRequest(id)
    }

    suspend fun rejectRequest(id: String, comment: String): EnrollmentRequest {
        return apiService.rejectEnrollmentRequest(id, mapOf("comment" to comment))
    }

    suspend fun requestRevision(id: String, comment: String): EnrollmentRequest {
        return apiService.requestRevision(id, mapOf("comment" to comment))
    }

    suspend fun getStudents(
        filters: Map<String, String>,
        page: Int,
        perPage: Int
    ): PaginatedResponse<StudentListItem> {
        val res: ApiPaginatedResponse<StudentListItem> = apiService.getStudents(filters, page, perPage)
        return PaginatedResponse(
            items = res.items,
            total = res.total,
            page = res.page,
            pageSize = res.page_size,
            totalPages = res.total_pages
        )
    }

    suspend fun getStudentProfile(id: String): EnrollmentStudent {
        return apiService.getStudentProfile(id)
    }

    suspend fun updateStudent(id: String, payload: StudentUpdatePayload): EnrollmentStudent {
        return apiService.updateStudent(id, payload)
    }

    suspend fun deactivateStudent(id: String) {
        apiService.deactivateStudent(id)
    }

    suspend fun reactivateStudent(id: String) {
        apiService.reactivateStudent(id)
    }

    suspend fun bulkUpload(students: List<EnrollmentRequestPayload>): BulkUploadResult {
        return apiService.bulkUploadStudents(students)
    }

    suspend fun exportStudents(): ResponseBody {
        return apiService.exportStudents()
    }
}
