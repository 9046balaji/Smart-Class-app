package com.vfstr.smartclass.utils

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.*

@RunWith(RobolectricTestRunner::class)
class JwtUtilsTest {

    @Test
    fun testDecodePayload() {
        val header = Base64.getEncoder().encodeToString("{\"alg\":\"HS256\"}".toByteArray())
        val payload = Base64.getEncoder().encodeToString("{\"role\":\"admin\",\"name\":\"Dr Rao\",\"exp\":4070908800}".toByteArray())
        val token = "$header.$payload.signature"
        
        val decoded = JwtUtils.decodePayload(token)
        assertNotNull(decoded)
        assertEquals("admin", decoded?.role)
        assertEquals("Dr Rao", decoded?.name)
        assertEquals(4070908800L, decoded?.exp)
    }
}
