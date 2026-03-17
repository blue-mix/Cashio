package com.bluemix.cashio.domain.model

/**
 * Supported currencies.
 *
 * Note: JPY and CNY intentionally share the ¥ symbol. Display logic in the
 * presentation layer should use [code] for disambiguation where needed.
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
        val CNY = Currency("CNY", "¥", "Chinese Yuan")   // ¥ shared with JPY — disambiguate by code

        /** All supported currencies in display order. */
        val ALL: List<Currency> by lazy {
            listOf(USD, EUR, GBP, INR, JPY, AUD, CAD, CNY)
        }

        private val BY_CODE: Map<String, Currency> by lazy {
            ALL.associateBy { it.code }
        }

        /**
         * Returns the [Currency] for [code], or `null` if the code is unrecognised.
         *
         * Callers should provide a sensible fallback:
         * ```
         * val currency = Currency.fromCode(savedCode) ?: Currency.INR
         * ```
         */
        fun fromCode(code: String): Currency? = BY_CODE[code.uppercase()]
    }
}