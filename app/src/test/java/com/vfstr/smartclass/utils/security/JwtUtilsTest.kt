package com.vfstr.smartclass.utils.security

import com.vfstr.smartclass.domain.models.UserRole
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.Base64

@RunWith(RobolectricTestRunner::class)
class JwtUtilsTest {

    @Test
    fun testDecodePayload_Success() {
        val payload = "{\"role\":\"admin\",\"username\":\"rao\",\"exp\":4070908800}"
        val token = createMockToken(payload)
        
        val decoded = JwtUtils.decodePayload(token)
        assertNotNull(decoded)
        assertEquals("admin", decoded?.role)
        assertEquals("rao", decoded?.username)
        assertEquals(4070908800L, decoded?.exp)
    }

    @Test
    fun testGetRoleFromToken() {
        assertEquals(UserRole.admin, JwtUtils.getRoleFromToken(createMockToken("{\"role\":\"admin\"}")))
        assertEquals(UserRole.superadmin, JwtUtils.getRoleFromToken(createMockToken("{\"role\":\"superadmin\"}")))
        assertEquals(UserRole.faculty, JwtUtils.getRoleFromToken(createMockToken("{\"role\":\"faculty\"}")))
        assertEquals(UserRole.student, JwtUtils.getRoleFromToken(createMockToken("{\"role\":\"student\"}")))
        assertEquals(UserRole.viewer, JwtUtils.getRoleFromToken(createMockToken("{\"role\":\"unknown\"}")))
    }

    @Test
    fun testIsExpired() {
        val futureExp = (System.currentTimeMillis() / 1000) + 3600
        val pastExp = (System.currentTimeMillis() / 1000) - 3600
        
        assertFalse(JwtUtils.isExpired(createMockToken("{\"exp\":$futureExp}")))
        assertTrue(JwtUtils.isExpired(createMockToken("{\"exp\":$pastExp}")))
        assertFalse(JwtUtils.isExpired(createMockToken("{}"))) // No exp claim
    }

    private fun createMockToken(payloadJson: String): String {
        val header = Base64.getUrlEncoder().withoutPadding().encodeToString("{\"alg\":\"HS256\"}".toByteArray())
        val payload = Base64.getUrlEncoder().withoutPadding().encodeToString(payloadJson.toByteArray())
        return "$header.$payload.signature"
    }
}
