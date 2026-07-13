package com.vfstr.smartclass.data.preferences

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.vfstr.smartclass.domain.models.UserRole
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecurePreferences @Inject constructor(@ApplicationContext private val context: Context) {
    
    private val masterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val prefs: SharedPreferences by lazy {
        try {
            createEncryptedPrefs()
        } catch (e: Exception) {
            // If encrypted prefs fail to initialize (common on some devices or after OS updates),
            // we try to clear the corrupted file and recreate it.
            try {
                context.deleteSharedPreferences("smartclass_secure_prefs")
                createEncryptedPrefs()
            } catch (e2: Exception) {
                throw IllegalStateException("Failed to initialize secure storage. Fallback is disabled for security compliance.", e2)
            }
        }
    }

    private fun createEncryptedPrefs(): SharedPreferences {
        return EncryptedSharedPreferences.create(
            context,
            "smartclass_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    companion object {
        private const val KEY_STAFF_TOKEN = "key_staff_jwt"
        private const val KEY_STUDENT_TOKEN = "key_student_jwt"
        private const val KEY_USER_NAME = "key_user_name"
        private const val KEY_USER_ROLE = "key_user_role"
        private const val KEY_BIOMETRIC_CONSENT = "key_biometric_consent"
        
        // Student Local Fields
        private const val KEY_CGPA = "key_cgpa"
        private const val KEY_LAST_YEAR_SUBJECTS = "key_last_year_subjects"
        
        // Settings
        private const val KEY_THEME = "key_theme" // "dark" or "light"
        private const val KEY_NOTIF_ATTENDANCE = "key_notif_attendance"
        private const val KEY_NOTIF_DEFAULTER = "key_notif_defaulter"
        private const val KEY_NOTIF_COMPLIANCE = "key_notif_compliance"
        private const val KEY_BIOMETRIC_LOCK_ENABLED = "key_biometric_lock_enabled"
        private const val KEY_ACTIVE_ROLE_CONTEXT = "key_active_role_context"
        private const val KEY_DATA_SAVER_ENABLED = "key_data_saver_enabled"
        private const val KEY_DND_AUTOMATION_ENABLED = "key_dnd_automation_enabled"
        
        private const val KEY_BIOMETRIC_CIPHERTEXT = "key_biometric_ciphertext"
        private const val KEY_BIOMETRIC_IV = "key_biometric_iv"
    }

    fun saveEncryptedToken(ciphertext: ByteArray, iv: ByteArray) {
        prefs.edit()
            .putString(KEY_BIOMETRIC_CIPHERTEXT, Base64.encodeToString(ciphertext, Base64.DEFAULT))
            .putString(KEY_BIOMETRIC_IV, Base64.encodeToString(iv, Base64.DEFAULT))
            .apply()
    }

    fun getEncryptedToken(): Pair<ByteArray, ByteArray>? {
        val ciphertextStr = prefs.getString(KEY_BIOMETRIC_CIPHERTEXT, null) ?: return null
        val ivStr = prefs.getString(KEY_BIOMETRIC_IV, null) ?: return null
        
        return try {
            val ciphertext = Base64.decode(ciphertextStr, Base64.DEFAULT)
            val iv = Base64.decode(ivStr, Base64.DEFAULT)
            Pair(ciphertext, iv)
        } catch (e: Exception) {
            null
        }
    }

    fun hasBiometricToken(): Boolean {
        return prefs.contains(KEY_BIOMETRIC_CIPHERTEXT) && prefs.contains(KEY_BIOMETRIC_IV)
    }

    fun clearBiometricToken() {
        prefs.edit()
            .remove(KEY_BIOMETRIC_CIPHERTEXT)
            .remove(KEY_BIOMETRIC_IV)
            .apply()
    }

    fun saveActiveRoleContext(role: UserRole?) {
        prefs.edit().putString(KEY_ACTIVE_ROLE_CONTEXT, role?.name).apply()
    }

    fun getActiveRoleContext(): UserRole? {
        val roleStr = prefs.getString(KEY_ACTIVE_ROLE_CONTEXT, null) ?: return null
        return try {
            UserRole.valueOf(roleStr)
        } catch (e: Exception) {
            null
        }
    }

    fun saveDataSaverEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DATA_SAVER_ENABLED, enabled).apply()
    }

    fun isDataSaverEnabled(): Boolean {
        return prefs.getBoolean(KEY_DATA_SAVER_ENABLED, false)
    }

    fun saveDndAutomationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DND_AUTOMATION_ENABLED, enabled).apply()
    }

    fun isDndAutomationEnabled(): Boolean {
        return prefs.getBoolean(KEY_DND_AUTOMATION_ENABLED, false)
    }

    fun saveBiometricLockEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_LOCK_ENABLED, enabled).apply()
    }

    fun isBiometricLockEnabled(): Boolean {
        return prefs.getBoolean(KEY_BIOMETRIC_LOCK_ENABLED, false)
    }

    fun saveBiometricConsent(granted: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_CONSENT, granted).apply()
    }

    fun hasBiometricConsent(): Boolean {
        return prefs.getBoolean(KEY_BIOMETRIC_CONSENT, true)
    }

    fun saveCgpa(cgpa: Float) {
        prefs.edit().putFloat(KEY_CGPA, cgpa).apply()
    }

    fun getCgpa(): Float {
        return prefs.getFloat(KEY_CGPA, 0.0f)
    }

    fun saveLastYearSubjects(subjects: String) {
        prefs.edit().putString(KEY_LAST_YEAR_SUBJECTS, subjects).apply()
    }

    fun getLastYearSubjects(): String {
        return prefs.getString(KEY_LAST_YEAR_SUBJECTS, "") ?: ""
    }

    fun saveTheme(theme: String) {
        prefs.edit().putString(KEY_THEME, theme).apply()
    }

    fun getTheme(): String {
        return prefs.getString(KEY_THEME, "dark") ?: "dark"
    }

    fun saveNotificationSetting(key: String, enabled: Boolean) {
        val prefKey = when(key) {
            "attendance" -> KEY_NOTIF_ATTENDANCE
            "defaulter" -> KEY_NOTIF_DEFAULTER
            "compliance" -> KEY_NOTIF_COMPLIANCE
            else -> return
        }
        prefs.edit().putBoolean(prefKey, enabled).apply()
    }

    fun getNotificationSetting(key: String): Boolean {
        val prefKey = when(key) {
            "attendance" -> KEY_NOTIF_ATTENDANCE
            "defaulter" -> KEY_NOTIF_DEFAULTER
            "compliance" -> KEY_NOTIF_COMPLIANCE
            else -> return true
        }
        return prefs.getBoolean(prefKey, true)
    }

    fun saveStaffToken(token: String) {
        prefs.edit().putString(KEY_STAFF_TOKEN, token).apply()
        decodeAndSaveUser(token)
    }

    fun getStaffToken(): String? {
        return prefs.getString(KEY_STAFF_TOKEN, null)
    }

    fun clearStaffToken() {
        prefs.edit().remove(KEY_STAFF_TOKEN).apply()
    }

    fun saveStudentToken(token: String) {
        prefs.edit().putString(KEY_STUDENT_TOKEN, token).apply()
        decodeAndSaveUser(token)
    }

    fun getStudentToken(): String? {
        return prefs.getString(KEY_STUDENT_TOKEN, null)
    }

    fun clearStudentToken() {
        prefs.edit().remove(KEY_STUDENT_TOKEN).apply()
    }

    fun getUserName(): String {
        return prefs.getString(KEY_USER_NAME, "") ?: ""
    }

    fun getUserRole(): UserRole? {
        val roleStr = prefs.getString(KEY_USER_ROLE, null) ?: return null
        return try {
            UserRole.valueOf(roleStr)
        } catch (e: Exception) {
            null
        }
    }

    fun logout() {
        prefs.edit()
            .remove(KEY_STAFF_TOKEN)
            .remove(KEY_STUDENT_TOKEN)
            .remove(KEY_USER_NAME)
            .remove(KEY_USER_ROLE)
            .remove(KEY_ACTIVE_ROLE_CONTEXT)
            // Note: We don't remove biometric token here, 
            // so user can use it for the next login.
            .apply()
    }

    fun saveMockUser(name: String, role: UserRole) {
        prefs.edit()
            .putString(KEY_USER_NAME, name)
            .putString(KEY_USER_ROLE, role.name)
            .apply()
    }

    private fun decodeAndSaveUser(token: String) {
        try {
            val parts = token.split(".")
            if (parts.size >= 2) {
                val payload = String(Base64.decode(parts[1], Base64.URL_SAFE), StandardCharsets.UTF_8)
                val json = JSONObject(payload)
                
                val name = json.optString("name", json.optString("username", "User"))
                val role = json.optString("role", "viewer").lowercase()
                
                val matchedRole = when {
                    role.contains("superadmin") -> UserRole.superadmin
                    role.contains("admin") -> UserRole.admin
                    role.contains("faculty") -> UserRole.faculty
                    role.contains("student") -> UserRole.student
                    else -> UserRole.viewer
                }

                prefs.edit()
                    .putString(KEY_USER_NAME, name)
                    .putString(KEY_USER_ROLE, matchedRole.name)
                    .apply()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun isTokenExpired(token: String): Boolean {
        return try {
            val parts = token.split(".")
            if (parts.size >= 2) {
                val payload = String(Base64.decode(parts[1], Base64.URL_SAFE), StandardCharsets.UTF_8)
                val json = JSONObject(payload)
                val exp = json.optLong("exp", 0)
                if (exp > 0) {
                    val currentTimeSeconds = System.currentTimeMillis() / 1000
                    currentTimeSeconds >= exp
                } else {
                    false
                }
            } else {
                true
            }
        } catch (e: Exception) {
            true
        }
    }
}
