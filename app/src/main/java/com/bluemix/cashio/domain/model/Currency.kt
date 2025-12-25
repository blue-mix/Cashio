package com.bluemix.cashio.domain.model

/**
 * Supported currencies
 */
data class Currency(
    val code: String,
    val symbol: String,
    val name: String
) {
    companion object {
        val USD = Currency("USD", "$", "US Dollar")
        val EUR = Currency("EUR", "€", "Euro")
        val GBP = Currency("GBP", "£", "British Pound")
        val INR = Currency("INR", "₹", "Indian Rupee")
        val JPY = Currency("JPY", "¥", "Japanese Yen")
        val AUD = Currency("AUD", "A$", "Australian Dollar")
        val CAD = Currency("CAD", "C$", "Canadian Dollar")
        val CNY = Currency("CNY", "¥", "Chinese Yuan")

        fun getAll(): List<Currency> = listOf(
            USD, EUR, GBP, INR, JPY, AUD, CAD, CNY
        )

        fun fromCode(code: String): Currency? = getAll().find { it.code == code }
    }
}
