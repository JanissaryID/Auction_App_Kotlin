package com.polytron.auctionapp.domain.usecase.auth

import com.polytron.auctionapp.domain.session.SessionManager
import com.polytron.auctionapp.test.FakeUserPreferencesRepository
import com.polytron.auctionapp.test.NoOpLogger
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

class LogoutUseCaseTest {

    @Test
    fun logoutClearsSessionAndPreferences() = runBlocking {
        val prefs = FakeUserPreferencesRepository()
        val session = SessionManager(prefs, NoOpLogger)
        val useCase = LogoutUseCase(session)

        session.onLoginSuccess("token-1")
        useCase()

        assertFalse(session.isLoggedIn.value)
        assertNull(session.token.value)
        assertEquals(1, prefs.clearLoginCount)
    }
}
