package com.yourapp.pocketbase

import com.polytron.auctionapp.model.RealtimeEvent
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.readUTF8Line
import kotlinx.coroutines.*
import kotlinx.serialization.json.*
import java.io.Closeable

class PocketBaseCollection(
    private val baseUrl: String,
    private val name: String,
    client: HttpClient // gunakan parameter
) : Closeable {
    private var sseJob: Job? = null
    private val client = client // ✅ gunakan client yang dikirim dari PocketBaseClient

    fun subscribe(
        token: String,
        scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
        onEvent: (RealtimeEvent) -> Unit
    ) {
        sseJob?.cancel()

        val realtimeUrl = "$baseUrl/api/realtime?channel=collections/$name"

        sseJob = scope.launch {
            try {
                println("🔌 Connecting to PocketBase SSE...")
                val response: HttpResponse = client.get(realtimeUrl) {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    header(HttpHeaders.Accept, "text/event-stream")
                }

                val channel = response.bodyAsChannel()
                val buffer = StringBuilder()

                while (!channel.isClosedForRead) {
                    val line = channel.readUTF8Line() ?: continue

                    if (line.isBlank()) {
                        val lines = buffer.lines()
                        val action = lines.firstOrNull { it.startsWith("event:") }
                            ?.removePrefix("event:")?.trim() ?: "message"
                        val dataLine = lines.firstOrNull { it.startsWith("data:") }
                            ?.removePrefix("data:")?.trim()

                        if (dataLine != null) {
                            val json = Json.parseToJsonElement(dataLine).jsonObject
                            val record = json["record"]?.jsonObject
                            if (record != null) {
                                val event = RealtimeEvent(action, record)
                                onEvent(event)
                            }
                        }
                        buffer.clear()
                    } else {
                        buffer.appendLine(line)
                    }
                }

                println("⚠️ SSE connection closed.")
            } catch (e: Exception) {
                println("❌ SSE connection error: ${e.message}")
            }
        }
    }

    fun unsubscribe() {
        sseJob?.cancel()
        println("⛔ SSE subscription cancelled.")
    }

    override fun close() {
        unsubscribe()
        // ❗Jangan close client di sini, karena client dimiliki oleh PocketBaseClient
    }
}

