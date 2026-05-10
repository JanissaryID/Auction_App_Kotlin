package com.polytron.auctionapp.domain.usecase.auth

import com.polytron.auctionapp.domain.session.SessionManager
import com.polytron.auctionapp.test.FakeItemsRepository
import com.polytron.auctionapp.test.FakeUserPreferencesRepository
import com.polytron.auctionapp.test.NoOpLogger
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LoginUseCaseTest {

    private fun createDeps(): Triple<FakeItemsRepository, FakeUserPreferencesRepository, SessionManager> {
        val repo = FakeItemsRepository()
        val prefs = FakeUserPreferencesRepository()
        val session = SessionManager(prefs, NoOpLogger)
        return Triple(repo, prefs, session)
    }

    @Test
    fun loginSuccessSavesPreferencesAndStartsSession() = runBlocking {
        val (repo, prefs, session) = createDeps()
        val useCase = LoginUseCase(repo, prefs, session)

        val result = useCase(email = "gkj@kudus.com", password = "gkjkudus")

        assertEquals("fake-token", result.token)
        assertEquals("user-1", result.userId)
        assertEquals(1, repo.loginWithEmailPasswordCount)
        assertEquals(1, repo.loginWithTokenCount)
        assertEquals("fake-token", repo.lastLoginToken)
        assertEquals(1, prefs.saveLoginCount)
        assertEquals("gkj@kudus.com", prefs.lastSavedEmail)
        assertEquals("fake-token", prefs.lastSavedToken)
        assertEquals(true, session.isLoggedIn.value)
        assertEquals("fake-token", session.token.value)
    }

    @Test
    fun loginFailurePropagatesException() = runBlocking {
        val (repo, prefs, session) = createDeps()
        repo.loginShouldFail = true
        val useCase = LoginUseCase(repo, prefs, session)

        assertFailsWith<RuntimeException> {
            useCase(email = "bad@test.com", password = "wrong")
        }

        assertEquals(0, prefs.saveLoginCount)
        assertEquals(false, session.isLoggedIn.value)
    }
}
