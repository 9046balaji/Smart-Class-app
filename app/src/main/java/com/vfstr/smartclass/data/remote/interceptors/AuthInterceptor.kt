package com.vfstr.smartclass.data.remote.interceptors

import com.vfstr.smartclass.data.preferences.SecurePreferences
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val securePreferences: SecurePreferences
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Don't add auth header for login endpoints
        val path = originalRequest.url.encodedPath
        if (path.contains("auth/token") || path.contains("api/v1/student/login")) {
            return chain.proceed(originalRequest)
        }

        val token = securePreferences.getStaffToken() ?: securePreferences.getStudentToken()
        
        return if (token != null) {
            val newRequest = originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
            chain.proceed(newRequest)
        } else {
            chain.proceed(originalRequest)
        }
    }
}
