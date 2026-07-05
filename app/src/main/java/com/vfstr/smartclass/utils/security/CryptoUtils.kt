package com.vfstr.smartclass.utils.security

import android.util.Base64
import java.nio.ByteBuffer
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

object CryptoUtils {
    private const val ALGORITHM = "AES"
    // In a production app, this key should be securely stored or derived.
    // For this implementation, we use a fixed key as a base.
    private val FIXED_KEY = "VFSTR_SMARTCLASS_SECRET_KEY_2026".toByteArray().sliceArray(0..15)

    /**
     * Encrypts a session ID with a rolling 30-second timestamp window.
     * Payload = AES_Encrypt(session_id + (current_timestamp / 30000))
     */
    fun encryptSessionToken(sessionId: String): ByteArray {
        val timestampWindow = System.currentTimeMillis() / 30000
        val payload = "$sessionId|$timestampWindow"
        
        val secretKey = SecretKeySpec(FIXED_KEY, ALGORITHM)
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        
        return cipher.doFinal(payload.toByteArray())
    }

    /**
     * Decodes the encrypted token for local verification (if needed by student app for validation).
     */
    fun decryptSessionToken(encryptedData: ByteArray): String? {
        return try {
            val secretKey = SecretKeySpec(FIXED_KEY, ALGORITHM)
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, secretKey)
            
            val decryptedBytes = cipher.doFinal(encryptedData)
            String(decryptedBytes)
        } catch (e: Exception) {
            null
        }
    }
}
