package com.yourapp.pocketbase

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.utils.io.core.Closeable

class PocketBaseClient(val baseUrl: String) : Closeable {
    private val client = HttpClient(CIO) {
        engine {
            requestTimeout = 0 // ⏱️ unlimited
        }
    }

    fun collection(name: String): PocketBaseCollection {
        return PocketBaseCollection(baseUrl, name, client)
    }

    override fun close() {
        client.close()
    }
}
