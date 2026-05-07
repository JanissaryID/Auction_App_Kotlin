package com.polytron.auctionapp.desktop.data.remote

import io.github.agrevster.pocketbaseKotlin.PocketbaseClient
import io.github.agrevster.pocketbaseKotlin.dsl.login
import io.github.agrevster.pocketbaseKotlin.models.AuthRecord
import io.ktor.http.URLProtocol
import kotlinx.serialization.json.Json

/**
 * Desktop PocketBase repository — handles all API calls to PocketBase.
 * Mirrors Android's ItemsRepositoryImpl but without Android-specific dependencies.
 */
class PocketBaseRepository(
    private val baseHost: String = "pb.janissaryid.com",
    private val collectionItems: String = "Items"
) {
    private val client = PocketbaseClient(
        baseUrl = {
            protocol = URLProtocol.HTTPS
            host = baseHost
        }
    )

    // =========================================================================
    // Auth
    // =========================================================================
    suspend fun loginWithEmailPassword(email: String, password: String): AuthResult {
        val loginResult = client.records.authWithPassword<AuthRecord>(
            collection = "users",
            email = email,
            password = password
        )

        val userId = loginResult.record.id.orEmpty()
        client.login(loginResult.token)

        val userProfile = getUser(userId)

        return AuthResult(
            token = loginResult.token,
            userId = userId,
            name = userProfile.name,
            avatar = userProfile.avatar
        )
    }

    suspend fun getUser(id: String): User {
        return client.records.getOne<User>(
            sub = "users",
            id = id
        )
    }

    suspend fun loginWithToken(token: String) {
        client.login(token)
    }

    // =========================================================================
    // CRUD
    // =========================================================================
    suspend fun getItems(page: Int = 1, perPage: Int = 500): List<ItemResponse> {
        val fetched = client.records.getList<ItemResponse>(
            sub = collectionItems,
            page = page,
            perPage = perPage
        )
        return fetched.items.reversed()
    }

    suspend fun createItem(item: ItemResponse): ItemResponse {
        return client.records.create(
            sub = collectionItems,
            body = Json.encodeToString(item)
        )
    }

    suspend fun updateItem(id: String, item: ItemResponse): ItemResponse {
        return client.records.update(
            id = id,
            sub = collectionItems,
            body = Json.encodeToString(item)
        )
    }

    suspend fun deleteItem(id: String) {
        client.records.delete(id = id, sub = collectionItems)
    }
}
