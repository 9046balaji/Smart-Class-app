package com.vfstr.smartclass.domain.usecases

import com.vfstr.smartclass.domain.models.UserRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.Base64

@RunWith(RobolectricTestRunner::class)
class JwtUtilsTest {

    @Test
    fun testDecodeRoleFromToken() {
        val payload = "{\"role\":\"faculty\",\"name\":\"Dr. Rao\"}"
        val encodedPayload = Base64.getUrlEncoder().withoutPadding().encodeToString(payload.toByteArray())
        val token = "header.$encodedPayload.signature"

        val role = JwtUtils.decodeRoleFromToken(token)
        assertEquals(UserRole.faculty, role)
    }

    @Test
    fun testIsTokenExpired() {
        val currentTime = System.currentTimeMillis() / 1000
        
        val expiredPayload = "{\"exp\":${currentTime - 100}}"
        val expiredToken = "header.${Base64.getUrlEncoder().withoutPadding().encodeToString(expiredPayload.toByteArray())}.sig"
        assertTrue(JwtUtils.isTokenExpired(expiredToken))

        val validPayload = "{\"exp\":${currentTime + 10000}}"
        val validToken = "header.${Base64.getUrlEncoder().withoutPadding().encodeToString(validPayload.toByteArray())}.sig"
        assertFalse(JwtUtils.isTokenExpired(validToken))
    }
}
