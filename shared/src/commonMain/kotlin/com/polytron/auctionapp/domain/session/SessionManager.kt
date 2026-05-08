package com.polytron.auctionapp.domain.session

import com.polytron.auctionapp.core.logging.AppLogger
import com.polytron.auctionapp.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class SessionManager(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val logger: AppLogger
) : SessionExpiryHandler {
    private val tag = "SessionManager"

    private val _sessionExpiredEvent = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val sessionExpiredEvent: SharedFlow<String> = _sessionExpiredEvent.asSharedFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _token = MutableStateFlow<String?>(null)
    val token: StateFlow<String?> = _token.asStateFlow()

    private val expiryMutex = Mutex()
    private var isHandlingExpiry = false

    fun onLoginSuccess(token: String) {
        _token.value = token
        _isLoggedIn.value = true
        isHandlingExpiry = false
        logger.debug(tag, "Session started.")
    }

    override suspend fun onSessionExpired() {
        val shouldHandle = expiryMutex.withLock {
            if (isHandlingExpiry) {
                false
            } else {
                isHandlingExpiry = true
                true
            }
        }
        if (!shouldHandle) return

        logger.warn(tag, "Session expired. Clearing login data.")
        _isLoggedIn.value = false
        _token.value = null
        userPreferencesRepository.clearLogin()
        _sessionExpiredEvent.emit("Sesi telah habis. Silakan login kembali.")
    }

    suspend fun onLogout() {
        logger.debug(tag, "User logged out.")
        expiryMutex.withLock {
            isHandlingExpiry = false
        }
        _isLoggedIn.value = false
        _token.value = null
        userPreferencesRepository.clearLogin()
    }
}
