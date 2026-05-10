package com.polytron.auctionapp.test

import com.polytron.auctionapp.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * In-memory fake for UserPreferencesRepository.
 * Tracks clearLogin call count for verification.
 * Reusable across all test classes.
 */
class FakeUserPreferencesRepository : UserPreferencesRepository {
    var clearLoginCount = 0
        private set

    var saveLoginCount = 0
        private set

    var lastSavedEmail: String? = null
        private set
    var lastSavedToken: String? = null
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
        saveLoginCount++
        lastSavedEmail = email
        lastSavedToken = token
        _userEmail.value = email
        _userPassword.value = password
        _userToken.value = token
        _userIdUser.value = idUser
        _userName.value = name
        _userAvatar.value = avatar
        _isLoggedIn.value = true
    }

    override suspend fun clearLogin() {
        clearLoginCount++
        _userEmail.value = null
        _userPassword.value = null
        _userToken.value = null
        _userIdUser.value = null
        _userName.value = null
        _userAvatar.value = null
        _isLoggedIn.value = false
    }

    /** Preset token for restore-session tests. */
    fun presetToken(token: String) {
        _userToken.value = token
    }
}
