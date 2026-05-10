package com.polytron.auctionapp.domain.usecase.auth

import com.polytron.auctionapp.domain.session.SessionManager
import com.polytron.auctionapp.test.FakeItemsRepository
import com.polytron.auctionapp.test.FakeUserPreferencesRepository
import com.polytron.auctionapp.test.NoOpLogger
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RestoreSessionUseCaseTest {

    @Test
    fun restoreWithSavedTokenSucceeds() = runBlocking {
        val repo = FakeItemsRepository()
        val prefs = FakeUserPreferencesRepository()
        val session = SessionManager(prefs, NoOpLogger)
        prefs.presetToken("saved-token")

        val useCase = RestoreSessionUseCase(repo, prefs, session)
        val result = useCase()

        assertTrue(result)
        assertEquals(1, repo.loginWithTokenCount)
        assertEquals("saved-token", repo.lastLoginToken)
        assertTrue(session.isLoggedIn.value)
        assertEquals("saved-token", session.token.value)
    }

    @Test
    fun restoreWithNoTokenReturnsFalse() = runBlocking {
        val repo = FakeItemsRepository()
        val prefs = FakeUserPreferencesRepository()
        val session = SessionManager(prefs, NoOpLogger)

        val useCase = RestoreSessionUseCase(repo, prefs, session)
        val result = useCase()

        assertFalse(result)
        assertEquals(0, repo.loginWithTokenCount)
        assertFalse(session.isLoggedIn.value)
    }

    @Test
    fun restoreWithTokenButLoginFailsExpiresSession() = runBlocking {
        val repo = FakeItemsRepository()
        repo.loginShouldFail = true
        val prefs = FakeUserPreferencesRepository()
        val session = SessionManager(prefs, NoOpLogger)
        prefs.presetToken("expired-token")

        val useCase = RestoreSessionUseCase(repo, prefs, session)

        assertFailsWith<RuntimeException> {
            useCase()
        }

        assertFalse(session.isLoggedIn.value)
        assertEquals(1, prefs.clearLoginCount) // session expired clears login
    }
}
