package com.yourapp.pocketbase

import androidx.core.app.PendingIntentCompat.send
import com.polytron.auctionapp.model.RealtimeEvent
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.sse.sse
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
    private val client: HttpClient
) : Closeable {
    private var sseJob: Job? = null

    fun subscribe(
        token: String,
        scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
        onEvent: (RealtimeEvent) -> Unit
    ) {
//        unsubscribe()

        val url = "$baseUrl/api/realtime"

        sseJob = scope.launch {
            try {
                client.sse(
                    urlString = url,
                    request = {
                        headers {
                            append(HttpHeaders.Authorization, "Bearer $token")
                            append(HttpHeaders.Accept, "text/event-stream")
                        }
                    }
                ) {
                    incoming.collect { event ->
                        val raw = event.data ?: return@collect
                        val json = Json.parseToJsonElement(raw).jsonObject

                        val action = json["action"]?.jsonPrimitive?.contentOrNull ?: return@collect
                        val record = json["record"]?.jsonObject ?: return@collect

                        onEvent(RealtimeEvent(action, record))
                    }
                }
            } catch (e: Exception) {
                println("❌ SSE error: ${e.message}")
            }
        }
    }

    fun unsubscribe() {
        sseJob?.cancel()
        sseJob = null
        println("⛔ Unsubscribed from PocketBase collection '$name'")
    }

    override fun close() {
        unsubscribe()
    }
}


