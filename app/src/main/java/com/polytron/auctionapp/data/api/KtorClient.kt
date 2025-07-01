package com.polytron.auctionapp.data.api

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object KtorClient {
    val httpClient = HttpClient(CIO) {
        // JSON serialization
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true // mengabaikan field tak dikenal
                isLenient = true         // format longgar
                prettyPrint = false      // nonaktif di produksi
                encodeDefaults = true    // sertakan nilai default
            })
        }

        // Timeout settings
        install(HttpTimeout) {
            requestTimeoutMillis = 15_000
            connectTimeoutMillis = 10_000
            socketTimeoutMillis = 10_000
        }

        // Logging
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.BODY // ganti ke INFO atau NONE di produksi
        }

        // Optional: Default request headers (kalau kamu selalu butuh JSON)
        defaultRequest {
            header("Accept", "application/json")
        }
    }
}