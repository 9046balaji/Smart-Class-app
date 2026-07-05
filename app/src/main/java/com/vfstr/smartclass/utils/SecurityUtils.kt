package com.vfstr.smartclass.utils

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

class SecurityUtils(context: Context) {

    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    private val sharedPrefs = EncryptedSharedPreferences.create(
        "smart_class_secure_pref",
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    companion object {
        private const val KEY_STAFF_TOKEN = "staff_jwt_token"
        private const val KEY_STUDENT_TOKEN = "student_jwt_token"
        private const val KEY_USER_ROLE = "user_role"
        private const val KEY_REMEMBER_ME = "remember_me"
        private const val KEY_REMEMBERED_USER = "remembered_user"
    }

    fun saveStaffToken(token: String?) {
        sharedPrefs.edit().putString(KEY_STAFF_TOKEN, token).apply()
    }

    fun getStaffToken(): String? {
        return sharedPrefs.getString(KEY_STAFF_TOKEN, null)
    }

    fun saveStudentToken(token: String?) {
        sharedPrefs.edit().putString(KEY_STUDENT_TOKEN, token).apply()
    }

    fun getStudentToken(): String? {
        return sharedPrefs.getString(KEY_STUDENT_TOKEN, null)
    }

    fun saveUserRole(role: String?) {
        sharedPrefs.edit().putString(KEY_USER_ROLE, role).apply()
    }

    fun getUserRole(): String? {
        return sharedPrefs.getString(KEY_USER_ROLE, null)
    }

    fun saveRememberMe(enabled: Boolean, username: String = "") {
        sharedPrefs.edit()
            .putBoolean(KEY_REMEMBER_ME, enabled)
            .putString(KEY_REMEMBERED_USER, username)
            .apply()
    }

    fun isRememberMeEnabled(): Boolean = sharedPrefs.getBoolean(KEY_REMEMBER_ME, false)
    fun getRememberedUser(): String? = sharedPrefs.getString(KEY_REMEMBERED_USER, "")

    fun logout() {
        sharedPrefs.edit()
            .remove(KEY_STAFF_TOKEN)
            .remove(KEY_STUDENT_TOKEN)
            .remove(KEY_USER_ROLE)
            .apply()
    }
}

