package com.polytron.auctionapp.desktop.auth

import com.polytron.auctionapp.desktop.data.remote.PocketBaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class User(
    val userId: String,
    val name: String?,
    val avatar: String?,
    val email: String
)

class AuthManager(
    private val pocketBaseRepository: PocketBaseRepository
) {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    /**
     * Login with email & password via PocketBase API.
     * Returns true on success, false on failure.
     */
    suspend fun login(email: String, password: String): Boolean {
        _authState.value = AuthState.Loading
        return try {
            val authResult = pocketBaseRepository.loginWithEmailPassword(email, password)
            val user = User(
                userId = authResult.userId,
                name = authResult.name,
                avatar = authResult.avatar,
                email = email
            )
            _currentUser.value = user
            _authState.value = AuthState.Authenticated(user)
            true
        } catch (e: Exception) {
            val errorMsg = when {
                e.message?.contains("400") == true -> "Email atau password salah"
                e.message?.contains("Failed to authenticate") == true -> "Email atau password salah"
                e.message?.contains("connect") == true -> "Gagal terhubung ke server"
                e.message?.contains("timeout") == true -> "Koneksi timeout"
                else -> "Login gagal: ${e.message}"
            }
            _authState.value = AuthState.Error(errorMsg)
            false
        }
    }

    fun logout() {
        _currentUser.value = null
        _authState.value = AuthState.Unauthenticated
    }

    fun isAuthenticated(): Boolean {
        return _authState.value is AuthState.Authenticated
    }

    fun clearError() {
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.Unauthenticated
        }
    }
}

sealed class AuthState {
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    data class Authenticated(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}
