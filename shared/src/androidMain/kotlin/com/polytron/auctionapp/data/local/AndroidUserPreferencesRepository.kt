package com.polytron.auctionapp.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.polytron.auctionapp.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.auctionUserDataStore by preferencesDataStore(name = "user_prefs")

class AndroidUserPreferencesRepository(
    private val context: Context
) : UserPreferencesRepository {
    private companion object {
        val KEY_EMAIL = stringPreferencesKey("email")
        val KEY_PASSWORD = stringPreferencesKey("password")
        val KEY_TOKEN = stringPreferencesKey("token")
        val KEY_ID_USER = stringPreferencesKey("id_user")
        val KEY_NAME = stringPreferencesKey("user_name")
        val KEY_AVATAR = stringPreferencesKey("user_avatar")
        val KEY_IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    }

    override suspend fun saveLogin(
        email: String,
        password: String,
        token: String,
        idUser: String,
        name: String,
        avatar: String?
    ) {
        context.auctionUserDataStore.edit { prefs ->
            prefs[KEY_EMAIL] = email
            prefs[KEY_PASSWORD] = password
            prefs[KEY_TOKEN] = token
            prefs[KEY_ID_USER] = idUser
            prefs[KEY_NAME] = name
            prefs[KEY_AVATAR] = avatar.orEmpty()
            prefs[KEY_IS_LOGGED_IN] = true
        }
    }

    override suspend fun clearLogin() {
        context.auctionUserDataStore.edit { prefs ->
            prefs.clear()
        }
    }

    override val userEmail: Flow<String?> = context.auctionUserDataStore.data.map { it[KEY_EMAIL] }
    override val userPassword: Flow<String?> = context.auctionUserDataStore.data.map { it[KEY_PASSWORD] }
    override val userToken: Flow<String?> = context.auctionUserDataStore.data.map { it[KEY_TOKEN] }
    override val userIdUser: Flow<String?> = context.auctionUserDataStore.data.map { it[KEY_ID_USER] }
    override val userName: Flow<String?> = context.auctionUserDataStore.data.map { it[KEY_NAME] }
    override val userAvatar: Flow<String?> = context.auctionUserDataStore.data.map { it[KEY_AVATAR] }
    override val isLoggedIn: Flow<Boolean> = context.auctionUserDataStore.data.map {
        it[KEY_IS_LOGGED_IN] ?: false
    }
}
