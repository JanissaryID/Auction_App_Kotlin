package com.polytron.auctionapp.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extension DataStore
val Context.dataStore by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {

    companion object {
        val KEY_EMAIL = stringPreferencesKey("email")
        val KEY_PASSWORD = stringPreferencesKey("password")
        val KEY_TOKEN = stringPreferencesKey("token")
        val KEY_ID_USER = stringPreferencesKey("id_user")
        val KEY_IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    }

    // ✅ Simpan data login
    suspend fun saveLogin(email: String, password: String, token: String, idUser: String) {
//        Log.i("DataStore", "Token: $token")
        context.dataStore.edit { prefs ->
            prefs[KEY_EMAIL] = email
            prefs[KEY_PASSWORD] = password
            prefs[KEY_TOKEN] = token
            prefs[KEY_ID_USER] = idUser
            prefs[KEY_IS_LOGGED_IN] = true
        }
    }

    // 🧹 Hapus semua data login
    suspend fun clearLogin() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }

    // 🔁 Data sebagai Flow (observasi)
    val userEmail: Flow<String?> = context.dataStore.data.map { it[KEY_EMAIL] }
    val userPassword: Flow<String?> = context.dataStore.data.map { it[KEY_PASSWORD] }
    val userToken: Flow<String?> = context.dataStore.data.map { it[KEY_TOKEN] }
    val userIdUser: Flow<String?> = context.dataStore.data.map { it[KEY_ID_USER] }
    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { it[KEY_IS_LOGGED_IN] ?: false }
}