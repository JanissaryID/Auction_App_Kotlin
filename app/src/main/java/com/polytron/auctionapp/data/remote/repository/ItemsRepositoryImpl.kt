package com.polytron.auctionapp.data.remote.repository

import android.util.Log
import com.polytron.auctionapp.data.remote.model.AuthResult
import com.polytron.auctionapp.data.remote.model.RealtimeSse
import com.polytron.auctionapp.data.remote.model.User
import com.polytron.auctionapp.data.session.SessionManager
import com.polytron.auctionapp.model.ItemResponse
import io.github.agrevster.pocketbaseKotlin.PocketbaseClient
import io.github.agrevster.pocketbaseKotlin.dsl.login
import io.github.agrevster.pocketbaseKotlin.models.AuthRecord
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.sse.sse
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.URLProtocol
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.http.path
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

class ItemsRepositoryImpl(
    private val sessionManager: SessionManager,
    private val baseHost: String = "pb.janissaryid.com",
    private val collectionItems: String = "Items",
) : ItemsRepository {

    private val TAG = "ItemsRepo"

    private val client = PocketbaseClient(
        baseUrl = {
            protocol = URLProtocol.HTTPS
            host = baseHost
        }
    )

    // =========================================================================
    // Safe API Call Wrapper — intercepts 401/403 at the HTTP level
    // =========================================================================
    private suspend fun <T> safeApiCall(block: suspend () -> T): T {
        return try {
            block()
        } catch (e: ClientRequestException) {
            // Ktor throws this for 4xx/5xx when HttpCallValidator is active
            val statusCode = e.response.status.value
            Log.e(TAG, "ClientRequestException: HTTP $statusCode — ${e.message}")
            if (statusCode == 401 || statusCode == 403) {
                sessionManager.onSessionExpired()
            }
            throw e
        } catch (e: Exception) {
            // Fallback: PocketBase library may wrap errors in generic exceptions
            val msg = e.message.orEmpty()
            Log.e(TAG, "API Error: ${e::class.simpleName} — $msg")
            if (msg.contains("401") || msg.contains("403") ||
                msg.contains("unauthorized", ignoreCase = true) ||
                msg.contains("Token is expired", ignoreCase = true) ||
                msg.contains("missing auth", ignoreCase = true)
            ) {
                sessionManager.onSessionExpired()
            }
            throw e
        }
    }

    // =========================================================================
    // Auth
    // =========================================================================
    override suspend fun loginWithEmailPassword(email: String, password: String): AuthResult {
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

    override suspend fun getUser(id: String): User {
        Log.i(TAG, "getUser: $id")
        return client.records.getOne<User>(
            sub = "users",
            id = id
        )
    }

    override suspend fun loginWithToken(token: String) {
        client.login(token)
    }

    // =========================================================================
    // CRUD — all wrapped with safeApiCall
    // =========================================================================
    override suspend fun getItems(page: Int, perPage: Int): List<ItemResponse> {
        return safeApiCall {
            val fetched = client.records.getList<ItemResponse>(
                sub = collectionItems,
                page = page,
                perPage = perPage
            )
            fetched.items.reversed()
        }
    }

    override suspend fun createItem(item: ItemResponse): ItemResponse {
        return safeApiCall {
            client.records.create(
                sub = collectionItems,
                body = Json.encodeToString(item)
            )
        }
    }

    override suspend fun updateItem(id: String, item: ItemResponse): ItemResponse {
        return safeApiCall {
            client.records.update(
                id = id,
                sub = collectionItems,
                body = Json.encodeToString(item)
            )
        }
    }

    override suspend fun deleteItem(id: String) {
        safeApiCall {
            client.records.delete(id = id, sub = collectionItems)
        }
    }

    // =========================================================================
    // Realtime (SSE)
    // =========================================================================
    override suspend fun withRealtimeEvents(onEvent: suspend (RealtimeSse) -> Unit) {
        try {
            client.httpClient.sse(path = "/api/realtime") {
                incoming.collect { evt ->
                    onEvent(
                        RealtimeSse(
                            id = evt.id,
                            data = evt.data
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "SSE connection error: ${e.message}")
        }
    }

    override suspend fun subscribeRealtime(clientId: String, collections: List<String>): Boolean {
        return try {
            val body: JsonObject = buildJsonObject {
                put("clientId", JsonPrimitive(clientId))
                put(
                    "subscriptions",
                    JsonArray(collections.map { JsonPrimitive(it) })
                )
            }

            val response = client.httpClient.post {
                url { path("/api/realtime") }
                contentType(ContentType.Application.Json)
                setBody(body)
            }
            response.status.isSuccess()
        } catch (e: Exception) {
            Log.e(TAG, "Realtime subscribe error: ${e.message}")
            false
        }
    }
}