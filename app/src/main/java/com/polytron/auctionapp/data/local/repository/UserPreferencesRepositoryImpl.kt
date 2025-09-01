package com.polytron.auctionapp.data.local.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserPreferencesRepositoryImpl(
    private val context: Context
) : UserPreferencesRepository {

    companion object {
        private val KEY_EMAIL = stringPreferencesKey("email")
        private val KEY_PASSWORD = stringPreferencesKey("password")
        private val KEY_TOKEN = stringPreferencesKey("token")
        private val KEY_ID_USER = stringPreferencesKey("id_user")
        private val KEY_IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    }

    override suspend fun saveLogin(email: String, password: String, token: String, idUser: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_EMAIL] = email
            prefs[KEY_PASSWORD] = password
            prefs[KEY_TOKEN] = token
            prefs[KEY_ID_USER] = idUser
            prefs[KEY_IS_LOGGED_IN] = true
        }
    }

    override suspend fun clearLogin() {
        context.dataStore.edit { prefs -> prefs.clear() }
    }

    override val userEmail: Flow<String?> = context.dataStore.data.map { it[KEY_EMAIL] }
    override val userPassword: Flow<String?> = context.dataStore.data.map { it[KEY_PASSWORD] }
    override val userToken: Flow<String?> = context.dataStore.data.map { it[KEY_TOKEN] }
    override val userIdUser: Flow<String?> = context.dataStore.data.map { it[KEY_ID_USER] }
    override val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { it[KEY_IS_LOGGED_IN] ?: false }
}