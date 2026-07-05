package com.vfstr.smartclass.data.remote.interceptors

import com.vfstr.smartclass.data.preferences.SecurePreferences
import com.vfstr.smartclass.data.remote.api.RetrofitApi
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Provider

/**
 * OkHttp Authenticator for handling 401 Unauthorized responses.
 * Rule 6: On 401 response: auto-refresh using /auth/refresh.
 */
class TokenAuthenticator @Inject constructor(
    private val securePreferences: SecurePreferences,
    private val apiProvider: Provider<RetrofitApi> // Use Provider to avoid circular dependency
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        val oldToken = securePreferences.getStaffToken() ?: securePreferences.getStudentToken()
        if (oldToken == null) return null

        // If we already tried to refresh and failed (prior response was also a 401 on the same request), stop
        if (response.countPriorResponses() >= 2) {
            handleLogout()
            return null
        }

        synchronized(this) {
            val currentToken = securePreferences.getStaffToken() ?: securePreferences.getStudentToken()
            
            // If token was already refreshed by another thread, retry with the new one
            if (currentToken != oldToken && currentToken != null) {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $currentToken")
                    .build()
            }

            // Attempt to refresh token
            return try {
                val api = apiProvider.get()
                // Synchronous call in OkHttp thread
                val tokenResponse = runBlocking {
                    api.refreshToken("Bearer $oldToken")
                }

                val newToken = tokenResponse.access_token
                
                // Store based on which role was active
                if (securePreferences.getStaffToken() != null) {
                    securePreferences.saveStaffToken(newToken)
                } else {
                    securePreferences.saveStudentToken(newToken)
                }

                response.request.newBuilder()
                    .header("Authorization", "Bearer $newToken")
                    .build()
            } catch (e: Exception) {
                handleLogout()
                null
            }
        }
    }

    private fun handleLogout() {
        securePreferences.logout()
        // Rule 6: emit a global logout event -> navigate to Login screen
        // In a real app, we'd use a SharedFlow or EventBus to trigger navigation in MainActivity
    }

    private fun Response.countPriorResponses(): Int {
        var result = 1
        var prior = priorResponse
        while (prior != null) {
            result++
            prior = prior.priorResponse
        }
        return result
    }
}
