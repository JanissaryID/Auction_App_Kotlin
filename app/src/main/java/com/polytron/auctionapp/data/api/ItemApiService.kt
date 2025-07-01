package com.polytron.auctionapp.data.api

import com.polytron.auctionapp.model.Item
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.contentType

class ItemApiService(
    private val client: HttpClient,
    private val baseUrl: String
) {
    private fun HttpRequestBuilder.addHeaders(headers: Map<String, String>) {
        headers.forEach { (key, value) -> header(key, value) }
    }

    suspend fun fetchAll(headers: Map<String, String>, limit: Int = 500): List<Item> {
        return client.get("$baseUrl/GerejaItems") {
            url.parameters.append("\$limit", limit.toString())
            addHeaders(headers)
        }.body()
    }

    suspend fun getById(headers: Map<String, String>, id: String): Item {
        return client.get("$baseUrl/GerejaItems/$id") {
            addHeaders(headers)
        }.body()
    }

    suspend fun create(headers: Map<String, String>, bodyObj: Item): Item {
        return client.post("$baseUrl/GerejaItems") {
            contentType(ContentType.Application.Json)
            setBody(bodyObj)
            addHeaders(headers)
        }.body()
    }

    suspend fun update(headers: Map<String, String>, id: String, bodyObj: Item): Item {
        return client.patch("$baseUrl/GerejaItems/$id") {
            contentType(ContentType.Application.Json)
            setBody(bodyObj)
            addHeaders(headers)
        }.body()
    }

    suspend fun delete(headers: Map<String, String>, id: String): Item {
        return client.delete("$baseUrl/GerejaItems/$id") {
            addHeaders(headers)
        }.body()
    }
}
