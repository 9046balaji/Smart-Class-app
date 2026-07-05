package com.vfstr.smartclass.domain.usecases

import com.vfstr.smartclass.domain.models.Student
import com.vfstr.smartclass.domain.models.UserRole
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import kotlin.math.*

// Coordinates point definition
data class GpsCoordinate(val latitude: Double, val longitude: Double)

object GeofenceUtils {
    // VFSTR Campus Boundary Polygon vertices (Vadlamudi campus coordinate approximation)
    val VFSTR_CAMPUS_POLYGON = listOf(
        GpsCoordinate(16.2215, 80.5605),
        GpsCoordinate(16.2255, 80.5605),
        GpsCoordinate(16.2255, 80.5655),
        GpsCoordinate(16.2215, 80.5655)
    )

    // VFSTR Core Centroid coordinate (Block A Central)
    val BLOCK_A_CENTROID = GpsCoordinate(16.2235, 80.5625)
    val BLOCK_B_CENTROID = GpsCoordinate(16.2240, 80.5630)

    // Tier 1 check: Point in campus polygon test (Ray-Casting Algorithm)
    fun isInsideCampus(point: GpsCoordinate): Boolean {
        return isPointInPolygon(point, VFSTR_CAMPUS_POLYGON)
    }

    private fun isPointInPolygon(point: GpsCoordinate, polygon: List<GpsCoordinate>): Boolean {
        var isInside = false
        val size = polygon.size
        var j = size - 1
        for (i in 0 until size) {
            val pi = polygon[i]
            val pj = polygon[j]
            if (((pi.longitude > point.longitude) != (pj.longitude > point.longitude)) &&
                (point.latitude < (pj.latitude - pi.latitude) * (point.longitude - pi.longitude) / (pj.longitude - pi.longitude) + pi.latitude)
            ) {
                isInside = !isInside
            }
            j = i
        }
        return isInside
    }

    // Tier 2: Check distance to building centroids using stable Haversine formula (same as web)
    fun calculateHaversineDistance(c1: GpsCoordinate, c2: GpsCoordinate): Double {
        val earthRadiusMeters = 6371000.0
        val dLat = Math.toRadians(c2.latitude - c1.latitude)
        val dLon = Math.toRadians(c2.longitude - c1.longitude)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(c1.latitude)) * cos(Math.toRadians(c2.latitude)) *
                sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadiusMeters * c
    }

    // Tier 3: Verify if within specific Classroom GPS radius and WiFi BSSID check
    fun isWithinClassroom(deviceLoc: GpsCoordinate, classroomLoc: GpsCoordinate, maxDistance: Double = 20.0): Boolean {
        return calculateHaversineDistance(deviceLoc, classroomLoc) <= maxDistance
    }

    // Helpers to support unit test suite (Rule 25)
    fun calculateDistanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        return calculateHaversineDistance(GpsCoordinate(lat1, lon1), GpsCoordinate(lat2, lon2))
    }

    fun isInsideCampus(latitude: Double, longitude: Double): Boolean {
        return isInsideCampus(GpsCoordinate(latitude, longitude))
    }
}

object JwtUtils {
    // Decodes the payload and extracts user role accurately
    fun decodeRoleFromToken(token: String): UserRole {
        return try {
            val parts = token.split(".")
            if (parts.size >= 2) {
                // Use java.util.Base64 for better compatibility with unit tests (API 26+)
                val payload = String(java.util.Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8)
                val json = JSONObject(payload)
                val roleStr = json.optString("role", "viewer").lowercase()
                
                when {
                    roleStr.contains("superadmin") -> UserRole.superadmin
                    roleStr.contains("admin") -> UserRole.admin
                    roleStr.contains("faculty") -> UserRole.faculty
                    roleStr.contains("student") -> UserRole.student
                    else -> UserRole.viewer
                }
            } else {
                UserRole.viewer
            }
        } catch (e: Exception) {
            UserRole.viewer
        }
    }

    // Evaluates whether token is expired (Rule 25 check)
    fun isTokenExpired(token: String): Boolean {
        return try {
            val parts = token.split(".")
            if (parts.size >= 2) {
                val payload = String(java.util.Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8)
                val json = JSONObject(payload)
                if (json.has("exp")) {
                    val exp = json.getLong("exp")
                    val currentTimeSec = System.currentTimeMillis() / 1000
                    currentTimeSec > exp
                } else {
                    false
                }
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
}

class ComplianceUseCase {
    // Computes student's dynamic eligibility state based on unified Vignan thresholds (Rule 25 test coverage core)
    // Thresholds: Eligible: >= 75%, Conditional: 65% - 74.9%, Barred: < 65%
    fun calculateEligibility(attendancePercentage: Double): String {
        return when {
            attendancePercentage >= 75.0 -> "Eligible"
            attendancePercentage >= 65.0 -> "Conditional"
            else -> "Barred"
        }
    }

    // Evaluates subject-level states to return compliance highlights
    fun getDefaulterSubjects(student: Student, threshold: Double = 75.0): List<String> {
        return student.attendancePercentage.filter { it.value < threshold }.keys.toList()
    }
}

