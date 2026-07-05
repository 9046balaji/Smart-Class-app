package com.vfstr.smartclass.utils.security

import android.util.Base64
import com.vfstr.smartclass.domain.models.UserRole
import org.json.JSONObject
import java.nio.charset.StandardCharsets

object JwtUtils {

    data class JwtPayload(
        val sub: String?,
        val username: String?,
        val role: String?,
        val exp: Long?
    )

    fun decodePayload(token: String): JwtPayload? {
        return try {
            val parts = token.split(".")
            if (parts.size < 2) return null
            
            val payloadBytes = Base64.decode(parts[1], Base64.URL_SAFE)
            val json = JSONObject(String(payloadBytes, StandardCharsets.UTF_8))
            
            JwtPayload(
                sub = json.optString("sub", null),
                username = json.optString("username", json.optString("name", null)),
                role = json.optString("role", null),
                exp = json.optLong("exp", 0).takeIf { it > 0 }
            )
        } catch (e: Exception) {
            null
        }
    }

    fun getRoleFromToken(token: String): UserRole {
        val payload = decodePayload(token)
        val roleStr = payload?.role?.lowercase() ?: "viewer"
        
        return when {
            roleStr.contains("superadmin") -> UserRole.superadmin
            roleStr.contains("admin") -> UserRole.admin
            roleStr.contains("faculty") -> UserRole.faculty
            roleStr.contains("student") -> UserRole.student
            else -> UserRole.viewer
        }
    }
    
    fun isExpired(token: String): Boolean {
        val payload = decodePayload(token) ?: return true
        val exp = payload.exp ?: return false
        val currentTimeSeconds = System.currentTimeMillis() / 1000
        return currentTimeSeconds >= exp
    }
}
