package com.vfstr.smartclass.data.repositories

import com.vfstr.smartclass.data.remote.api.EnrollmentApiService
import com.vfstr.smartclass.domain.models.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EnrollmentRepository @Inject constructor(
    private val apiService: EnrollmentApiService
) {
    suspend fun enrollStudent(
        payload: EnrollmentRequestPayload,
        photoFront: File,
        photoLeft: File,
        photoRight: File
    ): EnrollmentRequest {
        val rollNumber = payload.rollNumber.toRequestBody("text/plain".toMediaTypeOrNull())
        val name = payload.name.toRequestBody("text/plain".toMediaTypeOrNull())
        val branch = payload.branch.toRequestBody("text/plain".toMediaTypeOrNull())
        val year = payload.year.toRequestBody("text/plain".toMediaTypeOrNull())
        val section = payload.section.toRequestBody("text/plain".toMediaTypeOrNull())
        val mobileNumber = payload.mobileNumber?.toRequestBody("text/plain".toMediaTypeOrNull())
        val dob = payload.dateOfBirth?.toRequestBody("text/plain".toMediaTypeOrNull())

        val photoFrontPart = MultipartBody.Part.createFormData(
            "photo_front",
            photoFront.name,
            photoFront.asRequestBody("image/*".toMediaTypeOrNull())
        )
        val photoLeftPart = MultipartBody.Part.createFormData(
            "photo_left",
            photoLeft.name,
            photoLeft.asRequestBody("image/*".toMediaTypeOrNull())
        )
        val photoRightPart = MultipartBody.Part.createFormData(
            "photo_right",
            photoRight.name,
            photoRight.asRequestBody("image/*".toMediaTypeOrNull())
        )

        return apiService.enrollStudent(
            rollNumber, name, branch, year, section, mobileNumber, dob,
            photoFrontPart, photoLeftPart, photoRightPart
        )
    }

    suspend fun getEnrollmentRequest(id: String): EnrollmentRequest {
        return apiService.getEnrollmentRequest(id)
    }

    suspend fun submitEnrollmentRequest(payload: EnrollmentRequestPayload): EnrollmentRequest {
        return apiService.submitEnrollmentRequest(payload)
    }

    suspend fun updateEnrollmentRequest(id: String, payload: EnrollmentRequestPayload): EnrollmentRequest {
        return apiService.updateEnrollmentRequest(id, payload)
    }

    suspend fun getNotifications(id: String): List<EnrollmentNotification> {
        return apiService.getEnrollmentNotifications(id)
    }

    suspend fun markNotificationsRead(id: String) {
        apiService.markNotificationsRead(id)
    }
}
