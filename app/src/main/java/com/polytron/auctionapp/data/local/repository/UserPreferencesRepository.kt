package com.polytron.auctionapp.data.local.repository

import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    // Tambahkan parameter name
    suspend fun saveLogin(email: String, password: String, token: String, idUser: String, name: String, avatar: String? = null)
    suspend fun clearLogin()

    val userEmail: Flow<String?>
    val userPassword: Flow<String?>
    val userToken: Flow<String?>
    val userIdUser: Flow<String?>
    val userName: Flow<String?> // Tambahkan ini
    val userAvatar: Flow<String?>
    val isLoggedIn: Flow<Boolean>
}