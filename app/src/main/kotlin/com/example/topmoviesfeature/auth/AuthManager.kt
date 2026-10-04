package com.example.topmoviesfeature.auth

import android.content.SharedPreferences
import dagger.hilt.android.scopes.ActivityRetainedScoped
import javax.inject.Inject

@ActivityRetainedScoped
class AuthManager @Inject constructor(
    private val sharedPreferences: SharedPreferences
) {
    companion object {
        private const val SESSION_ID_KEY = "session_id"
        private const val ACCOUNT_ID_KEY = "account_id"
        private const val USERNAME_KEY = "username"
    }

    fun isLoggedIn(): Boolean {
        return getSessionId().isNotEmpty() && getAccountId() != 0L
    }

    fun getSessionId(): String {
        return sharedPreferences.getString(SESSION_ID_KEY, "") ?: ""
    }

    fun getAccountId(): Long {
        return sharedPreferences.getLong(ACCOUNT_ID_KEY, 0L)
    }

    fun getUsername(): String {
        return sharedPreferences.getString(USERNAME_KEY, "") ?: ""
    }

    fun saveSession(sessionId: String, accountId: Long, username: String) {
        sharedPreferences.edit()
            .putString(SESSION_ID_KEY, sessionId)
            .putLong(ACCOUNT_ID_KEY, accountId)
            .putString(USERNAME_KEY, username)
            .apply()
    }

    fun logout() {
        sharedPreferences.edit()
            .remove(SESSION_ID_KEY)
            .remove(ACCOUNT_ID_KEY)
            .remove(USERNAME_KEY)
            .apply()
    }
}
