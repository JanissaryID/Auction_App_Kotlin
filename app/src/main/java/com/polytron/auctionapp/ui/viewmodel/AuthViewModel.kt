package com.polytron.auctionapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.polytron.auctionapp.core.logging.AppLogger
import com.polytron.auctionapp.domain.repository.UserPreferencesRepository
import com.polytron.auctionapp.domain.session.SessionManager
import com.polytron.auctionapp.domain.usecase.auth.LoginUseCase
import com.polytron.auctionapp.domain.usecase.auth.LogoutUseCase
import com.polytron.auctionapp.domain.usecase.auth.RestoreSessionUseCase
import com.polytron.auctionapp.presentation.auth.AuthViewModel as SharedAuthViewModel

class AuthViewModel(
    loginUseCase: LoginUseCase,
    restoreSessionUseCase: RestoreSessionUseCase,
    logoutUseCase: LogoutUseCase,
    userPreferencesRepository: UserPreferencesRepository,
    sessionManager: SessionManager,
    logger: AppLogger
) : ViewModel() {
    private val delegate = SharedAuthViewModel(
        scope = viewModelScope,
        loginUseCase = loginUseCase,
        restoreSessionUseCase = restoreSessionUseCase,
        logoutUseCase = logoutUseCase,
        userPreferencesRepository = userPreferencesRepository,
        sessionManager = sessionManager,
        logger = logger
    )

    val email = delegate.email
    val password = delegate.password
    val isLoggedIn = delegate.isLoggedIn
    val token = delegate.token
    val idUser = delegate.idUser
    val userName = delegate.userName
    val avatarFileName = delegate.avatarFileName
    val toastEvent = delegate.toastEvent
    val isLoading = delegate.isLoading

    fun onEmailChange(value: String) = delegate.onEmailChange(value)
    fun onPasswordChange(value: String) = delegate.onPasswordChange(value)
    fun login(onSuccess: () -> Unit, onError: (String) -> Unit) = delegate.login(onSuccess, onError)
    fun logout() = delegate.logout()
}
