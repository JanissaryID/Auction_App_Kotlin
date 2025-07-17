package com.yourapp.pocketbase

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.sse.SSE
import io.ktor.utils.io.core.Closeable
import kotlin.time.Duration.Companion.seconds

class PocketBaseClient(val baseUrl: String) : Closeable {
    val client = HttpClient(CIO) {
        install(SSE) {
            maxReconnectionAttempts = 5
            reconnectionTime = 3.seconds
            showCommentEvents()
            showRetryEvents()
        }
    }

    fun collection(name: String): PocketBaseCollection {
        return PocketBaseCollection(baseUrl, name, client)
    }

    override fun close() {
        client.close()
    }
}
