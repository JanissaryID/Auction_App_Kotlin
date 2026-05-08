package com.polytron.auctionapp.domain.repository

import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository : Repository {
    suspend fun saveLogin(
        email: String,
        password: String,
        token: String,
        idUser: String,
        name: String,
        avatar: String? = null
    )

    suspend fun clearLogin()

    val userEmail: Flow<String?>
    val userPassword: Flow<String?>
    val userToken: Flow<String?>
    val userIdUser: Flow<String?>
    val userName: Flow<String?>
    val userAvatar: Flow<String?>
    val isLoggedIn: Flow<Boolean>
}
