package com.polytron.auctionapp.data.local

import com.polytron.auctionapp.domain.repository.UserPreferencesRepository
import java.io.File
import java.util.Properties
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class DesktopUserPreferencesRepository(
    private val sessionFile: File = defaultSessionFile()
) : UserPreferencesRepository {
    private val _userEmail = MutableStateFlow<String?>(null)
    override val userEmail: StateFlow<String?> = _userEmail

    private val _userPassword = MutableStateFlow<String?>(null)
    override val userPassword: StateFlow<String?> = _userPassword

    private val _userToken = MutableStateFlow<String?>(null)
    override val userToken: StateFlow<String?> = _userToken

    private val _userIdUser = MutableStateFlow<String?>(null)
    override val userIdUser: StateFlow<String?> = _userIdUser

    private val _userName = MutableStateFlow<String?>(null)
    override val userName: StateFlow<String?> = _userName

    private val _userAvatar = MutableStateFlow<String?>(null)
    override val userAvatar: StateFlow<String?> = _userAvatar

    private val _isLoggedIn = MutableStateFlow(false)
    override val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    init {
        loadFromDisk()
    }

    override suspend fun saveLogin(
        email: String,
        password: String,
        token: String,
        idUser: String,
        name: String,
        avatar: String?
    ) {
        val properties = Properties().apply {
            setProperty(KEY_EMAIL, email)
            setProperty(KEY_PASSWORD, password)
            setProperty(KEY_TOKEN, token)
            setProperty(KEY_ID_USER, idUser)
            setProperty(KEY_NAME, name)
            setProperty(KEY_AVATAR, avatar.orEmpty())
            setProperty(KEY_IS_LOGGED_IN, true.toString())
        }

        sessionFile.parentFile?.mkdirs()
        sessionFile.outputStream().use { output ->
            properties.store(output, "AuctionApp user session")
        }

        applyValues(properties)
    }

    override suspend fun clearLogin() {
        if (sessionFile.exists()) {
            sessionFile.delete()
        }

        _userEmail.value = null
        _userPassword.value = null
        _userToken.value = null
        _userIdUser.value = null
        _userName.value = null
        _userAvatar.value = null
        _isLoggedIn.value = false
    }

    private fun loadFromDisk() {
        if (!sessionFile.exists()) return

        val properties = Properties()
        sessionFile.inputStream().use { input ->
            properties.load(input)
        }
        applyValues(properties)
    }

    private fun applyValues(properties: Properties) {
        _userEmail.value = properties.getProperty(KEY_EMAIL)
        _userPassword.value = properties.getProperty(KEY_PASSWORD)
        _userToken.value = properties.getProperty(KEY_TOKEN)
        _userIdUser.value = properties.getProperty(KEY_ID_USER)
        _userName.value = properties.getProperty(KEY_NAME)
        _userAvatar.value = properties.getProperty(KEY_AVATAR)
        _isLoggedIn.value = properties.getProperty(KEY_IS_LOGGED_IN).toBoolean()
    }

    private companion object {
        const val KEY_EMAIL = "email"
        const val KEY_PASSWORD = "password"
        const val KEY_TOKEN = "token"
        const val KEY_ID_USER = "id_user"
        const val KEY_NAME = "user_name"
        const val KEY_AVATAR = "user_avatar"
        const val KEY_IS_LOGGED_IN = "is_logged_in"

        fun defaultSessionFile(): File {
            val userHome = System.getProperty("user.home")
            return File(File(userHome, ".auctionapp"), "session.properties")
        }
    }
}
