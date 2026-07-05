package com.vfstr.smartclass.data.remote.api

import com.vfstr.smartclass.domain.models.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface EnrollmentApiService {

    // --- Student Endpoints ---

    /**
     * Submit new enrollment registration with 3 face photos.
     * Uses @Multipart with named @Part parameters for each photo and form fields.
     */
    @Multipart
    @POST("api/students/enroll")
    suspend fun enrollStudent(
        @Part("roll_number") rollNumber: RequestBody,
        @Part("name") name: RequestBody,
        @Part("branch") branch: RequestBody,
        @Part("year") year: RequestBody,
        @Part("section") section: RequestBody,
        @Part("mobile_number") mobileNumber: RequestBody?,
        @Part("date_of_birth") dob: RequestBody?,
        @Part photo_front: MultipartBody.Part,
        @Part photo_left: MultipartBody.Part,
        @Part photo_right: MultipartBody.Part
    ): EnrollmentRequest

    @GET("api/enrollment-requests/{id}")
    suspend fun getEnrollmentRequest(@Path("id") id: String): EnrollmentRequest

    @POST("api/enrollment-requests")
    suspend fun submitEnrollmentRequest(@Body payload: EnrollmentRequestPayload): EnrollmentRequest

    @PUT("api/enrollment-requests/{id}")
    suspend fun updateEnrollmentRequest(
        @Path("id") id: String,
        @Body payload: EnrollmentRequestPayload
    ): EnrollmentRequest

    @GET("api/enrollment-requests/{id}/notifications")
    suspend fun getEnrollmentNotifications(@Path("id") id: String): List<EnrollmentNotification>

    @POST("api/enrollment-requests/{id}/notifications/read")
    suspend fun markNotificationsRead(@Path("id") id: String): Map<String, Any>


    // --- Admin Endpoints ---

    @GET("api/students/stats")
    suspend fun getEnrollmentStats(): DashboardStats

    @GET("api/enrollment-requests")
    suspend fun getEnrollmentRequests(
        @Query("page") page: Int,
        @Query("per_page") perPage: Int,
        @Query("status") status: String? = null
    ): PaginatedResponse<StudentListItem>

    @PUT("api/enrollment-requests/{id}/approve")
    suspend fun approveEnrollmentRequest(@Path("id") id: String): EnrollmentRequest

    @PUT("api/enrollment-requests/{id}/reject")
    suspend fun rejectEnrollmentRequest(
        @Path("id") id: String,
        @Body body: Map<String, String> // { "comment": "string" }
    ): EnrollmentRequest

    @PUT("api/enrollment-requests/{id}/request-revision")
    suspend fun requestRevision(
        @Path("id") id: String,
        @Body body: Map<String, String> // { "comment": "string" }
    ): EnrollmentRequest

    @GET("api/students")
    suspend fun getStudents(
        @QueryMap filters: Map<String, String>,
        @Query("page") page: Int,
        @Query("per_page") perPage: Int
    ): PaginatedResponse<StudentListItem>

    @GET("api/students/{id}")
    suspend fun getStudentProfile(@Path("id") id: String): EnrollmentStudent

    @PUT("api/students/{id}")
    suspend fun updateStudent(
        @Path("id") id: String,
        @Body payload: StudentUpdatePayload
    ): EnrollmentStudent

    @DELETE("api/students/{id}")
    suspend fun deactivateStudent(@Path("id") id: String): Map<String, Any>

    @POST("api/students/reactivate/{id}")
    suspend fun reactivateStudent(@Path("id") id: String): Map<String, Any>

    @POST("api/students/bulk")
    suspend fun bulkUploadStudents(@Body students: List<EnrollmentRequestPayload>): BulkUploadResult

    @GET("api/students/export")
    @Streaming
    suspend fun exportStudents(): okhttp3.ResponseBody
}
