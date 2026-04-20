package com.polytron.auctionapp.data.session

import android.util.Log
import com.polytron.auctionapp.data.local.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Manages user session lifecycle.
 * Provides a centralized place to detect token expiration and notify the entire app.
 */
class SessionManager(
    private val userPreferencesRepository: UserPreferencesRepository
) {
    private val TAG = "SessionManager"

    // Event channel for session expiration messages (consumed by UI for Toast/Snackbar)
    private val _sessionExpiredEvent = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val sessionExpiredEvent: SharedFlow<String> = _sessionExpiredEvent.asSharedFlow()

    // Tracks whether the user is currently logged in
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    // Token for API calls
    private val _token = MutableStateFlow<String?>(null)
    val token: StateFlow<String?> = _token.asStateFlow()

    // Guard to prevent multiple simultaneous session-expired triggers
    private val isHandlingExpiry = AtomicBoolean(false)

    /**
     * Called after successful login or token restoration from DataStore.
     */
    fun onLoginSuccess(token: String) {
        _token.value = token
        _isLoggedIn.value = true
        isHandlingExpiry.set(false)
        Log.d(TAG, "Session started.")
    }

    /**
     * Called by Repository layer when a 401/403 is detected from the server.
     * Clears the session and emits an event so the UI can react.
     */
    suspend fun onSessionExpired() {
        // Prevent re-entrant calls
        if (!isHandlingExpiry.compareAndSet(false, true)) return

        Log.w(TAG, "Session expired! Clearing login data.")
        _isLoggedIn.value = false
        _token.value = null
        userPreferencesRepository.clearLogin()
        _sessionExpiredEvent.emit("Sesi telah habis. Silakan login kembali.")
    }

    /**
     * Called by AuthViewModel on explicit user logout.
     */
    suspend fun onLogout() {
        Log.d(TAG, "User logged out.")
        _isLoggedIn.value = false
        _token.value = null
        userPreferencesRepository.clearLogin()
    }
}
