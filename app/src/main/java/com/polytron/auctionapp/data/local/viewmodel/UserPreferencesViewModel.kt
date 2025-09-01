package com.polytron.auctionapp.data.local.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.polytron.auctionapp.data.local.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class UserPreferencesViewModel(
    private val repo: UserPreferencesRepository
) : ViewModel() {

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    private val _token = MutableStateFlow<String?>(null)
    val token: StateFlow<String?> = _token

    private val _idUser = MutableStateFlow<String?>(null)
    val idUser: StateFlow<String?> = _idUser

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    init {
        viewModelScope.launch {
            repo.userEmail.collectLatest { _email.value = it.orEmpty() }
        }
        viewModelScope.launch {
            repo.userPassword.collectLatest { _password.value = it.orEmpty() }
        }
        viewModelScope.launch {
            repo.userToken.collectLatest { _token.value = it }
        }
        viewModelScope.launch {
            repo.userIdUser.collectLatest { _idUser.value = it }
        }
        viewModelScope.launch {
            repo.isLoggedIn.collectLatest { _isLoggedIn.value = it }
        }
    }

    fun saveLogin(email: String, password: String, token: String, idUser: String) {
        viewModelScope.launch {
            repo.saveLogin(email, password, token, idUser)
        }
    }

    fun clearLogin() {
        viewModelScope.launch {
            repo.clearLogin()
        }
    }
}