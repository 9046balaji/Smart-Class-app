package com.vfstr.smartclass.utils.geofence

import kotlin.math.*

data class LatLon(val lat: Double, val lon: Double)

object GeofenceUtils {
    const val EARTH_RADIUS_M = 6_371_000.0
    const val MAX_ACCEPTABLE_GPS_ACCURACY_M = 30.0
    const val MAX_SPOOFING_SPEED_KMH = 15.0
    const val DEFAULT_BUILDING_RADIUS_M = 45.0
    const val DEFAULT_CLASSROOM_GPS_RADIUS_M = 20.0
    const val GEOFENCE_RADIUS_METERS = 100.0 // for anchor-point fallback

    // Campus Boundary Polygon (12 Vertices, Clockwise)
    val CAMPUS_BOUNDARY_POLYGON = listOf(
        LatLon(16.2348, 80.5465),  // NW corner – north of main gate
        LatLon(16.2352, 80.5480),  // North entrance / highway frontage
        LatLon(16.2352, 80.5510),  // NE section – north of VFSTR admin block
        LatLon(16.2348, 80.5530),  // North of auditorium / tennis court area
        LatLon(16.2335, 80.5540),  // East boundary – tennis courts east edge
        LatLon(16.2315, 80.5535),  // SE corner – south of playground
        LatLon(16.2295, 80.5520),  // South boundary – below Pharmacy College
        LatLon(16.2288, 80.5490),  // SW section
        LatLon(16.2295, 80.5465),  // SW corner near highway
        LatLon(16.2310, 80.5462),  // Western boundary south
        LatLon(16.2335, 80.5462),  // Western boundary central
        LatLon(16.2348, 80.5465),  // Close polygon back to NW corner
    )

    // Campus Anchor Points (12 Points)
    val CAMPUS_ANCHOR_POINTS = listOf(
        LatLon(16.232956, 80.547530),  // A Block (admin gate side)
        LatLon(16.232237, 80.548662),  // H Block (ECE + EEE + IT Lab)
        LatLon(16.233203, 80.548430),  // NTR-Vignan Library
        LatLon(16.232618, 80.550360),  // N Block
        LatLon(16.233550, 80.550728),  // U Block / Aryabhatta (Mech + Civil)
        LatLon(16.233375, 80.550908),  // VFSTR Main Building (Admin + classrooms)
        LatLon(16.234579, 80.551070),  // Vignan Online / North Dept Block
        LatLon(16.233696, 80.551617),  // Auditorium
        LatLon(16.232367, 80.552036),  // Playground / Sports Ground
        LatLon(16.233919, 80.552519),  // Tennis Courts
        LatLon(16.231660, 80.549176),  // Canteen
        LatLon(16.230643, 80.550553),  // Vignan Pharmacy College
    )

    /**
     * Tier 1: Point-in-polygon test against VFSTR campus polygon vertices
     */
    fun isWithinCampusPolygon(lat: Double, lon: Double): Boolean {
        var isInside = false
        var j = CAMPUS_BOUNDARY_POLYGON.size - 1
        for (i in CAMPUS_BOUNDARY_POLYGON.indices) {
            val pi = CAMPUS_BOUNDARY_POLYGON[i]
            val pj = CAMPUS_BOUNDARY_POLYGON[j]
            if (((pi.lon > lon) != (pj.lon > lon)) &&
                (lat < (((pj.lat - pi.lat) * (lon - pi.lon)) / (pj.lon - pi.lon)) + pi.lat)
            ) {
                isInside = !isInside
            }
            j = i
        }
        return isInside
    }

    /**
     * Tier 2: Anchor-point fallback
     */
    fun isWithinCampusFallback(lat: Double, lon: Double): Boolean {
        val dist = distanceToNearestCampusAnchor(lat, lon)
        return dist <= GEOFENCE_RADIUS_METERS
    }

    /**
     * Haversine formula for distance calculations (Rule 7)
     */
    fun haversineMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_M * c
    }

    fun distanceToNearestCampusAnchor(lat: Double, lon: Double): Double {
        return CAMPUS_ANCHOR_POINTS.minOf { anchor ->
            haversineMeters(lat, lon, anchor.lat, anchor.lon)
        }
    }

    /**
     * Tier 3: WiFi BSSID Cross-check (Rule 7)
     */
    val TRUSTED_BSSIDS = listOf<String>()

    fun isWifiTrusted(bssid: String?): Boolean {
        if (bssid == null) return false
        return TRUSTED_BSSIDS.contains(bssid.uppercase())
    }

    /**
     * Anti-spoofing check: Speed calculation between two points
     */
    fun calculateSpeedKmh(
        lat1: Double, lon1: Double, time1: Long,
        lat2: Double, lon2: Double, time2: Long
    ): Double {
        val distance = haversineMeters(lat1, lon1, lat2, lon2)
        val timeDiffSeconds = abs(time2 - time1) / 1000.0
        if (timeDiffSeconds == 0.0) return 0.0
        val speedMps = distance / timeDiffSeconds
        return speedMps * 3.6 // Convert to km/h
    }

    /**
     * Combined verification check
     */
    fun verifyLocation(
        lat: Double, 
        lon: Double, 
        accuracy: Float, 
        bssid: String? = null,
        prevLat: Double? = null,
        prevLon: Double? = null,
        prevTime: Long? = null,
        currentTime: Long = System.currentTimeMillis(),
        isMock: Boolean = false
    ): LocationResult {
        if (isMock) {
            return LocationResult(false, "SPOOFING_DETECTED", "Mock location provider detected", isMock = true)
        }
        
        if (accuracy > MAX_ACCEPTABLE_GPS_ACCURACY_M) {
            return LocationResult(false, "LOW_ACCURACY", "GPS accuracy too low ($accuracy m)")
        }
        
        // Anti-spoofing speed check
        if (prevLat != null && prevLon != null && prevTime != null) {
            val speed = calculateSpeedKmh(prevLat, prevLon, prevTime, lat, lon, currentTime)
            if (speed > MAX_SPOOFING_SPEED_KMH) {
                return LocationResult(false, "SPOOFING_DETECTED", "Suspicious movement detected ($speed km/h)")
            }
        }

        val inPolygon = isWithinCampusPolygon(lat, lon)
        val inFallback = isWithinCampusFallback(lat, lon)
        val wifiVerified = isWifiTrusted(bssid)

        val tier1 = inPolygon
        val tier2 = inFallback
        val tier3 = wifiVerified

        if (!(inPolygon || inFallback)) {
            return LocationResult(false, "OUT_OF_RANGE", "Location outside campus boundaries", tier1, tier2, tier3)
        }

        return LocationResult(true, "VERIFIED", "Location verified successfully", tier1, tier2, tier3)
    }

    data class LocationResult(
        val success: Boolean,
        val code: String,
        val message: String,
        val tier1: Boolean = false,
        val tier2: Boolean = false,
        val tier3: Boolean = false,
        val isMock: Boolean = false
    )

    fun isWithinBuilding(lat: Double, lon: Double, buildingId: String): Boolean {
        val index = buildingId.toIntOrNull() ?: 0
        val centroid = CAMPUS_ANCHOR_POINTS.getOrNull(index) ?: CAMPUS_ANCHOR_POINTS.firstOrNull()
        return haversineMeters(lat, lon, centroid?.lat ?: 0.0, centroid?.lon ?: 0.0) <= DEFAULT_BUILDING_RADIUS_M
    }
}
