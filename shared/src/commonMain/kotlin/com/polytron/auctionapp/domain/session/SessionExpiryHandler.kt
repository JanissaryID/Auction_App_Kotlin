package com.polytron.auctionapp.domain.session

interface SessionExpiryHandler {
    suspend fun onSessionExpired()
}
