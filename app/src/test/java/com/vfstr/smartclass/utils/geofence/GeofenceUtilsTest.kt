package com.vfstr.smartclass.utils.geofence

import org.junit.Assert.*
import org.junit.Test

class GeofenceUtilsTest {

    @Test
    fun testHaversineDistance() {
        val c1 = LatLon(16.2235, 80.5625) // Approx Block A
        val c2 = LatLon(16.2240, 80.5630) // Approx Block B
        val distance = GeofenceUtils.haversineMeters(c1.lat, c1.lon, c2.lat, c2.lon)
        assertTrue(distance > 0)
        assertTrue(distance < 1000) 
    }

    @Test
    fun testIsInsideCampus() {
        // Point from deep-dive vertices
        val insidePoint = LatLon(16.233375, 80.550908) // Main Building
        val outsidePoint = LatLon(16.2500, 80.6000)
        
        assertTrue(GeofenceUtils.isWithinCampusPolygon(insidePoint.lat, insidePoint.lon))
        assertFalse(GeofenceUtils.isWithinCampusPolygon(outsidePoint.lat, outsidePoint.lon))
    }
}
