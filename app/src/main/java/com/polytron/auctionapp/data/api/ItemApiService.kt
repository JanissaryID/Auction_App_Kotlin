package com.polytron.auctionapp.data.api

import com.polytron.auctionapp.model.ItemResponse
import com.polytron.auctionapp.model.Items
import com.polytron.auctionapp.model.UserRequest
import com.polytron.auctionapp.model.UserResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.json.Json

class ItemApiService(
    private val client: HttpClient,
    private val baseUrl: String
) {
    private fun HttpRequestBuilder.authHeader(token: String) {
        if (token.isNotBlank()) {
            header("Authorization", "Bearer $token")
        }
    }

    suspend fun fetchAll(
        token: String,
        perPage: Int = 100,
        page: Int = 1
    ): Items {
        return client.get("$baseUrl/Items/records") {
            url.parameters.append("perPage", perPage.toString())
            url.parameters.append("page", page.toString())
            authHeader(token)
        }.body()
    }

    suspend fun getById(token: String, id: String): ItemResponse {
        return client.get("$baseUrl/Items/records/$id") {
            authHeader(token)
        }.body()
    }

    suspend fun create(token: String, bodyObj: ItemResponse): ItemResponse {
        return client.post("$baseUrl/Items/records") {
            contentType(ContentType.Application.Json)
            setBody(bodyObj)
            authHeader(token)
        }.body()
    }

    suspend fun update(token: String, id: String, bodyObj: ItemResponse): ItemResponse {
        return client.patch("$baseUrl/Items/records/$id") {
            contentType(ContentType.Application.Json)
            setBody(bodyObj)
            authHeader(token)
        }.body()
    }

    suspend fun delete(token: String, id: String): Boolean {
        val response = client.delete("$baseUrl/Items/records/$id") {
            authHeader(token)
        }

        return response.status == HttpStatusCode.NoContent // 204
    }

    suspend fun login(bodyObj: UserRequest): UserResponse {
        return client.post("$baseUrl/users/auth-with-password") {
            contentType(ContentType.Application.Json)
            setBody(bodyObj)
        }.body()
    }

//    suspend fun login(bodyObj: UserRequest): UserResponse {
//        val response = client.post("$baseUrl/users/auth-with-password") {
//            contentType(ContentType.Application.Json)
//            setBody(bodyObj)
//        }
//
//        // Log kode HTTP
//        println("🔎 Response Code: ${response.status}")
//
//        // Log body mentah (string dulu)
//        val rawBody = response.bodyAsText()
//        println("📦 Raw Body: $rawBody")
//
//        // Parse ke UserResponse (pastikan cocok)
//        return Json.decodeFromString<UserResponse>(rawBody)
//    }

}