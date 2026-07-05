package com.vfstr.smartclass.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LocalEnrollmentStudent(
    val student_id: String,
    val name: String,
    val roll_number: String,
    val branch: String,     // raw branch code from CSV e.g. "CS", "AIANDML"
    val year: String,       // admission year e.g. "2023"
    val section: String,    // e.g. "A", "B"
    val dob: String,        // "YYYY-MM-DD"
    val photo_path: String, // relative path
) : Parcelable

typealias StudentHierarchy = Map<String, Map<String, Map<String, List<LocalEnrollmentStudent>>>>

@Parcelize
data class AttendanceScanResult(
    val status: String,          // marked, wrong_section, success, low_confidence, not_recognized, no_face, error
    val message: String?,
    val student_name: String?,
    val student_section: String?,
    val confidence: Double?,     // 0.0–1.0
    val marked_count: Int?,      // for group photo
    val scan_count: Int?,        // faces detected
    val results: List<ScanResultItem>?,  // per-face results for group photo
) : Parcelable

@Parcelize
data class ScanResultItem(
    val status: String,
    val student_name: String?,
    val studentName: String?,
    val name: String?,
    val student_section: String?,
    val confidence: Double?,
) : Parcelable

fun ScanResultItem.displayName(): String =
    student_name ?: studentName ?: name ?: "Unknown"

data class MarkBulkRequest(
    val session_id: String,
    val present_student_ids: List<String>,
    val marked_via: String = "bulk"
)

data class MarkBulkResponse(
    val marked: Int,
    val updated: Int,
    val errors: List<Any>,
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
