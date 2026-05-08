package com.polytron.auctionapp.domain.session

import com.polytron.auctionapp.core.logging.AppLogger
import com.polytron.auctionapp.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SessionManagerTest {
    @Test
    fun onLoginSuccessStartsSession() {
        val manager = SessionManager(FakeUserPreferencesRepository(), NoOpLogger)

        manager.onLoginSuccess("token-1")

        assertTrue(manager.isLoggedIn.value)
        assertEquals("token-1", manager.token.value)
    }

    @Test
    fun onSessionExpiredClearsLoginOnlyOnceUntilNextLogin() = runBlocking {
        val preferences = FakeUserPreferencesRepository()
        val manager = SessionManager(preferences, NoOpLogger)

        manager.onLoginSuccess("token-1")
        manager.onSessionExpired()
        manager.onSessionExpired()

        assertFalse(manager.isLoggedIn.value)
        assertNull(manager.token.value)
        assertEquals(1, preferences.clearLoginCount)

        manager.onLoginSuccess("token-2")
        manager.onSessionExpired()

        assertEquals(2, preferences.clearLoginCount)
    }

    @Test
    fun onLogoutClearsSessionAndPreferences() = runBlocking {
        val preferences = FakeUserPreferencesRepository()
        val manager = SessionManager(preferences, NoOpLogger)

        manager.onLoginSuccess("token-1")
        manager.onLogout()

        assertFalse(manager.isLoggedIn.value)
        assertNull(manager.token.value)
        assertEquals(1, preferences.clearLoginCount)
    }

    private class FakeUserPreferencesRepository : UserPreferencesRepository {
        var clearLoginCount = 0
            private set

        private val _userEmail = MutableStateFlow<String?>(null)
        override val userEmail: Flow<String?> = _userEmail

        private val _userPassword = MutableStateFlow<String?>(null)
        override val userPassword: Flow<String?> = _userPassword

        private val _userToken = MutableStateFlow<String?>(null)
        override val userToken: Flow<String?> = _userToken

        private val _userIdUser = MutableStateFlow<String?>(null)
        override val userIdUser: Flow<String?> = _userIdUser

        private val _userName = MutableStateFlow<String?>(null)
        override val userName: Flow<String?> = _userName

        private val _userAvatar = MutableStateFlow<String?>(null)
        override val userAvatar: Flow<String?> = _userAvatar

        private val _isLoggedIn = MutableStateFlow(false)
        override val isLoggedIn: Flow<Boolean> = _isLoggedIn

        override suspend fun saveLogin(
            email: String,
            password: String,
            token: String,
            idUser: String,
            name: String,
            avatar: String?
        ) {
            _userEmail.value = email
            _userPassword.value = password
            _userToken.value = token
            _userIdUser.value = idUser
            _userName.value = name
            _userAvatar.value = avatar
            _isLoggedIn.value = true
        }

        override suspend fun clearLogin() {
            clearLoginCount += 1
            _userEmail.value = null
            _userPassword.value = null
            _userToken.value = null
            _userIdUser.value = null
            _userName.value = null
            _userAvatar.value = null
            _isLoggedIn.value = false
        }
    }

    private object NoOpLogger : AppLogger {
        override fun debug(tag: String, message: String) = Unit
        override fun info(tag: String, message: String) = Unit
        override fun warn(tag: String, message: String) = Unit
        override fun error(tag: String, message: String, throwable: Throwable?) = Unit
    }
}
