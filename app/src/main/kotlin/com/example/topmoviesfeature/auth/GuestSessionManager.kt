package com.example.topmoviesfeature.auth

import android.content.SharedPreferences
import dagger.hilt.android.scopes.ActivityRetainedScoped
import javax.inject.Inject

@ActivityRetainedScoped
class GuestSessionManager @Inject constructor(
    private val sharedPreferences: SharedPreferences
) {
    companion object {
        private const val GUEST_SESSION_ID_KEY = "guest_session_id"
        private const val GUEST_SESSION_TIME_KEY = "guest_session_time"
        private const val SESSION_VALIDITY_MS = 60 * 60 * 1000L // 60 minutes
    }

    fun getGuestSessionId(): String? {
        val sessionId = sharedPreferences.getString(GUEST_SESSION_ID_KEY, null)
        val sessionTime = sharedPreferences.getLong(GUEST_SESSION_TIME_KEY, 0L)

        // Check if session is still valid
        if (sessionId != null && System.currentTimeMillis() - sessionTime < SESSION_VALIDITY_MS) {
            return sessionId
        }

        // Session expired
        clearGuestSession()
        return null
    }

    fun saveGuestSession(sessionId: String) {
        sharedPreferences.edit()
            .putString(GUEST_SESSION_ID_KEY, sessionId)
            .putLong(GUEST_SESSION_TIME_KEY, System.currentTimeMillis())
            .apply()
    }

    fun clearGuestSession() {
        sharedPreferences.edit()
            .remove(GUEST_SESSION_ID_KEY)
            .remove(GUEST_SESSION_TIME_KEY)
            .apply()
    }

    fun isSessionValid(): Boolean {
        return getGuestSessionId() != null
    }
}
