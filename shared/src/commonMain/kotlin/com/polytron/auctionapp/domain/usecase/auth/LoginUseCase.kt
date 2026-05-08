package com.polytron.auctionapp.domain.usecase.auth

import com.polytron.auctionapp.domain.model.AuthResult
import com.polytron.auctionapp.domain.repository.ItemsRepository
import com.polytron.auctionapp.domain.repository.UserPreferencesRepository
import com.polytron.auctionapp.domain.session.SessionManager

class LoginUseCase(
    private val repository: ItemsRepository,
    private val preferences: UserPreferencesRepository,
    private val sessionManager: SessionManager
) {
    suspend operator fun invoke(email: String, password: String): AuthResult {
        val auth = repository.loginWithEmailPassword(email, password)
        preferences.saveLogin(
            email = email,
            password = password,
            token = auth.token,
            idUser = auth.userId,
            name = auth.name ?: "Unknown",
            avatar = auth.avatar
        )
        repository.loginWithToken(auth.token)
        sessionManager.onLoginSuccess(auth.token)
        return auth
    }
}
