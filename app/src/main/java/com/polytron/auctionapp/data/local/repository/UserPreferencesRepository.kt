package com.polytron.auctionapp.data.local.repository

import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    suspend fun saveLogin(email: String, password: String, token: String, idUser: String)
    suspend fun clearLogin()

    val userEmail: Flow<String?>
    val userPassword: Flow<String?>
    val userToken: Flow<String?>
    val userIdUser: Flow<String?>
    val isLoggedIn: Flow<Boolean>
}