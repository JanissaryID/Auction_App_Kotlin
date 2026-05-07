package com.polytron.auctionapp.desktop.data.session

import java.util.prefs.Preferences

class DesktopSessionManager {
    private val prefs = Preferences.userNodeForPackage(DesktopSessionManager::class.java)

    companion object {
        private const val KEY_TOKEN = "auth_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
    }

    fun saveSession(token: String, userId: String, name: String?, email: String) {
        prefs.put(KEY_TOKEN, token)
        prefs.put(KEY_USER_ID, userId)
        prefs.put(KEY_USER_NAME, name ?: "")
        prefs.put(KEY_USER_EMAIL, email)
    }

    fun getToken(): String? = prefs.get(KEY_TOKEN, null)
    fun getUserId(): String? = prefs.get(KEY_USER_ID, null)
    fun getUserName(): String? = prefs.get(KEY_USER_NAME, null)
    fun getUserEmail(): String? = prefs.get(KEY_USER_EMAIL, null)

    fun clearSession() {
        prefs.remove(KEY_TOKEN)
        prefs.remove(KEY_USER_ID)
        prefs.remove(KEY_USER_NAME)
        prefs.remove(KEY_USER_EMAIL)
    }

    fun hasSession(): Boolean = getToken() != null
}
