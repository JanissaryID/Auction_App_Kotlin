package com.polytron.auctionapp.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SharedUtilsTest {
    @Test
    fun formatRupiahFormatsNumericStrings() {
        assertEquals("Rp 1.234.567", formatRupiah("1234567"))
        assertEquals("Rp -1.234", formatRupiah("-1234"))
        assertEquals("Rp 0", formatRupiah("0"))
    }

    @Test
    fun formatRupiahReturnsDashForInvalidValues() {
        assertEquals("-", formatRupiah(null))
        assertEquals("-", formatRupiah(""))
        assertEquals("-", formatRupiah("1.000"))
    }

    @Test
    fun formatCurrencyInputFiltersAndGroupsDigits() {
        assertEquals("1.234.567", formatCurrencyInput("Rp 1.234.567"))
        assertEquals("0", formatCurrencyInput("000"))
        assertEquals("", formatCurrencyInput(""))
    }

    @Test
    fun formatCurrencyInputReturnsEmptyWhenValueExceedsLongRange() {
        assertEquals("", formatCurrencyInput("9223372036854775808"))
    }

    @Test
    fun generateRandomAlphanumericUsesExpectedShape() {
        val allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toSet()

        repeat(100) {
            val value = generateRandomAlphanumeric()
            assertEquals(6, value.length)
            assertTrue(value.all { char -> char in allowedChars })
        }

        assertEquals(10, generateRandomAlphanumeric(10).length)
        assertEquals("", generateRandomAlphanumeric(0))
    }
}
