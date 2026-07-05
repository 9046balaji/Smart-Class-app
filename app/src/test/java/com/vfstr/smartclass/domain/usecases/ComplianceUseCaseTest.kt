package com.vfstr.smartclass.domain.usecases

import com.vfstr.smartclass.domain.models.Student
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ComplianceUseCaseTest {

    private val useCase = ComplianceUseCase()

    @Test
    fun testCalculateEligibility() {
        assertEquals("Eligible", useCase.calculateEligibility(85.0))
        assertEquals("Eligible", useCase.calculateEligibility(75.0))
        assertEquals("Conditional", useCase.calculateEligibility(70.0))
        assertEquals("Conditional", useCase.calculateEligibility(65.0))
        assertEquals("Barred", useCase.calculateEligibility(60.0))
        assertEquals("Barred", useCase.calculateEligibility(0.0))
    }

    @Test
    fun testGetDefaulterSubjects() {
        val student = Student(
            id = "s1",
            studentId = "22L11A0501",
            rollNo = "22L11A0501",
            name = "Test",
            section = "A",
            year = "III",
            department = "CSAI",
            faceEnrolled = true,
            biometricConsent = true,
            cgpa = 8.0,
            attendancePercentage = mapOf(
                "S1" to 80.0,
                "S2" to 60.0,
                "S3" to 70.0
            )
        )

        val defaulters = useCase.getDefaulterSubjects(student, 75.0)
        assertEquals(2, defaulters.size)
        assertTrue(defaulters.contains("S2"))
        assertTrue(defaulters.contains("S3"))
    }
}
