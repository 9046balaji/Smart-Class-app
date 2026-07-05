package com.vfstr.smartclass.utils

import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.util.Base64

data class JwtPayload(
    val sub: String?,
    val userId: String?,
    val uid: String?,
    val role: String?,
    val exp: Long?,
    val dept: String?,
    val name: String?
)

object JwtUtils {

    fun decodePayload(token: String): JwtPayload? {
        return try {
            val parts = token.split(".")
            if (parts.size < 2) return null
            
            val decoder = Base64.getUrlDecoder()
            val payloadStr = String(
                decoder.decode(parts[1]),
                StandardCharsets.UTF_8
            )
            val json = JSONObject(payloadStr)
            
            JwtPayload(
                sub = json.optString("sub", "").let { if (it.isEmpty()) null else it },
                userId = json.optString("user_id", json.optString("id", "")).let { if (it.isEmpty()) null else it },
                uid = json.optString("uid", "").let { if (it.isEmpty()) null else it },
                role = json.optString("role", "").let { if (it.isEmpty()) null else it },
                exp = if (json.has("exp")) json.getLong("exp") else null,
                dept = json.optString("dept", json.optString("department", "")).let { if (it.isEmpty()) null else it },
                name = json.optString("name", json.optString("full_name", "")).let { if (it.isEmpty()) null else it }
            )
        } catch (e: Exception) {
            // In some Android environments, org.json.JSONObject might still fail in Unit Tests
            // without Robolectric, but java.util.Base64 will work.
            null
        }
    }

    fun isExpired(token: String): Boolean {
        val payload = decodePayload(token) ?: return true
        val exp = payload.exp ?: return false
        return (System.currentTimeMillis() / 1000) >= exp
    }
}
