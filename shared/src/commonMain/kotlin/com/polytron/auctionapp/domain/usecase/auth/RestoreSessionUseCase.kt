package com.polytron.auctionapp.domain.usecase.auth

import com.polytron.auctionapp.domain.repository.ItemsRepository
import com.polytron.auctionapp.domain.repository.UserPreferencesRepository
import com.polytron.auctionapp.domain.session.SessionManager
import kotlinx.coroutines.flow.first

class RestoreSessionUseCase(
    private val repository: ItemsRepository,
    private val preferences: UserPreferencesRepository,
    private val sessionManager: SessionManager
) {
    suspend operator fun invoke(): Boolean {
        val savedToken = preferences.userToken.first()
        if (savedToken.isNullOrEmpty()) return false

        try {
            repository.loginWithToken(savedToken)
            sessionManager.onLoginSuccess(savedToken)
        } catch (e: Exception) {
            sessionManager.onSessionExpired()
            throw e
        }

        return true
    }
}
