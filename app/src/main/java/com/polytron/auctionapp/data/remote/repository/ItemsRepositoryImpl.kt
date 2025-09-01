package com.polytron.auctionapp.data.remote.repository

import com.polytron.auctionapp.data.remote.model.AuthResult
import com.polytron.auctionapp.data.remote.model.RealtimeSse
import com.polytron.auctionapp.model.ItemResponse
import io.github.agrevster.pocketbaseKotlin.PocketbaseClient
import io.github.agrevster.pocketbaseKotlin.dsl.login
import io.github.agrevster.pocketbaseKotlin.models.AuthRecord
import io.ktor.client.call.*
import io.ktor.client.plugins.sse.sse
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.sse.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*

class ItemsRepositoryImpl(
    private val baseHost: String = "pb.janissaryid.com",
    private val collectionItems: String = "Items",
) : ItemsRepository {

    private val client = PocketbaseClient(
        baseUrl = {
            protocol = URLProtocol.HTTPS
            host = baseHost
        }
    )

    override suspend fun loginWithEmailPassword(email: String, password: String): AuthResult {
        val loginResult = client.records.authWithPassword<AuthRecord>(
            collection = "users",
            email = email,
            password = password
        )
        return AuthResult(
            token = loginResult.token,
            userId = loginResult.record.id.orEmpty()
        )
    }

    override suspend fun loginWithToken(token: String) {
        client.login(token)
    }

    override suspend fun getItems(page: Int, perPage: Int): List<ItemResponse> {
        val fetched = client.records.getList<ItemResponse>(
            sub = collectionItems,
            page = page,
            perPage = perPage
        )
        // sesuai versi lama: reversed
        return fetched.items.reversed()
    }

    override suspend fun createItem(item: ItemResponse): ItemResponse {
        return client.records.create(
            sub = collectionItems,
            body = Json.encodeToString(item)
        )
    }

    override suspend fun updateItem(id: String, item: ItemResponse): ItemResponse {
        return client.records.update(
            id = id,
            sub = collectionItems,
            body = Json.encodeToString(item)
        )
    }

    override suspend fun deleteItem(id: String) {
        client.records.delete(id = id, sub = collectionItems)
    }

    override suspend fun withRealtimeEvents(onEvent: suspend (RealtimeSse) -> Unit) {
        // Tidak ada launch di sini. Caller (VM) yang memanggil dalam scope-nya.
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
    }

    override suspend fun subscribeRealtime(clientId: String, collections: List<String>): Boolean {
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
        // Anda bisa validasi response bila perlu
        return response.status.isSuccess()
    }
}