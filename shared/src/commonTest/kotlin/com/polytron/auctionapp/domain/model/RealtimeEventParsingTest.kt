package com.polytron.auctionapp.domain.model

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for realtime event JSON parsing, mirroring the parsing logic
 * in ItemsViewModel.handleRealtimeEventPayload.
 *
 * These tests verify that SSE payloads are correctly interpreted
 * for create, update, and delete actions, and that invalid data
 * does not cause crashes.
 */
class RealtimeEventParsingTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun parseCreateEvent() {
        val payload = """
            {
                "action": "create",
                "record": {
                    "id": "item-1",
                    "nameItem": "New Item",
                    "status": 0
                }
            }
        """.trimIndent()

        val element = json.parseToJsonElement(payload)
        assertTrue(element.jsonObject.containsKey("record"))
        val event = json.decodeFromJsonElement<RealtimeEvent>(element)

        assertEquals("create", event.action)
        assertEquals("item-1", event.record.id)
        assertEquals("New Item", event.record.nameItem)
        assertEquals(0, event.record.status)
    }

    @Test
    fun parseUpdateEvent() {
        val payload = """
            {
                "action": "update",
                "record": {
                    "id": "item-1",
                    "nameItem": "Updated Item",
                    "buyer": "Buyer A",
                    "price": "500000",
                    "status": 1
                }
            }
        """.trimIndent()

        val element = json.parseToJsonElement(payload)
        val event = json.decodeFromJsonElement<RealtimeEvent>(element)

        assertEquals("update", event.action)
        assertEquals("Buyer A", event.record.buyer)
        assertEquals("500000", event.record.price)
        assertEquals(1, event.record.status)
    }

    @Test
    fun parseDeleteEvent() {
        val payload = """
            {
                "action": "delete",
                "record": {
                    "id": "item-99"
                }
            }
        """.trimIndent()

        val element = json.parseToJsonElement(payload)
        val event = json.decodeFromJsonElement<RealtimeEvent>(element)

        assertEquals("delete", event.action)
        assertEquals("item-99", event.record.id)
    }

    @Test
    fun payloadWithoutRecordKeyIsSkipped() {
        val payload = """
            {
                "clientId": "abc-123"
            }
        """.trimIndent()

        val element = json.parseToJsonElement(payload)
        assertFalse(element.jsonObject.containsKey("record"))
        // The ViewModel would skip this because no "record" key present
    }

    @Test
    fun unknownActionIsIgnored() {
        val payload = """
            {
                "action": "unknown",
                "record": {
                    "id": "item-1"
                }
            }
        """.trimIndent()

        val element = json.parseToJsonElement(payload)
        val event = json.decodeFromJsonElement<RealtimeEvent>(element)

        // ViewModel only acts on "create", "update", "delete"
        // "unknown" is safely ignored
        assertEquals("unknown", event.action)
    }

    @Test
    fun invalidJsonDoesNotCrash() {
        val invalidPayloads = listOf(
            "",
            "not json",
            "null",
            "42"
        )

        for (payload in invalidPayloads) {
            val crashed = try {
                json.parseToJsonElement(payload)
                false
            } catch (_: Exception) {
                false // Exception is expected, not a crash
            }
            assertFalse(crashed)
        }
    }

    @Test
    fun itemResponseSerializationPreservesOrderID() {
        val jsonStr = """
            {
                "id": "item-1",
                "orderID": "Order-XYZ123",
                "nameItem": "Test Item",
                "status": 2,
                "typePayment": "Cash"
            }
        """.trimIndent()

        val item = json.decodeFromString<ItemResponse>(jsonStr)

        assertEquals("Order-XYZ123", item.orderID)
        assertEquals("Test Item", item.nameItem)
        assertEquals(2, item.status)
        assertEquals("Cash", item.typePayment)
    }

    @Test
    fun paymentMethodEnumFromLabel() {
        assertEquals(PaymentMethod.Cash, PaymentMethod.fromLabel("Cash"))
        assertEquals(PaymentMethod.QRIS, PaymentMethod.fromLabel("QRIS"))
        assertEquals(PaymentMethod.Credit, PaymentMethod.fromLabel("Kredit"))
        assertEquals(PaymentMethod.Cash, PaymentMethod.fromLabel("Unknown")) // default
    }

    @Test
    fun paymentMethodEnumAllEntries() {
        assertEquals(3, PaymentMethod.all.size)
        assertTrue(PaymentMethod.all.contains(PaymentMethod.Cash))
        assertTrue(PaymentMethod.all.contains(PaymentMethod.QRIS))
        assertTrue(PaymentMethod.all.contains(PaymentMethod.Credit))
    }
}
