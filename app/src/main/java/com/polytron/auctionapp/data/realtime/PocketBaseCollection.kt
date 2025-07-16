package com.yourapp.pocketbase

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.readUTF8Line
import kotlinx.coroutines.*
import kotlinx.serialization.json.*
import java.io.Closeable

data class RealtimeEvent(
    val action: String,
    val record: JsonObject
)

class PocketBaseCollection(
    private val baseUrl: String,
    private val name: String
) : Closeable {
    private var sseJob: Job? = null
    private val client = HttpClient(CIO)

    fun subscribe(
        token: String,
        scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
        onEvent: (RealtimeEvent) -> Unit
    ) {
        sseJob?.cancel() // pastikan tidak ganda

        val realtimeUrl = "$baseUrl/api/realtime"

        sseJob = scope.launch {
            try {
                println("🔌 Connecting to PocketBase SSE...")
                val response: HttpResponse = client.get(realtimeUrl) {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }

                val channel = response.bodyAsChannel()
                val buffer = StringBuilder()

                while (!channel.isClosedForRead) {
                    val line = channel.readUTF8Line() ?: continue

                    if (line.isBlank()) {
                        // selesai satu event
                        val lines = buffer.lines()
                        val action = lines.firstOrNull { it.startsWith("event:") }
                            ?.removePrefix("event:")?.trim() ?: "message"
                        val dataLine = lines.firstOrNull { it.startsWith("data:") }
                            ?.removePrefix("data:")?.trim()

                        if (dataLine != null) {
                            val json = Json.parseToJsonElement(dataLine).jsonObject
                            val event = RealtimeEvent(action, json["record"]!!.jsonObject)
                            onEvent(event)
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
        client.close()
    }
}
