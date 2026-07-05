package com.vfstr.smartclass.utils.security

import org.junit.Assert.*
import org.junit.Test

class CryptoUtilsTest {

    @Test
    fun `encryptSessionToken produces consistent results within 30s window`() {
        val sessionId = "test_session_123"
        val token1 = CryptoUtils.encryptSessionToken(sessionId)
        val token2 = CryptoUtils.encryptSessionToken(sessionId)
        
        assertArrayEquals(token1, token2)
    }

    @Test
    fun `decryptSessionToken recovers original session id and window`() {
        val sessionId = "test_session_123"
        val encrypted = CryptoUtils.encryptSessionToken(sessionId)
        val decrypted = CryptoUtils.decryptSessionToken(encrypted)
        
        assertNotNull(decrypted)
        assertTrue(decrypted!!.startsWith(sessionId))
        assertTrue(decrypted.contains("|"))
    }

    @Test
    fun `encryptSessionToken produces different results for different sessions`() {
        val token1 = CryptoUtils.encryptSessionToken("session_1")
        val token2 = CryptoUtils.encryptSessionToken("session_2")
        
        assertFalse(token1.contentEquals(token2))
    }
}
