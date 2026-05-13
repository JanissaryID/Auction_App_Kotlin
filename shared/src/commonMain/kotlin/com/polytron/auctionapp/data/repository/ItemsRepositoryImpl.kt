package com.polytron.auctionapp.data.repository

import com.polytron.auctionapp.data.remote.dto.PocketBaseAuthResponse
import com.polytron.auctionapp.data.remote.dto.PocketBaseListResponse
import com.polytron.auctionapp.domain.model.AuthResult
import com.polytron.auctionapp.domain.model.ItemResponse
import com.polytron.auctionapp.domain.model.RealtimeSse
import com.polytron.auctionapp.domain.model.User
import com.polytron.auctionapp.domain.repository.ItemsRepository
import com.polytron.auctionapp.domain.session.SessionExpiryHandler
import io.ktor.client.HttpClient
import io.ktor.client.plugins.sse.SSEClientException
import io.ktor.client.plugins.sse.sse
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLProtocol
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.http.path
import kotlinx.coroutines.flow.collect
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

class ItemsRepositoryImpl(
    private val httpClient: HttpClient,
    private val sessionExpiryHandler: SessionExpiryHandler,
    private val baseHost: String = "pb.janissaryid.com",
    private val collectionItems: String = "Items",
    private val collectionUsers: String = "users"
) : ItemsRepository {
    private val json = Json { ignoreUnknownKeys = true }
    /** Encoder that omits null fields – used for create/update requests
     *  so PocketBase does not receive read-only fields (id, collectionId, etc.). */
    private val jsonForWrite = Json {
        ignoreUnknownKeys = true
        encodeDefaults = false
        explicitNulls = false
    }
    private var authToken: String? = null

    override suspend fun loginWithEmailPassword(email: String, password: String): AuthResult {
        val body = buildJsonObject {
            put("identity", JsonPrimitive(email))
            put("password", JsonPrimitive(password))
        }

        val authResponse = request(
            method = HttpMethod.Post,
            pathSegments = listOf("api", "collections", collectionUsers, "auth-with-password"),
            body = body.toString(),
            includeAuth = false,
            notifySessionExpiry = false
        ).decode<PocketBaseAuthResponse>()

        authToken = authResponse.token

        val userId = authResponse.record?.id.orEmpty()
        val userProfile = if (userId.isNotBlank()) {
            getUser(userId)
        } else {
            authResponse.record ?: User()
        }

        return AuthResult(
            token = authResponse.token,
            userId = userId,
            name = userProfile.name,
            avatar = userProfile.avatar
        )
    }

    override suspend fun loginWithToken(token: String) {
        authToken = token
    }

    override suspend fun getUser(id: String): User {
        return request(
            method = HttpMethod.Get,
            pathSegments = listOf("api", "collections", collectionUsers, "records", id)
        ).decode()
    }

    override suspend fun getItems(page: Int, perPage: Int): List<ItemResponse> {
        val response = request(
            method = HttpMethod.Get,
            pathSegments = listOf("api", "collections", collectionItems, "records"),
            queryParameters = mapOf(
                "page" to page.toString(),
                "perPage" to perPage.toString()
            )
        ).decode<PocketBaseListResponse<ItemResponse>>()

        return response.items.reversed()
    }

    override suspend fun createItem(item: ItemResponse): ItemResponse {
        return request(
            method = HttpMethod.Post,
            pathSegments = listOf("api", "collections", collectionItems, "records"),
            body = jsonForWrite.encodeToString(item)
        ).decode()
    }

    override suspend fun updateItem(id: String, item: ItemResponse): ItemResponse {
        return request(
            method = HttpMethod.Patch,
            pathSegments = listOf("api", "collections", collectionItems, "records", id),
            body = jsonForWrite.encodeToString(item)
        ).decode()
    }

    override suspend fun deleteItem(id: String) {
        request(
            method = HttpMethod.Delete,
            pathSegments = listOf("api", "collections", collectionItems, "records", id)
        ).ensureSuccess()
    }

    override suspend fun withRealtimeEvents(onEvent: suspend (RealtimeSse) -> Unit) {
        try {
            httpClient.sse(
                scheme = "https",
                host = baseHost,
                path = "/api/realtime",
                request = {
                    applyAuthHeader()
                }
            ) {
                incoming.collect { event ->
                    onEvent(
                        RealtimeSse(
                            id = event.id,
                            data = event.data
                        )
                    )
                }
            }
        } catch (e: SSEClientException) {
            e.response?.status?.notifyIfSessionExpired()
            throw e
        }
    }

    override suspend fun subscribeRealtime(clientId: String, collections: List<String>): Boolean {
        val body = buildJsonObject {
            put("clientId", JsonPrimitive(clientId))
            put("subscriptions", JsonArray(collections.map { JsonPrimitive(it) }))
        }

        return try {
            val response = request(
                method = HttpMethod.Post,
                pathSegments = listOf("api", "realtime"),
                body = body.toString(),
                throwOnHttpError = false
            )
            response.status.notifyIfSessionExpired()
            response.status.isSuccess()
        } catch (_: Exception) {
            false
        }
    }

    private suspend fun request(
        method: HttpMethod,
        pathSegments: List<String>,
        queryParameters: Map<String, String> = emptyMap(),
        body: String? = null,
        includeAuth: Boolean = true,
        notifySessionExpiry: Boolean = true,
        throwOnHttpError: Boolean = true
    ): HttpResponse {
        val response = httpClient.request {
            this.method = method
            url {
                protocol = URLProtocol.HTTPS
                host = baseHost
                path(*pathSegments.toTypedArray())
            }
            queryParameters.forEach { (key, value) -> parameter(key, value) }
            if (includeAuth) applyAuthHeader()
            if (body != null) {
                contentType(ContentType.Application.Json)
                setBody(body)
            }
        }

        if (notifySessionExpiry) {
            response.status.notifyIfSessionExpired()
        }
        if (throwOnHttpError) {
            response.ensureSuccess()
        }

        return response
    }

    private fun io.ktor.client.request.HttpRequestBuilder.applyAuthHeader() {
        authToken?.takeIf { it.isNotBlank() }?.let { token ->
            header(HttpHeaders.Authorization, "Bearer $token")
        }
    }

    private suspend inline fun <reified T> HttpResponse.decode(): T {
        val responseText = ensureSuccess()
        return json.decodeFromString(responseText)
    }

    private suspend fun HttpResponse.ensureSuccess(): String {
        val responseText = bodyAsText()
        if (!status.isSuccess()) {
            throw IllegalStateException("HTTP ${status.value}: $responseText")
        }
        return responseText
    }

    private suspend fun HttpStatusCode.notifyIfSessionExpired() {
        if (value == 401 || value == 403) {
            sessionExpiryHandler.onSessionExpired()
        }
    }
}
