package com.polytron.auctionapp.shared.util

/**
 * Utility functions untuk string operations
 */
object StringUtils {
    
    /**
     * Extract base name dari item name
     * Example: "Sanco - 1" -> "Sanco"
     */
    fun extractBaseName(name: String?): String {
        if (name.isNullOrBlank()) return "Unknown"
        
        // Pattern: "Name - Number" atau "Name-Number"
        val regex = Regex("""^(.+?)\s*-\s*\d+$""")
        val match = regex.find(name)
        
        return match?.groupValues?.get(1)?.trim() ?: name.trim()
    }
    
    /**
     * Parse price string ke Long
     * Removes all non-digit characters
     */
    fun parsePrice(price: String?): Long {
        if (price.isNullOrBlank()) return 0L
        return price.replace(Regex("\\D"), "").toLongOrNull() ?: 0L
    }
    
    /**
     * Format Long ke Rupiah string
     */
    fun formatRupiah(amount: Long): String {
        if (amount == 0L) return "Rp 0"
        
        val formatted = amount.toString()
            .reversed()
            .chunked(3)
            .joinToString(".")
            .reversed()
        
        return "Rp $formatted"
    }
    
    /**
     * Check if string contains query (case insensitive)
     */
    fun containsIgnoreCase(text: String?, query: String?): Boolean {
        if (text.isNullOrBlank() || query.isNullOrBlank()) return false
        return text.contains(query, ignoreCase = true)
    }
}

/**
 * Extension functions
 */
fun String?.toPrice(): Long = StringUtils.parsePrice(this)
fun Long.toRupiah(): String = StringUtils.formatRupiah(this)
fun String?.extractBaseName(): String = StringUtils.extractBaseName(this)
