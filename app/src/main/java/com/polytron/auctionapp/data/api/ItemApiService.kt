package com.polytron.auctionapp.data.api

import com.polytron.auctionapp.model.ItemRequest
import com.polytron.auctionapp.model.ItemResponse
import com.polytron.auctionapp.model.Items
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class ItemApiService(
    private val client: HttpClient,
    private val baseUrl: String
) {
    private fun HttpRequestBuilder.addHeaders(headers: Map<String, String>) {
        headers.forEach { (key, value) -> header(key, value) }
    }

    suspend fun fetchAll(headers: Map<String, String>, limit: Int = 500): Items {
        return client.get("$baseUrl/Items/records") {
            url.parameters.append("\$limit", limit.toString())
            addHeaders(headers)
        }.body()
    }

    suspend fun getById(headers: Map<String, String>, id: String): ItemResponse {
        return client.get("$baseUrl/Items/records/$id") {
            addHeaders(headers)
        }.body()
    }

    suspend fun create(headers: Map<String, String>, bodyObj: ItemRequest): ItemResponse {
        return client.post("$baseUrl/Items/records") {
            contentType(ContentType.Application.Json)
            setBody(bodyObj)
            addHeaders(headers)
        }.body()
    }

    suspend fun update(headers: Map<String, String>, id: String, bodyObj: ItemRequest): ItemResponse {
        return client.patch("$baseUrl/Items/records/$id") {
            contentType(ContentType.Application.Json)
            setBody(bodyObj)
            addHeaders(headers)
        }.body()
    }

    suspend fun delete(headers: Map<String, String>, id: String): ItemResponse {
        return client.delete("$baseUrl/Items/records/$id") {
            addHeaders(headers)
        }.body()
    }
}
