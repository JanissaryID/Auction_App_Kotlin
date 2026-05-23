package com.polytron.auctionapp.presentation.auth

import com.polytron.auctionapp.core.logging.AppLogger
import com.polytron.auctionapp.domain.repository.UserPreferencesRepository
import com.polytron.auctionapp.domain.session.SessionManager
import com.polytron.auctionapp.domain.usecase.auth.LoginUseCase
import com.polytron.auctionapp.domain.usecase.auth.LogoutUseCase
import com.polytron.auctionapp.domain.usecase.auth.RestoreSessionUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AuthViewModel(
    private val scope: CoroutineScope,
    private val loginUseCase: LoginUseCase,
    private val restoreSessionUseCase: RestoreSessionUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val sessionManager: SessionManager,
    private val logger: AppLogger
) {
    private val tag = "AuthViewModel"

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    val isLoggedIn: StateFlow<Boolean> = sessionManager.isLoggedIn
    val token: StateFlow<String?> = sessionManager.token

    private val _idUser = MutableStateFlow<String?>(null)
    val idUser: StateFlow<String?> = _idUser.asStateFlow()

    private val _userName = MutableStateFlow<String?>(null)
    val userName: StateFlow<String?> = _userName.asStateFlow()

    private val _avatarFileName = MutableStateFlow<String?>(null)
    val avatarFileName: StateFlow<String?> = _avatarFileName.asStateFlow()

    private val _toastEvent = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var hasRestoredSession = false

    init {
        scope.launch {
            userPreferencesRepository.userEmail.collectLatest { _email.value = it.orEmpty() }
        }
        scope.launch {
            userPreferencesRepository.userPassword.collectLatest { _password.value = it.orEmpty() }
        }
        scope.launch {
            userPreferencesRepository.userIdUser.collectLatest { _idUser.value = it.orEmpty() }
        }
        scope.launch {
            userPreferencesRepository.userName.collectLatest { _userName.value = it }
        }
        scope.launch {
            userPreferencesRepository.userAvatar.collectLatest { _avatarFileName.value = it }
        }
        scope.launch {
            if (!hasRestoredSession) {
                hasRestoredSession = true
                try {
                    restoreSessionUseCase()
                } catch (e: Exception) {
                    logger.error(tag, "Token restore failed: ${e.message}", e)
                }
            }
        }
        scope.launch {
            sessionManager.sessionExpiredEvent.collect { message ->
                _toastEvent.emit(message)
                clearProfile()
            }
        }
    }

    fun onEmailChange(value: String) {
        _email.value = value
    }

    fun onPasswordChange(value: String) {
        _password.value = value
    }

    fun login(onSuccess: () -> Unit, onError: (String) -> Unit) {
        scope.launch {
            _isLoading.value = true
            try {
                if (_email.value.isBlank() || _password.value.isBlank()) {
                    onError("Email atau password tidak boleh kosong")
                    return@launch
                }

                val auth = loginUseCase(email = _email.value, password = _password.value)
                _idUser.value = auth.userId
                _userName.value = auth.name
                _avatarFileName.value = auth.avatar
                _toastEvent.emit("Berhasil Login, ${auth.name}!")
                onSuccess()
            } catch (e: Exception) {
                logger.error(tag, "Login failed: ${e.message}", e)
                _toastEvent.emit("Login gagal")
                onError("Email atau password salah")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        scope.launch {
            logoutUseCase()
            clearProfile()
            _toastEvent.emit("Berhasil Logout")
        }
    }

    private fun clearProfile() {
        _idUser.value = null
        _userName.value = null
        _avatarFileName.value = null
    }
}
