package com.polytron.auctionapp.domain.usecase.auth

import com.polytron.auctionapp.domain.session.SessionManager

class LogoutUseCase(
    private val sessionManager: SessionManager
) {
    suspend operator fun invoke() {
        sessionManager.onLogout()
    }
}
