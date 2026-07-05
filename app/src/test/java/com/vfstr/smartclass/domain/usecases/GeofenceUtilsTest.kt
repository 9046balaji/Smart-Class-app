package com.vfstr.smartclass.domain.usecases

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test

class GeofenceUtilsTest {

    @Test
    fun testHaversineDistance() {
        // Test distance between two known points
        val c1 = GpsCoordinate(16.2235, 80.5625) // Block A
        val c2 = GpsCoordinate(16.2240, 80.5630) // Block B approx
        
        val distance = GeofenceUtils.calculateHaversineDistance(c1, c2)
        // Distance should be around 75 meters
        assertTrue(distance > 50 && distance < 100)
    }

    @Test
    fun testIsInsideCampus() {
        // Point inside the defined polygon
        val insidePoint = GpsCoordinate(16.2235, 80.5625)
        assertTrue(GeofenceUtils.isInsideCampus(insidePoint))

        // Point outside the defined polygon
        val outsidePoint = GpsCoordinate(16.0, 80.0)
        assertFalse(GeofenceUtils.isInsideCampus(outsidePoint))
    }

    @Test
    fun testIsWithinClassroom() {
        val classroom = GpsCoordinate(16.2235, 80.5625)
        val deviceClose = GpsCoordinate(16.223505, 80.562505)
        val deviceFar = GpsCoordinate(16.2250, 80.5650)

        assertTrue(GeofenceUtils.isWithinClassroom(deviceClose, classroom, 20.0))
        assertFalse(GeofenceUtils.isWithinClassroom(deviceFar, classroom, 20.0))
    }
}
