package com.polytron.auctionapp.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.polytron.auctionapp.data.local.repository.UserPreferencesRepository
import com.polytron.auctionapp.data.remote.repository.ItemsRepository
import com.polytron.auctionapp.data.session.SessionManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Handles authentication state: login, logout, user profile info.
 * Observes SessionManager for token expiration events.
 */
class AuthViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val itemsRepository: ItemsRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val TAG = "AuthViewModel"

    // --- Input fields ---
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    // --- Session state (delegated from SessionManager) ---
    val isLoggedIn: StateFlow<Boolean> = sessionManager.isLoggedIn
    val token: StateFlow<String?> = sessionManager.token

    // --- User profile ---
    private val _idUser = MutableStateFlow<String?>(null)
    val idUser: StateFlow<String?> = _idUser.asStateFlow()

    private val _userName = MutableStateFlow<String?>(null)
    val userName: StateFlow<String?> = _userName.asStateFlow()

    private val _avatarFileName = MutableStateFlow<String?>(null)
    val avatarFileName: StateFlow<String?> = _avatarFileName.asStateFlow()

    // --- UI events ---
    private val _toastEvent = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    // --- Loading state ---
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Guard: cegah token restoration dipanggil lebih dari sekali
    private val hasRestoredSession = AtomicBoolean(false)

    init {
        // Restore persisted user data from DataStore
        viewModelScope.launch {
            userPreferencesRepository.userEmail.collectLatest { _email.value = it.orEmpty() }
        }
        viewModelScope.launch {
            userPreferencesRepository.userPassword.collectLatest { _password.value = it.orEmpty() }
        }
        viewModelScope.launch {
            userPreferencesRepository.userIdUser.collectLatest { _idUser.value = it.orEmpty() }
        }
        viewModelScope.launch {
            userPreferencesRepository.userName.collectLatest { _userName.value = it }
        }
        viewModelScope.launch {
            userPreferencesRepository.userAvatar.collectLatest { _avatarFileName.value = it }
        }

        // Restore session from saved token — hanya sekali saat app start
        // Menggunakan first() bukan collectLatest agar tidak re-trigger
        // setiap kali DataStore emit ulang (mencegah duplicate SSE connections)
        viewModelScope.launch {
            if (hasRestoredSession.compareAndSet(false, true)) {
                val savedToken = userPreferencesRepository.userToken.first()
                if (!savedToken.isNullOrEmpty()) {
                    try {
                        itemsRepository.loginWithToken(savedToken)
                        sessionManager.onLoginSuccess(savedToken)
                    } catch (e: Exception) {
                        Log.e(TAG, "Token restore failed: ${e.message}")
                        sessionManager.onSessionExpired()
                    }
                }
            }
        }

        // Forward session-expired events as toasts
        viewModelScope.launch {
            sessionManager.sessionExpiredEvent.collect { message ->
                _toastEvent.emit(message)
                // Clear local profile state
                _idUser.value = null
                _userName.value = null
                _avatarFileName.value = null
            }
        }
    }

    // --- Input handlers ---
    fun onEmailChange(value: String) { _email.value = value }
    fun onPasswordChange(value: String) { _password.value = value }

    // --- Login ---
    fun login(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                if (_email.value.isBlank() || _password.value.isBlank()) {
                    onError("Email atau password tidak boleh kosong")
                    return@launch
                }

                val auth = itemsRepository.loginWithEmailPassword(_email.value, _password.value)

                _idUser.value = auth.userId
                _userName.value = auth.name
                _avatarFileName.value = auth.avatar

                // Persist login data
                userPreferencesRepository.saveLogin(
                    email = _email.value,
                    password = _password.value,
                    token = auth.token,
                    idUser = auth.userId,
                    name = auth.name ?: "Unknown",
                    avatar = auth.avatar
                )

                // Activate session
                itemsRepository.loginWithToken(auth.token)
                sessionManager.onLoginSuccess(auth.token)

                _toastEvent.emit("Berhasil Login, ${auth.name}!")
                onSuccess()
            } catch (e: Exception) {
                Log.e(TAG, "Login failed: $e")
                _toastEvent.emit("Login gagal")
                onError("Login gagal: ${e.localizedMessage}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // --- Logout ---
    fun logout() {
        viewModelScope.launch {
            sessionManager.onLogout()
            _idUser.value = null
            _userName.value = null
            _avatarFileName.value = null
            _toastEvent.emit("Berhasil Logout")
        }
    }
}
